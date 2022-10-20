package com.yangnk.mySpringMVC;

import com.yangnk.mySpringMVC.annotation.MyAutowired;
import com.yangnk.mySpringMVC.annotation.MyController;
import com.yangnk.mySpringMVC.annotation.MyRequestMapping;
import com.yangnk.mySpringMVC.annotation.MyRequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@MyController
@MyRequestMapping("/demo")
public class DemoAction {

//  	@MyAutowired
//	private IDemoService demoService;

	@MyRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @MyRequestParam("name") String name){
//		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(name);
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

}
