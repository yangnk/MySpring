package com.yangnk.mySpringMVC.frameWork.ioc.context;

import com.yangnk.mySpringMVC.annotation.MyAutowired;
import com.yangnk.mySpringMVC.annotation.MyController;
import com.yangnk.mySpringMVC.annotation.MyService;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanDefinition;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanDefinitionReader;
import com.yangnk.mySpringMVC.frameWork.ioc.beans.MyBeanWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Data
public class MyApplicationContext {
    private MyBeanDefinitionReader beanDefinitionReader;
    private List<MyBeanDefinition> beanDefinitionList;
    private Map<String, MyBeanDefinition> beanDefinitionMap = new HashMap<String, MyBeanDefinition>();
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();//map的key值保存类名，例如：Hello，value保存类实例
    private Map<String,MyBeanWrapper> factoryBeanInstanceCache = new HashMap<String, MyBeanWrapper>();

    public MyApplicationContext(String configLocation) {
        //加载配置文件
        beanDefinitionReader = new MyBeanDefinitionReader(configLocation);
        //解析配置文件
        beanDefinitionList = beanDefinitionReader.loadBeanDefinition();//todo
        try {
            //缓存到beanDefinitionMap中
            doRegistryBeanDefinition(beanDefinitionList);
            //依赖注入
            doAutowired();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        //由配置阶段到实例化阶段
        for (MyBeanDefinition entry : beanDefinitionList) {
            getBean(entry.getBeanClassName());
        }
    }

    public Object getBean(String beanName) {
        //获取配置信息
        MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //实例化
        Object instance = instantiateBean(beanDefinition, beanName);
        //实例化后的instance封装成BeanWrapper
        MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);
        //beanWrapper保存到IoC中
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        //依赖注入
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
            log.info(">>>111.className:{}>>>",className);
            instance = clazz.newInstance();
            factoryBeanObjectCache.put(beanName, instance);
            log.info("===factoryBeanObjectCache put factoryBeanObjectCache:{} ===", beanName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }
    private void doRegistryBeanDefinition(List<MyBeanDefinition> beanDefinitionList) throws Exception {
        //将beanDefinitionList保存到beanDefinitionMap中保存
        for (MyBeanDefinition beanDefinition : beanDefinitionList) {
            if (beanDefinitionMap.containsKey(beanDefinition.getSimleBeanName())) {
                throw new Exception(beanDefinition.getSimleBeanName() + " beanName has exist.");
            }
//            beanDefinitionMap.put(beanDefinition.getSimleBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }
    public Properties getConfig() {
        return this.beanDefinitionReader.getConfig();
    }
}
