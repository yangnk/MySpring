package com.yangnk.mySpringMVC.frameWork.aop;

import lombok.Data;

import java.lang.reflect.Method;

//通知的配置信息
@Data
public class MyAdvice {
    private Object aspect;//切面对象
    private Method adviceMethod;//切点方法
    private String throwName;


    public MyAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
