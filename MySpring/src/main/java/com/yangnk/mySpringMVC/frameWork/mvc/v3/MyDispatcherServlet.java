package com.yangnk.mySpringMVC.frameWork.mvc.v3;

import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyRequestMapping;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanDefinition;
import com.yangnk.mySpringMVC.frameWork.ioc.context.MyApplicationContext;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    private MyApplicationContext applicationContext;
    private List<MyHandlerMapping> handlerMappingList = new ArrayList();
    private Map<MyHandlerMapping, MyHandlerAdatper> handlerAdapterMap = new HashMap();
    private List<MyViewResolver> viewResolverList = new ArrayList();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            log.info("=== doPost start===");
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req, resp, new MyModelAndView("500"));
            } catch (Exception exception) {
                exception.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //根据url获取handlerMapping-->根据handlerMapping获取handlerAdapter-->封装成ModelAndView-->到viewResolver中处理
        MyHandlerMapping handlerMapping = getHandler(req);
//        log.info(">>> handlerMapping.getPattern().toString():{}, handlerMapping.getMethod().getName():{}>>>", handlerMapping.getPattern().toString(), handlerMapping.getMethod().getName());
        if (handlerMapping == null) {
            processDispatchResult(req, resp, new MyModelAndView("404"));
            return;
        }

        MyHandlerAdatper handlerAdatper = getHandlerAdapter(handlerMapping);

        MyModelAndView modelAndView = handlerAdatper.getHandler(req, resp, handlerMapping);
        processDispatchResult(req,resp,modelAndView);
        log.info("doDispatch success.");

    }

    private MyHandlerAdatper getHandlerAdapter(MyHandlerMapping handlerMapping) {
        if (this.handlerMappingList.size() == 0) {
            return null;
        }
        return handlerAdapterMap.get(handlerMapping);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView modelAndView) throws Exception {
        if (null == modelAndView || this.viewResolverList.isEmpty()) {
            return;
        }

        for (MyViewResolver viewResolver : this.viewResolverList) {
            MyView view = viewResolver.resolveViewName(modelAndView.getViewName());
            if (view == null) {
                continue;
            }
            //直接往浏览器输出
            view.render(modelAndView.getModel(),req,resp);
            return;
        }
    }


    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappingList.isEmpty()){return  null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
//        url = "////first.html";
        for (MyHandlerMapping mapping : handlerMappingList) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
            return mapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化IoC容器
        applicationContext = new MyApplicationContext(config.getInitParameter("contextConfigLocation"));

        //初始化mvc基础组件
        initStrategies(applicationContext);

        log.info("MyDispatcherServlet init success.");
//        super.init(config);
    }

    private void initStrategies(MyApplicationContext applicationContext) {
        //初始化handlerMapping
        initHandlerMapping(applicationContext);
        //初始化handlerAdapter
        initHandlerAdapter(applicationContext);
        //初始化viewResolver
        initViewResolver(applicationContext);
    }

    //将模板文件放到ViewResolver中
    private void initViewResolver(MyApplicationContext applicationContext) {
        String templateRoot = applicationContext.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolverList.add(new MyViewResolver(templateRoot, file));
        }
    }

    //初始化HandlerAdapter
    private void initHandlerAdapter(MyApplicationContext applicationContext) {
        for (MyHandlerMapping handlerMapping :
                handlerMappingList) {
            handlerAdapterMap.put(handlerMapping, new MyHandlerAdatper());
        }
    }

    private void initHandlerMapping(MyApplicationContext applicationContext) {
        //拼接url，获取method，反射得到clazz
        if (applicationContext.getBeanDefinitionList().size() == 0) {
            return;
        }
        for (MyBeanDefinition beanDefinition : applicationContext.getBeanDefinitionList()) {
            String className = beanDefinition.getBeanClassName();
            Object bean = applicationContext.getBean(className);
            Class<?> clazz = bean.getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //获取public的method
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
//                String regex = "/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*")
//                        .replaceAll("/+", "/");
                String regex = requestMapping.value().replaceAll("\\*", ".*").replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappingList.add(new MyHandlerMapping(pattern, method, bean));
            }
        }
    }
}
