package com.yangnk.mySpringMVC.frameWork.aop;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
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
        log.info(">>> invoke proxy:{}, method:{}, args:{}>>>>", proxy.toString(), method.toString(), args.toString());
        Map<String, MyAdvice> advice = advicedSupport.getAdvice(method);
        Object instance = null;
        try {
            MyAdvice before = advice.get("before");
            before.getAdviceMethod().invoke(before.getAspect());
            instance = method.invoke(this.advicedSupport.getTarget(),args);
            MyAdvice after = advice.get("after");
            after.getAdviceMethod().invoke(after.getAspect());
        } catch (Exception e) {
            MyAdvice afterThrow = advice.get("afterThrow");
            afterThrow.getAdviceMethod().invoke(afterThrow.getAspect(), args);
            e.printStackTrace();
        }
        return instance;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.advicedSupport.getTargetClass().getInterfaces(), this);
    }
}
