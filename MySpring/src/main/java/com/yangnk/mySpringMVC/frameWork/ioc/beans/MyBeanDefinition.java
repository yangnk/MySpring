package com.yangnk.mySpringMVC.frameWork.ioc.beans;

import lombok.Data;

/**
 * 保存Bean的基本信息，beanClassName是Bean的全限定类名，例如：com.xxx.Hello，simleBeanName是类名，例如：Hello
 */
@Data
public class MyBeanDefinition {
    private String beanClassName;
    private String simleBeanName;
//
//    public String getBeanClassName() {
//        return beanClassName;
//    }
//
//    public void setBeanClassName(String beanClassName) {
//        this.beanClassName = beanClassName;
//    }
//
//    public String getSimleBeanName() {
//        return simleBeanName;
//    }
//
//    public void setSimleBeanName(String simleBeanName) {
//        this.simleBeanName = simleBeanName;
//    }
}
