package com.pch.service;

import com.pch.Bean;

@Bean
public class MorningService implements GreetingService {
    @Override
    public void hello() {
        System.out.println("Good morning!");
    }
}
