package com.yangnk.mySpringMVC.frameWork.mvc.v1;

import com.yangnk.mySpringMVC.frameWork.annotation.MyAutowired;
import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestMapping;
import com.yangnk.mySpringMVC.frameWork.annotation.MyService;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    private static HashMap<String, Object> handlerMapping = new HashMap<String, Object>();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doDispatch(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("500 Exception, " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        log.info("===request uri is:{}, request contextPath is:{}===", uri, contextPath);
        String newUri = uri.replace(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(newUri)) {
            response.getWriter().write("404 Not Found.");
            return;
        }
        Method method = (Method) handlerMapping.get(newUri);
        Map<String, String[]> parameterMap = request.getParameterMap();
        method.invoke(handlerMapping.get(method.getDeclaringClass().getName()),
                new Object[]{request, response, parameterMap.get("name")[0]});
        log.info("===handlerMapping method is:{},handlerMapping parameterMap is:{}===", method.toString(), parameterMap.entrySet().toArray().toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        Properties properties = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
        try {
            properties.load(inputStream);
            String scanPackage = properties.getProperty("scanPackage");
            doScanner(scanPackage);

            //handlerMapping 赋值value
            for (String className : handlerMapping.keySet()) {
                if (!className.contains(".")) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    handlerMapping.put(className, clazz.newInstance());
                    String baseUrl = "";

                    if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                        MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                            continue;
                        }

                        MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        handlerMapping.put(url, method);
                        log.info("===handlerMapping url is:{},handlerMapping method is:{}===", url, method.toString());
                    }
                } else if(clazz.isAnnotationPresent(MyService.class)){
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){beanName = clazz.getName();}
                    Object instance = clazz.newInstance();
                    handlerMapping.put(beanName,instance);//mapping保存:key:com.xxx.hello，value：<object>
                    for (Class<?> i : clazz.getInterfaces()) {
                        handlerMapping.put(i.getName(),instance);
                    }
                }
                for (Object object : handlerMapping.values()) {
                    if(object == null){continue;}
                    Class clazz1 = object.getClass();
                    if(clazz1.isAnnotationPresent(MyController.class)){
                        Field[] fields = clazz1.getDeclaredFields();
                        for (Field field : fields) {
                            if(!field.isAnnotationPresent(MyAutowired.class)){continue; }
                            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                            String beanName = autowired.value();
                            if("".equals(beanName)){beanName = field.getType().getName();}
                            field.setAccessible(true);
                            try {
                                field.set(handlerMapping.get(clazz1.getName()),handlerMapping.get(beanName));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            log.info("===init handlerMapping success.===");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(inputStream != null){
                try {inputStream.close();} catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                handlerMapping.put(clazzName, null);
                log.info("==={} put handlerMapping success. ===", clazzName);
            }
        }
    }

}
