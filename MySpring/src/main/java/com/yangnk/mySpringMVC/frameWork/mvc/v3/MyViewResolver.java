package com.yangnk.mySpringMVC.frameWork.mvc.v3;

import java.io.File;

public class MyViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File tempateRootDir;
    private String templateRoorFile;

    public MyViewResolver(String templateRoot, File file) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        tempateRootDir = new File(templateRootPath);
        templateRoorFile = file.toString();
    }
    public MyView resolveViewName(String viewName){
        if(null == viewName || "".equals(viewName.trim())){
            return null;
        }

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);

        String requestViewName = (tempateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/");
        if (!requestViewName.equals(templateRoorFile)) {
            return null;
        }

        File templateFile = new File(requestViewName);
        return new MyView(templateFile);
    }
}
