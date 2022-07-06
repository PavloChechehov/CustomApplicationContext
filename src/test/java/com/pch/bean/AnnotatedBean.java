package com.pch.bean;

import com.pch.Bean;
import com.pch.Inject;

@Bean
public class AnnotatedBean {

    @Inject
    public InjectBean injectBean;

    public String test() {
        return "It is a annotated class";
    }
}
