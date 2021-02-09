package com.zy.iot.datahandle.controller;

import com.zy.iot.datahandle.sevice.impl.TsdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * @author AnGuangYing
 * @since 2019-11-03 22:43
 */
@RestController
public class DataCleanController {
    @Autowired
    TsdbService tsdbService;

    @RequestMapping("/data/ten/in")
    public String testData(@RequestBody List<Map<String,Object>> data){
        for(Map<String,Object> map : data){
            tsdbService.airDataRepair2TsdbSync(map);
        }
        return  "OK";
    }
}
