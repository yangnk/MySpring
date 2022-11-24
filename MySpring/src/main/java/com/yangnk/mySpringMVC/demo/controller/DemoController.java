package com.yangnk.mySpringMVC.demo.controller;

import com.yangnk.mySpringMVC.frameWork.annotation.MyAutowired;
import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestMapping;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestParam;
import com.yangnk.mySpringMVC.demo.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@MyController
@MyRequestMapping("/demo")
public class DemoController {

  	@MyAutowired
	private DemoService demoService;

	@MyRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @MyRequestParam("name") String name){
		String result = demoService.get(name);
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MyRequestParam("a") Double a, @MyRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping("/remove")
	public String  remove(@MyRequestParam("id") Integer id){
		return "" + id;
	}

	public void sayHello() {
		System.out.println("hello");

	}

}
