package com.pch.exception;

import java.util.NoSuchElementException;

public class NoSuchBeanException extends NoSuchElementException {

    public NoSuchBeanException(String s) {
        super(s);
    }
}
