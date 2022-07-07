package com.pch.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


//https://stackoverflow.com/questions/496928/what-is-the-difference-between-instanceof-and-class-isassignablefrom
public class CompareInstanceOfAndAssignableFrom {

    @Test
    public void isInstanceOf() {
        Exception anEx1 = new Exception("ex");
        Exception anEx2 = new RuntimeException("ex");
        RuntimeException anEx3 = new RuntimeException("ex");

        //Base case, handles inheritance
        assertTrue(anEx1 instanceof Exception);
        assertTrue(anEx2 instanceof Exception);
        assertTrue(anEx3 instanceof Exception);

        //Other cases
        assertFalse(anEx1 instanceof RuntimeException);
        assertTrue(anEx2 instanceof RuntimeException);
        assertTrue(anEx3 instanceof RuntimeException);
    }

    @Test
    public void isAssignableFrom() {
        Exception anEx1 = new Exception("ex");
        Exception anEx2 = new RuntimeException("ex");
        RuntimeException anEx3 = new RuntimeException("ex");

        //Correct usage = The base class goes first
        assertTrue(Exception.class.isAssignableFrom(anEx1.getClass()));
        assertTrue(Exception.class.isAssignableFrom(anEx2.getClass()));
        assertTrue(Exception.class.isAssignableFrom(anEx3.getClass()));

        //Incorrect usage = Method parameter is used in the wrong order
        assertTrue(anEx1.getClass().isAssignableFrom(Exception.class));
        assertFalse(anEx2.getClass().isAssignableFrom(Exception.class));
        assertFalse(anEx3.getClass().isAssignableFrom(Exception.class));
    }
}
