package com.yangnk.mySpringMVC.frameWork.mvc.v2;

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
import java.util.*;

@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    private static HashMap<String, Object> handlerMapping = new HashMap<String, Object>();
    private static HashMap<String, Object> ioc = new HashMap<String, Object>();
    private List<String> classNamesList = new ArrayList();
    private Properties properties = new Properties();

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
        String relativeUri = uri.replace(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(relativeUri)) {
            response.getWriter().write("404 Not Found.");
            return;
        }
        Method method = (Method) handlerMapping.get(relativeUri);
        Map<String, String[]> parameterMap = request.getParameterMap();
        method.invoke(ioc.get(method.getDeclaringClass().getName()),
                new Object[]{request, response, parameterMap.get("name")[0]});
        log.info("===handlerMapping method is:{},handlerMapping parameterMap is:{}===", method.toString(), parameterMap.entrySet().toArray().toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置项
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //目录扫描
        doScanner(properties.getProperty("scanPackage"));
        //实例化
        doInstance();
        //注入依赖
        doAutowire();
        //springmvc初始化
        doHandlerMapping();
        log.info("===MyDispatcherServlet init success.===");
    }

    private void doHandlerMapping() {
        //判断是否是@MyController，如果是这根据@RequestMapping获取url，将其url和方法映射放入到handlerMapping中
        if (ioc == null || ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }
            String baseUrl = "";

            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();

            }
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url, method);
                log.info("===handlerMapping put url:{}, method:{}===", url, method.toString());
            }
        }
    }

    private void doAutowire() {
        //获取ioc中obj的field，如果有加@Autowired，则将其注入
        if (ioc == null || ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if (beanName == null || "".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                //访问Field
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNamesList == null || classNamesList.size() == 0) {
            return;
        }
        //将classNamesList加了注入注解的注入到ioc容器中
        for (String className : classNamesList) {
            try {
                Class<?> clazz = Class.forName(className);
                //如果是@MyController，将其实例化放到ioc中
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object instance = clazz.newInstance();
//                    String clazzName = lowerFirstName(clazz.getName());
                    ioc.put(clazz.getName(), instance);
                    log.info("===instance class success, class name is:{}===", clazz.getName());
                }

                //如果是@MyService，将其实例化放到ioc中
                else if (clazz.isAnnotationPresent(MyService.class)) {
                    String beanName = clazz.getAnnotation(MyService.class).value();
                    log.info("===MyService annotation beanName is:{}===", beanName);
                    if ("".equals(beanName.trim())) {
                        beanName = lowerFirstName(clazz.getSimpleName());
                    }
                    //获取实例，放到ioc中
                    Object obj = clazz.newInstance();
                    ioc.put(beanName, obj);//ioc的key:beanName,value：object
                    //如果@MyService放在接口上
                    for (Class<?> implClazz : clazz.getInterfaces()) {
                        if (ioc.containsKey(implClazz.getName())) {
                            throw new Exception("the beanName:" + implClazz.getName() + " exist.");
                        }
                        ioc.put(implClazz.getName(), obj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String lowerFirstName(String name) {
        char[] chars = name.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
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
                classNamesList.add(clazzName);
                log.info("==={} put classNamesList success. ===", clazzName);
            }
        }
    }
}
