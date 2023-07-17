package com.yangnk.mySpringMVC.frameWork.aop;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MyAdvicedSupport {
    private MyAopConfig aopConfig;
    private Object target;//目标对象，比如传入的Bean
    private Class targetClass;//目标类Class对象，比如传入的Bean的Class
    private Pattern pointCutClassPattern;//切点的正则表达式
    private Map<Method, Map<String, MyAdvice>> adviceCache = new HashMap();//保存目标类方法和通知的映射关系

    public MyAdvicedSupport(MyAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

//    public Map<String, MyAdvice> getAdvice(Method method) {
//        Map<String, MyAdvice> adviceMap = adviceCache.get(method);
//        //会存在找不到的问题吗？
//
////        if (adviceMap == null) {
////            try {
////                Method method1 = targetClass.getMethod(method.getName(), method.getParameterTypes());
////                Map<String, MyAdvice> myAdviceMap = adviceCache.get(method1);
////                adviceCache.put(method1, myAdviceMap);
////            } catch (NoSuchMethodException e) {
////                e.printStackTrace();
////            }
////        }
//        return adviceMap;
//    }

    //根据一个目标代理类的方法，获得其对应的通知
    public Map<String,MyAdvice> getAdvices(Method method, Object o) throws Exception {
        //享元设计模式的应用
        Map<String,MyAdvice> cache = adviceCache.get(method);
        if(null == cache){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cache = adviceCache.get(m);
            this.adviceCache.put(m,cache);
        }
        return cache;
    }

    //判断是否需要aop，切点和目标类进行匹配
    public boolean pointCutClassMatch() {
//        String s = "class com.yangnk.mySpringMVC.demo.";
//        return pointCutClassPattern.matcher(s).matches();
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    //解析配置文件
    private void initParse() {
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        log.info("=== initParse pointCut is:{} ===", pointCut);

        //保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try{
            Class aspectClass = Class.forName(this.aopConfig.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }

            //轮训目标类中匹配的方法，关联方法和通知的关系，保存到adviceCache中
            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,MyAdvice> advices = new HashMap<String, MyAdvice>();

                    if(!(null == aopConfig.getAspectBefore() || "".equals(aopConfig.getAspectBefore()))){
                        advices.put("before",new MyAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectBefore())));
                    }
                    if(!(null == aopConfig.getAspectAfter() || "".equals(aopConfig.getAspectAfter()))){
                        advices.put("after",new MyAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectAfter())));
                    }
                    if(!(null == aopConfig.getAspectAfterThrow() || "".equals(aopConfig.getAspectAfterThrow()))){
                        MyAdvice advice = new MyAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectAfterThrow()));
                        advice.setThrowName(aopConfig.getAspectAfterThrowingName());
                        advices.put("afterThrow",advice);
                    }
                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
                    adviceCache.put(method, advices);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        initParse();
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public Class getTargetClass() {
        return targetClass;
    }

}
