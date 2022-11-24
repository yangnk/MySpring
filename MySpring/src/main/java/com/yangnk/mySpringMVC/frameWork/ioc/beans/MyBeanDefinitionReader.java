package com.yangnk.mySpringMVC.frameWork.ioc.beans;

import com.yangnk.mySpringMVC.frameWork.annotation.MyController;
import com.yangnk.mySpringMVC.frameWork.annotation.MyService;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class MyBeanDefinitionReader {
    Properties propertiesConfig = new Properties();
    List<String> registryBeanClass = new ArrayList<String>();//registryBeanClass保存bean的全限定名称，比如com.xxx.Hello

    public MyBeanDefinitionReader(String configLocation) {
        //加载配置文件
        doLoadConfig(configLocation);
        //扫描classPath目录，将bean放到registryBeanClass中
        doScanner(propertiesConfig.getProperty("scanPackage"));
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        log.info(">>> doScanner url is :{} >>>", url);
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                registryBeanClass.add(clazzName);
                log.info("==={} add registryBeanClass success. ===", clazzName);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:", ""));
        try {
            propertiesConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<MyBeanDefinition> loadBeanDefinition() {
        //将registryBeanClass转为List<MyBeanDefinition>，需要将String转为Object保存
        List<MyBeanDefinition> result = new ArrayList<MyBeanDefinition>();
        for (String className : registryBeanClass) {
            try {
                //获取Clazz
                Class<?> clazz = Class.forName(className);

                //加了注解@MyController和@MyService 放入到List<MyBeanDefinition> 中
                if (!(clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class))) {
                    continue;
                }

                //组装BeanDefinition
                MyBeanDefinition beanDefinition = new MyBeanDefinition();
                beanDefinition.setSimleBeanName(toLowerFirstCase(clazz.getSimpleName()));
                beanDefinition.setBeanClassName(clazz.getName());
                //放到list返回
                result.add(beanDefinition);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    public Properties getConfig() {
        return this.propertiesConfig;
    }
}
