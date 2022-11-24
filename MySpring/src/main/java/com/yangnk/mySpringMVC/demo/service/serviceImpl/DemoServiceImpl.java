package com.yangnk.mySpringMVC.demo.service.serviceImpl;

import com.yangnk.mySpringMVC.frameWork.annotation.MyService;
import com.yangnk.mySpringMVC.demo.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import java.text.SimpleDateFormat;
import java.util.Date;

@MyService
@Slf4j
public class DemoServiceImpl implements DemoService {
    @Override
    public String get(String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        String result = "hello " + name + "," + "time is:" + time;
        log.info("DemoService result is：" + result);
        return result;
    }
}
