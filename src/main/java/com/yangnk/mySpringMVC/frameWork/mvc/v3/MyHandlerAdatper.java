package com.yangnk.mySpringMVC.frameWork.mvc.v3;

import com.yangnk.mySpringMVC.annotation.MyRequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyHandlerAdatper {
    public MyModelAndView getHandler(HttpServletRequest req, HttpServletResponse resp, MyHandlerMapping handlerMapping) throws InvocationTargetException, IllegalAccessException {
        //根据handlerAdatper获取传入参数和位置-->从req中后去参数key和value-->通过反射调用，将返回值放入resp中
        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        Annotation[] [] pa = handlerMapping.getMethod().getParameterAnnotations();
        //获取参数和位置
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof MyRequestParam){
                    String paramName = ((MyRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        Class<?> [] paramTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramterType = paramTypes[i];
            if(paramterType == HttpServletRequest.class || paramterType == HttpServletResponse.class){
                paramIndexMapping.put(paramterType.getName(),i);
            }
        }

        Map<String,String[]> params = req.getParameterMap();
        Object [] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String,String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");

            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
        if(result == null || result instanceof Void){return null;}

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == MyModelAndView.class;
        if(isModelAndView){
            return (MyModelAndView)result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }else if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }
    }
}
