package com.yangnk.mySpringMVC.demo.controller;

import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestMapping;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestParam;
import com.yangnk.mySpringMVC.frameWork.mvc.v3.MyModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@MyController
@MyRequestMapping("/")
public class PageController {

    @MyRequestMapping("/first.html")
    public MyModelAndView query(@MyRequestParam("name") String name){
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("name", name);
        model.put("data", new Date().toString());
        return new MyModelAndView("first.html",model);
    }
}
