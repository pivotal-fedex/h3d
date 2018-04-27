package com.fedex.h3d;

import org.junit.runner.JUnitCore;

public class TestClass {
    public static void main(String[] args) {
        JUnitCore core = new JUnitCore();

        core.run(TestTest.class);

        System.out.println("Hi There!");
    }
}
