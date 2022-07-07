package com.pch;

import com.pch.exception.NoSuchBeanException;
import com.pch.exception.NoUniqueBeanException;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.reflections.ReflectionUtils.Fields;

public class AnnotationApplicationContext implements ApplicationContext {

    private static final Map<String, Object> ANNOTATED_BEAN_CLASSES = new HashMap<>();
    private final Reflections reflections;

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

            Object instance = aClass.getDeclaredConstructor().newInstance();
            ANNOTATED_BEAN_CLASSES.put(name, instance);
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
        List<?> list = getStream(beanType)
            .map(Map.Entry::getValue)
            .toList();

        if (list.size() > 1) {
            throw new NoUniqueBeanException(String.format("More than 2 beans with annotation @Bean from this %s beanType",
                beanType.getSimpleName()));
        }

        if (list.isEmpty()) {
            throw new NoSuchBeanException(String.format("Doesn't exist bean with annotation @Bean from this %s beanType",
                beanType.getSimpleName()));
        }

        //only one unique class with @Bean
        return beanType.cast(list.get(0));
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        Map<String, Object> beanTypes = getStream(beanType)
            .filter(entry -> entry.getKey().equals(name))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (beanTypes.isEmpty()) {
            throw new NoSuchBeanException(
                String.format("Doesn't exist bean with annotation @Bean from this %s beanType and name %s",
                    beanType.getSimpleName(), name)
            );
        }

        return beanType.cast(beanTypes.get(name));
    }

    private <T> Stream<Map.Entry<String, Object>> getStream(Class<T> beanType) {
        return ANNOTATED_BEAN_CLASSES.entrySet()
            .stream()
            .filter(entry -> beanType.isAssignableFrom(entry.getValue().getClass()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return getStream(beanType)
            .collect(toMap(Map.Entry::getKey, entry -> beanType.cast(entry.getValue())));
    }
}
