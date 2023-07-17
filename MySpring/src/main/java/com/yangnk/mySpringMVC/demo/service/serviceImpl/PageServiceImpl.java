package com.yangnk.mySpringMVC.demo.service.serviceImpl;

import com.yangnk.mySpringMVC.frameWork.annotation.MyService;
import com.yangnk.mySpringMVC.demo.service.PageService;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;

@MyService
@Slf4j
public class PageServiceImpl implements PageService {
    @Override
    public String query(String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
        log.info("这是在业务方法中打印的：" + json);
        return json;
    }
}
