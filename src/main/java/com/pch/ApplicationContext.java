package com.pch;

import java.util.Map;

public interface ApplicationContext {

    /*
        getBean(Class<T> beanType) – returns a bean by its type,
            it should throw NoSuchBeanException if nothing is found
            it should throw NoUniqueBeanException if more than one bean is found
        getBean(String name, Class<T> beanType) – returns a bean by its name
            it should throw NoSuchBeanException if nothing is found
        getAllBeans(Class<T> beanType) – returns a map of beans where the key is it’s name and the value is the bean
     */

    <T> T getBean(Class<T> beanType);

    <T> T getBean(String name, Class<T> beanType);

    <T> Map<String, T> getAllBeans(Class<T> beanType);
}
