package com.pch.exception;

import java.util.NoSuchElementException;

public class NoUniqueBeanException extends NoSuchElementException {

    public NoUniqueBeanException(String s) {
        super(s);
    }
}
