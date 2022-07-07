package com.pch.bean;

import com.pch.Bean;

@Bean
public class StudentTwo implements Mentor {
    @Override
    public String work() {
        return "StudentTwo does homework";
    }
}
