package com.pch;

import com.pch.exception.NoSuchBeanException;
import com.pch.exception.NoUniqueBeanException;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.reflections.ReflectionUtils.Fields;

public class AnnotationApplicationContext implements ApplicationContext {

    private static final Map<String, Object> ANNOTATED_BEAN_CLASSES = new HashMap<>();
    private static final Map<String, Set<String>> ANNOTATED_INJECT_FIELDS = new HashMap<>();
    private final Reflections reflections;
    private static final Map<Class<?>, Map<String, Object>> BEAN_TYPE_ANNOTATED_BEAN_CLASSES = new HashMap<>();

    @SneakyThrows
    public AnnotationApplicationContext(String packageName) {
        /*
            Scan the package to find all classes annotated with @Bean
            Create instances of those classes
            Resolve a name for each bean
            If annotation has name like this @Bean("coolBean") – use "coolBean"
            Otherwise, use class name with the lowercased first letter. E.g. for PrinterService class – use "printerService"
            Store created object by its name in the application context
         */
        this.reflections = new Reflections(new ConfigurationBuilder().forPackage(packageName));

        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Bean.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            Bean annotation = aClass.getAnnotation(Bean.class);
            String value = annotation.value();
            String name = parseName(value, aClass.getSimpleName());

            //Get parent interfaces to create full hierarchy structure
            Class<?>[] interfaces = aClass.getInterfaces();
            Object instance = aClass.getDeclaredConstructor().newInstance();

            ANNOTATED_BEAN_CLASSES.put(name, instance);

            Map<String, Object> beanNameInstance = new HashMap<>();
            beanNameInstance.put(name, instance);

            BEAN_TYPE_ANNOTATED_BEAN_CLASSES.put(aClass, beanNameInstance);
            for (Class<?> parent : interfaces) {
                Map<String, Object> beanNameInstances = BEAN_TYPE_ANNOTATED_BEAN_CLASSES.get(parent);
                if (beanNameInstances != null) {
                    beanNameInstances.put(name, instance);
                } else {
                    beanNameInstance = new HashMap<>();
                    beanNameInstance.put(name, instance);
                    BEAN_TYPE_ANNOTATED_BEAN_CLASSES.put(parent, beanNameInstance);
                }
            }

        }

        injectInstanceInField();
    }

    @SneakyThrows
    private void injectInstanceInField() {
        for (Object value : ANNOTATED_BEAN_CLASSES.values()) {
            Class<?> aClass = value.getClass();

            //1. get all fields of this class
            //2. filter fields that are annotated with @Inject
            //3. get instance and inject it in this field

            Set<Field> fields = reflections.get(Fields.of(aClass)
                .filter(field -> field.isAnnotationPresent(Inject.class)));

            for (Field field : fields) {
                Class<?> type = field.getType();
                Object currentObjectBean = getBean(aClass);
                Object fieldObjectBean = getBean(type);
                field.setAccessible(true);
                field.set(currentObjectBean, fieldObjectBean);
            }
        }
    }

    private static String parseName(String value, String name) {
        String prettyName = name.substring(0, 1).toLowerCase() + name.substring(1);
        return value.equals("") ? prettyName : value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> beanType) {
        //1. find all classes that have the same beanType
        //2. it could be as recursive or while operation
        //3. filter only classes that annotated with exactly @Bean annotation
        Set<Class<? extends T>> allBeans = new CopyOnWriteArraySet<>();
        allBeans.add(beanType);

        for (Class<? extends T> bean : allBeans) {
            //possible hierarchy of classes
            //GreetingService -> MorningService @Bean
            //               \-> EveningService
            //                                 \-> MidnightService @Bean
            //add all subClasses of this beanType
            allBeans.addAll(reflections.getSubTypesOf(bean));
        }

        List<Class<? extends T>> annotatedClasses = new ArrayList<>();
        for (Class<? extends T> bean : allBeans) {
            Bean annotation = bean.getAnnotation(Bean.class);
            if (annotation != null) {
                annotatedClasses.add(bean);
            }
        }

        if (annotatedClasses.size() > 1) {
            throw new NoUniqueBeanException("More than 2 beans annotated @Bean annotation exist from the same beanType");
        }

        if (annotatedClasses.isEmpty()) {
            throw new NoSuchBeanException("Doesn't exist any class with annotation @Bean");
        }

        //only one unique class with @Bean
        Class<? extends T> annotatedClass = annotatedClasses.get(0);
        Bean annotation = annotatedClass.getAnnotation(Bean.class);
        String beanName = Optional.ofNullable(annotation)
            .map(name -> parseName(name.value(), annotatedClass.getSimpleName()))
            .orElseThrow(() -> new NoSuchBeanException("This beanType is not annotation with @Bean"));

        return (T) ANNOTATED_BEAN_CLASSES.get(beanName);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        Bean annotation = beanType.getAnnotation(Bean.class);
        if (annotation == null) {
            throw new NoSuchBeanException("This beanType is not annotation with @Bean");
        }

        if (annotation.value() != null && !annotation.value().equals(name)) {
            throw new NoSuchBeanException("Bean exists with different value property");
        }

        return (T) ANNOTATED_BEAN_CLASSES.get(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return (Map<String, T>) BEAN_TYPE_ANNOTATED_BEAN_CLASSES.get(beanType);
    }
}
