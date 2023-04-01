package com.yangnk.mySpringMVC.MY_TEST;

public class GenericsTest<T> {
    T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public static void main(String[] args) {
        GenericsTest<String> g1 = new GenericsTest<String>();
        g1.setT("aa");
        System.out.println(g1.getT());

        GenericsTest<Integer> g2 = new GenericsTest();
        g2.setT(1);
        System.out.println(g2.getT());
    }
    //    public static void main(String[] args) {
//        Integer[] integerArr = {1, 2, 3};
//        print(integerArr);
//
//        String[] stringArr = {"aa", "bb", "cc"};
//        print(stringArr);
//    }
//
//    public static  <E extends Object> void print(E[] arr) {
//        for (E ele :
//                arr) {
//            System.out.println(ele);
//        }
//    }
}
