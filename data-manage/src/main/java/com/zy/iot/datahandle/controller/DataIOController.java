package com.zy.iot.datahandle.controller;

import com.zy.iot.datahandle.sevice.impl.SchedulerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AnGuangYing
 * @since 2019-11-03 22:43
 */
@RestController
public class DataIOController {

    @Autowired
    SchedulerTask schedulerTask;

    @RequestMapping("/dataio/V1/data2DbTest")
    public String data2DbTest(){
        schedulerTask.airData2DbTask();
        return  "OK";
    }
}
