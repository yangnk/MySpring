package com.yangnk.mySpringMVC.frameWork.mvc.v3;

import lombok.Data;

import java.util.Map;

@Data
public class MyModelAndView {
    private String viewName;
    private Map<String,?> model;

    public MyModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public MyModelAndView(String viewName) {
        this.viewName = viewName;
    }
}
