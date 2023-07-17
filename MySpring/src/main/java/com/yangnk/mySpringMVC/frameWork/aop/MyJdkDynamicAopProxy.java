package com.yangnk.mySpringMVC.frameWork.aop;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@Slf4j
public class MyJdkDynamicAopProxy implements InvocationHandler {
    private MyAdvicedSupport advicedSupport;

    public MyJdkDynamicAopProxy(MyAdvicedSupport advicedSupport) {
        this.advicedSupport = advicedSupport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("=== invoke proxy:{}, method:{}, args:{}===", proxy.toString(), method.toString(), args.toString());
        Map<String,MyAdvice> advices = advicedSupport.getAdvices(method,null);//advices

        Object returnValue;
        try {
            invokeAdivce(advices.get("before"));
            returnValue = method.invoke(this.advicedSupport.getTarget(),args);
            invokeAdivce(advices.get("after"));
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }

    private void invokeAdivce(MyAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.advicedSupport.getTargetClass().getInterfaces(), this);
    }
}
