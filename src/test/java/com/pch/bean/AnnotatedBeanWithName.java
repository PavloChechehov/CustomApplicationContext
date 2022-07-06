package com.pch.bean;

import com.pch.Bean;

@Bean("simpleBean")
public class AnnotatedBeanWithName {

    public String greeting(){
        return "Hello, my name is simpleBean";
    }
}
