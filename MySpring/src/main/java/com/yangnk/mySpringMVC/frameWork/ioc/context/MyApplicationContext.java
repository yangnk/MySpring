package com.yangnk.mySpringMVC.frameWork.ioc.context;

import com.yangnk.mySpringMVC.frameWork.annotation.MyAutowired;
import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyService;
import com.yangnk.mySpringMVC.frameWork.aop.MyAdvicedSupport;
import com.yangnk.mySpringMVC.frameWork.aop.MyAopConfig;
import com.yangnk.mySpringMVC.frameWork.aop.MyJdkDynamicAopProxy;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanDefinition;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanDefinitionReader;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanWrapper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Getter
@Setter
public class MyApplicationContext {
    private MyBeanDefinitionReader beanDefinitionReader;
    private List<MyBeanDefinition> beanDefinitionList;
    private Map<String, MyBeanDefinition> beanDefinitionMap = new HashMap<String, MyBeanDefinition>();
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();//map的key值保存类名，例如：Hello，value保存类实例
    private Map<String,MyBeanWrapper> factoryBeanInstanceCache = new HashMap<String, MyBeanWrapper>();

    public MyApplicationContext(String configLocation) {
        //1.加载配置文件
        beanDefinitionReader = new MyBeanDefinitionReader(configLocation);
        //2.解析配置文件
        beanDefinitionList = beanDefinitionReader.loadBeanDefinition();
        try {
            //3.缓存到beanDefinitionMap中
            doRegistryBeanDefinition(beanDefinitionList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //4.依赖注入
        doAutowired();
    }

    private void doAutowired() {
        //由bean的注册阶段到实例化阶段
        for (MyBeanDefinition entry : beanDefinitionList) {
            getBean(entry.getBeanClassName());
        }
    }

    public Object getBean(String beanName) {
        //1.获取配置信息
        MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //2.实例化
        Object instance = instantiateBean(beanDefinition, beanName);
        //3.实例化后的instance封装成BeanWrapper
        MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);
        //4.beanWrapper保存到IoC中
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        //5.依赖注入
        populateBean(beanWrapper);
        //返回实例
        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(MyBeanWrapper beanWrapper) {
        Class<?> wrapperClazz = beanWrapper.getWrapperClazz();
        Object wrapperInstance = beanWrapper.getWrapperInstance();
        if (!(wrapperClazz.isAnnotationPresent(MyController.class) ||
                wrapperClazz.isAnnotationPresent(MyService.class))) {
            return;
        }
        for (Field field : wrapperClazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(MyAutowired.class)) {
                continue;
            }
            MyAutowired autowired = field.getAnnotation(MyAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            if (!factoryBeanInstanceCache.containsKey(autowiredBeanName)) {
                continue;
            }
            try {
                field.set(wrapperInstance, factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(MyBeanDefinition beanDefinition, String beanName) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            //===aop start===

            //初始化aop配置文件
            MyAdvicedSupport advicedSupport = initAopConfig(beanDefinition);
            advicedSupport.setTargetClass(clazz);
            advicedSupport.setTarget(instance);

            //判断是否需要aop
            if (advicedSupport.pointCutClassMatch()) {
                //通过jdk动态代理生成新实例
                instance = new MyJdkDynamicAopProxy(advicedSupport).getProxy();
            }

            //===aop end===

            factoryBeanObjectCache.put(beanName, instance);
            log.info("===factoryBeanObjectCache put factoryBeanObjectCache:{} ===", beanName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private MyAdvicedSupport initAopConfig(MyBeanDefinition beanDefinition) {
        MyAopConfig aopConfig = new MyAopConfig();
        aopConfig.setPointCut(this.beanDefinitionReader.getConfig().getProperty("pointCut"));
        aopConfig.setAspectAfterThrowingName(this.beanDefinitionReader.getConfig().getProperty("aspectAfterThrowingName"));
        aopConfig.setAspectBefore(this.beanDefinitionReader.getConfig().getProperty("aspectBefore"));
        aopConfig.setAspectAfter(this.beanDefinitionReader.getConfig().getProperty("aspectAfter"));
        aopConfig.setAspectAfterThrowingName(this.beanDefinitionReader.getConfig().getProperty("aspectAfterThrow"));
        aopConfig.setAspectClass(this.beanDefinitionReader.getConfig().getProperty("aspectClass"));

        return new MyAdvicedSupport(aopConfig);
    }

    private void doRegistryBeanDefinition(List<MyBeanDefinition> beanDefinitionList) throws Exception {
        //将beanDefinitionList保存到beanDefinitionMap中保存
        for (MyBeanDefinition beanDefinition : beanDefinitionList) {
            if (beanDefinitionMap.containsKey(beanDefinition.getSimleBeanName())) {
                throw new Exception(beanDefinition.getSimleBeanName() + " beanName has exist.");
            }
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }
    public Properties getConfig() {
        return this.beanDefinitionReader.getConfig();
    }
}
