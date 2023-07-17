package com.yangnk.mySpringMVC.MY_TEST;

import java.net.URL;

public class test {
    public static void main(String[] args) {

        try {
            throw new Exception("========");
        } catch (Exception e) {
            e.printStackTrace();

        }
//        test t = new test();
//        Class<? extends test> clazz = t.getClass();
//        System.out.println();

//        test t = new test();
//        t.method();
//        System.out.println();

    }

//    public void method() {
//        URL resource = this.getClass().getClassLoader().getResource("/Users/yangnk/IdeaProjects/MySpringAndORM/MySpring/src/main/resources/application.properties");
//        System.out.println();
//    }
}
