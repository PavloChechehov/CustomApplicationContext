package com.pch.service;

import com.pch.Bean;

@Bean
public class MidnightService extends EveningService {
    @Override
    public void hello() {
        System.out.println(" - It is very night!");
    }
}
