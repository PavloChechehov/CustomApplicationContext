package com.pch;

import com.pch.bean.AnnotatedBean;
import com.pch.bean.AnnotatedBeanWithName;
import com.pch.bean.InjectBean;
import com.pch.bean.Mentor;
import com.pch.bean.NoAnnotatedBean;
import com.pch.bean.StudentOne;
import com.pch.bean.StudentTwo;
import com.pch.exception.NoSuchBeanException;
import com.pch.exception.NoUniqueBeanException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationContextTest {

    private final ApplicationContext applicationContext
        = new AnnotationApplicationContext("com.pch");

    @Test
    void checkIfBeanExists() {
        AnnotatedBean annotatedBean = applicationContext.getBean(AnnotatedBean.class);
        assertNotNull(annotatedBean);
        annotatedBean.test();
    }

    @Test
    void checkIfNoAnnotatedBeanExistsAndThrowException() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean(NoAnnotatedBean.class));
        NoAnnotatedBean bean = new NoAnnotatedBean();
        bean.test();
    }

    @Test
    void moreThatOneBeanFromBeanType() {
        assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(Mentor.class));
    }

    @Test
    void createSpecificBeanFromBeanType() {
        StudentOne studentOne = applicationContext.getBean(StudentOne.class);
        StudentTwo studentTwo = applicationContext.getBean(StudentTwo.class);

        assertNotNull(studentOne);
        assertNotNull(studentTwo);

        assertEquals("StudentOne do homework", studentOne.work());
        assertEquals("StudentTwo do homework", studentTwo.work());
    }

    @Test
    void noSuchBeanTypeByName() {
        assertThrows(NoSuchBeanException.class,
            () -> applicationContext.getBean("bean", AnnotatedBean.class));
    }

    @Test
    void beanTypeWithSpecificName() {
        AnnotatedBeanWithName bean = applicationContext.getBean("simpleBean", AnnotatedBeanWithName.class);

        assertNotNull(bean);
        assertEquals("Hello, my name is simpleBean", bean.greeting());
    }

    @Test
    void findAllBeans() {
        Map<String, Mentor> allBeans = applicationContext.getAllBeans(Mentor.class);
        assertEquals(2, allBeans.size());
        Mentor studentOne = allBeans.get("studentOne");
        Mentor studentTwo = allBeans.get("studentTwo");
        assertEquals("StudentOne do homework", studentOne.work());
        assertEquals("StudentTwo do homework", studentTwo.work());
    }

    @Test
    void findAllBeans2() {
        Map<String, AnnotatedBean> allBeans = applicationContext.getAllBeans(AnnotatedBean.class);
        assertEquals(1, allBeans.size());

        AnnotatedBean annotatedBean = allBeans.get("annotatedBean");
        assertEquals("It is a annotated class", annotatedBean.test());
    }

    @Test
    void findAllBeans3() {
        Map<String, NoAnnotatedBean> allBeans = applicationContext.getAllBeans(NoAnnotatedBean.class);
        assertNull(allBeans);
    }

    @Test
    void getInstanceOfInjectBean() {
        AnnotatedBean annotatedBean = applicationContext.getBean(AnnotatedBean.class);
        InjectBean injectBean = annotatedBean.injectBean;
        assertNotNull(injectBean);

        assertEquals("It is a inject bean", injectBean.test());
    }
}
