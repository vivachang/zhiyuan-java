package com.zy.iot.datahandle.controller;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.datahandle.sevice.ICalcData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AnGuangYing
 * @since 2019-11-03 22:43
 */
@RestController
public class DataCleanController {
    @Autowired
    ICalcData calcAvg;

    @RequestMapping("/datacleaning/V1/testData")
    public String testData(@RequestBody String data){
        JSONObject obj = JSONObject.parseObject(data);
        calcAvg.repairCaclData(obj);
        return  "OK";
    }
}
