package com.yangnk.mySpringMVC.frameWork.aop;

import lombok.Data;

//切面的配置信息，配置信息保存在application.properties中
@Data
public class MyAopConfig {
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
