package com.yangnk.mySpringMVC.frameWork.ioc.beans;

public class MyBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrapperClazz;

    public MyBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClazz = wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class<?> getWrapperClazz() {
        return wrapperClazz;
    }

    public void setWrapperClazz(Class<?> wrapperClazz) {
        this.wrapperClazz = wrapperClazz;
    }


}
