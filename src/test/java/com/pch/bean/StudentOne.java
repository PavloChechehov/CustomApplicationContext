package com.pch.bean;

import com.pch.Bean;

@Bean
public class StudentOne implements Mentor {

    @Override
    public String work() {
        return "StudentOne does homework";
    }
}
