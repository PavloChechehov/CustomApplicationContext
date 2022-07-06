package com.pch.service;

import com.pch.Bean;

@Bean("simpleService")
public class EveningService implements GreetingService {
    @Override
    public void hello() {
        System.out.println(" - Good evening!");
    }
}
