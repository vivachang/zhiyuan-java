package com.zy.iot.datahandle.controller;

import com.zy.iot.datahandle.model.AirQueryData;
import com.zy.iot.datahandle.model.TSDBQueryParam;
import com.zy.iot.datahandle.sevice.impl.TsdbService;
import com.zy.iot.utils.DateUtils;
import com.zy.iot.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据查询
 */
@RestController
public class DataController {

    @Autowired
    private TsdbService tsdbService;

    @Value("${aliyun.tsdb.original}")
    private String tbAirOriginal;
    @Value("${aliyun.tsdb.airdata}")
    private String tbAirData;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 查询原始数据
     * @param params
     * @return
     */
    @RequestMapping("/api/airdata/original")
    public HashMap<String, Object> dataOriginalQuery(@RequestParam Map<String, Object> params) throws ParseException {
        logger.debug("请求参数：" +params);
        HashMap<String,Object> returnMap = new HashMap<>();

        Long end = System.currentTimeMillis();
        if (params.get("endTime") !=null){
            end = DateUtils.parse("yyyy-MM-dd HH:mm:ss",params.get("endTime").toString()).getTime();
        }
        Long start = end - 7*24*3600*1000L;
        if(params.get("startTime")!= null){
            start = DateUtils.parse("yyyy-MM-dd HH:mm:ss",params.get("startTime").toString()).getTime();
        }
        TSDBQueryParam tsqp = new TSDBQueryParam();
        if(params.get("type").equals("1")){
            tsqp.setMetric(tbAirOriginal);
        }else{
            tsqp.setMetric(tbAirData);
        }
        tsqp.setStartTime(start);
        tsqp.setEndTime(end);
        Map<String,String> kv = new HashMap<>();
        if (params.get("monitorId")!=null && !"".equals(params.get("monitorId"))){
            kv.put("monitorId",params.get("monitorId").toString());
        }
        if (kv.size()>0){
            tsqp.setTagKVMap(kv);
        }
        tsqp.setSortBy("timestamp");
        tsqp.setSortOrder("desc");
        Integer pageSize = 0;
        Integer currPage = 0;
        if(params.get("pageSize") != null && params.get("page") != null){
            pageSize = Integer.parseInt(params.get("pageSize").toString());
            currPage = Integer.parseInt(params.get("page").toString());
            tsqp.setLimit(pageSize);
            tsqp.setOffset((currPage-1)*pageSize);
        }
        List<AirQueryData> list = new ArrayList<>();
        int total = 0;
        if(params.get("type").equals("1")){
            list = tsdbService.queryOriginData(tsqp);
            if(params.get("pageSize") != null && params.get("page") != null){
                total = tsdbService.queryOriginDataCount(tsqp);
            }
        }else{
            list = tsdbService.queryAvgData(tsqp);
            if(params.get("pageSize") != null && params.get("page") != null){
                total = tsdbService.queryAvgDataCount(tsqp);
            }
        }
        if(params.get("type").equals("3")){ //需要补齐中间差的数据
            Date date = new Date(end);
            String second = DateUtils.format("yyyyMMddHHmmss",date);
            String hour = second.substring(0,10);
            String minute = second.substring(10,12);
            // 以10分钟为一个纬度进行计算，确定redis的key
            int m = Integer.parseInt(minute);
            String min = "";
            if(m < 10){
                min = "00";
            }else if (10 <= m && m < 20){
                min = "10";
            }else if (20 <= m && m < 30){
                min = "20";
            }else if (30 <= m && m < 40){
                min = "30";
            }else if (40 <= m && m < 50){
                min = "40";
            }else {
                min = "50";
            }
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdfo=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            end = sdf.parse(hour+min+"00").getTime();
            List<AirQueryData> returnList = new ArrayList<>();
            String monitorId = params.get("monitorId").toString();
            if(params.get("projectStartTime") != null){
                start = DateUtils.parse("yyyy-MM-dd HH:mm:ss",params.get("projectStartTime").toString()).getTime();
            }
            for(long time = end ; time >= start ; time = time-600000){
                boolean isHave = false;
                for(AirQueryData air : list){
                    if(time == sdfo.parse(air.getTimestamp()).getTime()){
                        returnList.add(air);
                        isHave = true;
                        list.remove(air);
                        break;
                    }
                }
                if(!isHave){
                    AirQueryData newAir = new AirQueryData();
                    newAir.setCO2(null);
                    newAir.setTVOC(null);
                    newAir.setFormaldehyde(null);
                    newAir.setHumidity(null);
                    newAir.setPM25(null);
                    newAir.setTemperature(null);
                    newAir.setDeviceId("0");
                    newAir.setMonitorId(monitorId);
                    newAir.setProjectId("0");
                    newAir.setTimestamp(DateUtils.formatLongTime("yyyy-MM-dd HH:mm:ss",time));
                    returnList.add(newAir);
                }
            }
            list.addAll(returnList);
        }
        PageUtils pageUtils = new PageUtils(list, total, pageSize, currPage);
        returnMap.put("code", 200);
        returnMap.put("msg", "success");
        returnMap.put("body",pageUtils);
        return returnMap;
    }

    /**
     * 查询平均数据
     * @param params
     * @return
     */
    @RequestMapping("/api/airdata/average")
    public HashMap<String, Object> dataAverageQuery(@RequestParam Map<String, Object> params){
        HashMap<String,Object> returnMap = new HashMap<>();

        Long end = System.currentTimeMillis();
        if (params.get("endTime") !=null){
            end = DateUtils.parse("yyyy-MM-dd HH:mm:ss",params.get("endTime").toString()).getTime();
        }
        Long start = end - 7*24*3600*1000L;
        if(params.get("startTime")!= null){
            start = DateUtils.parse("yyyy-MM-dd HH:mm:ss",params.get("startTime").toString()).getTime();
        }
        TSDBQueryParam tsqp = new TSDBQueryParam();
        tsqp.setMetric(tbAirData);
        tsqp.setStartTime(start);
        tsqp.setEndTime(end);
        Map<String,String> inkv = new HashMap<>();
        if (params.get("monitorId")!=null && !"".equals(params.get("monitorId"))){
            inkv.put("monitorId",params.get("monitorId").toString());
        }
        if (inkv.size()>0){
            tsqp.setTagInKVMap(inkv);
        }
        tsqp.setSortBy("timestamp");
        tsqp.setSortOrder("desc");
        Integer pageSize = 0;
        Integer currPage = 0;
        if(params.get("pageSize") != null && params.get("page") != null){
            pageSize = Integer.parseInt(params.get("pageSize").toString());
            currPage = Integer.parseInt(params.get("page").toString());
            tsqp.setLimit(pageSize);
            tsqp.setOffset((currPage-1)*pageSize);
        }


        List<AirQueryData> list = tsdbService.queryAvgData(tsqp);
        int total = 0;
        if(params.get("pageSize") != null && params.get("page") != null){
            total = tsdbService.queryAvgDataCount(tsqp);
        }
        PageUtils pageUtils = new PageUtils(list, total, pageSize, currPage);

        returnMap.put("code", 200);
        returnMap.put("msg", "success");
        returnMap.put("body",pageUtils);
        return returnMap;
    }
}
