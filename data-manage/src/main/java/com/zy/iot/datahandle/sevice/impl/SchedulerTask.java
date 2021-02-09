package com.zy.iot.datahandle.sevice.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zy.iot.cache.RedisCache;
import com.zy.iot.config.Constant;
import com.zy.iot.config.FileDir;
import com.zy.iot.datahandle.model.*;
import com.zy.iot.datahandle.sevice.FileService;
import com.zy.iot.utils.DateUtils;
import oadd.com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * 计算平均值定时任务
 */
@Service
public class SchedulerTask {

    @Autowired
    RedisCache redisCache;
    @Autowired
    TsdbService tsdbService;
    @Autowired
    Constant constant;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileDir fileDir;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 数据从redis 中读取10分钟平均值记录
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void airData2DbTask(){
        long ts = System.currentTimeMillis();
        logger.info("开始计算移动平均值");
        // 创建保存数据的目录
        Date date = new Date();
        fileDir.yingjian = fileDir.yingjianOriginal + DateUtils.format(DateUtils.FORMAT_YYYYMMDD,date) + "/";
        fileDir.yingjianbreakdown = fileDir.yingjianbreakdownOriginal + DateUtils.format(DateUtils.FORMAT_YYYYMMDD,date) + "/";
        File f1 = new File(fileDir.yingjian);
        if(!f1.exists()){
            f1.mkdirs();
        }
        File f2 = new File(fileDir.yingjianbreakdown);
        if(!f2.exists()){
            f2.mkdirs();
        }
        // 从缓存中取得缓存的数据
        Map<String,String> records = redisCache.getHashMap(constant.redis_air_data_record);
        JSONArray dataArray = new JSONArray();
        Set<String> isSet = new HashSet<>();
        for(Map.Entry<String,String> ent : records.entrySet()){
            try{
                String filed = ent.getKey();

                String data = redisCache.getHashMapValue(constant.redis_air_data_avg,filed);
                AirData airData = JSONObject.parseObject(data,AirData.class);
                String lt = redisCache.getHashMapValue(constant.REDIS_AIR_UPDATA_DEVICES,airData.getDeviceId());

                String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,airData.getDeviceId());
                if (null==tagsVal){
                    continue;
                }
                JSONObject obj = JSONObject.parseObject(tagsVal);
                obj.remove("status");
                obj.remove("updateTime");
                String prostatusstage = redisCache.getHashMapValue(constant.redis_air_data_prostatusstage,obj.getString("projectId"));
                JSONObject proObj = null;
                if (prostatusstage != null){
                    proObj = JSONObject.parseObject(prostatusstage);
                }
                // 判断十分钟数据不能重复写入
                if(isSet.contains(filed)){
                    continue;
                }
                tsdbService.airData2TsdbSync(airData,obj.toJavaObject(Map.class),proObj);
                isSet.add(filed);
                //上报数据的设备加入缓存
                long recTs =Long.valueOf(ent.getValue());
                // 超过15分钟的数据删除且跳过
                if (ts-recTs>15*60*1000){
                    redisCache.delHashMapFiled(constant.redis_air_data_record,filed);
                    redisCache.delHashMapFiled(constant.redis_air_data_avg,filed);
                    continue;
                }
                redisCache.setHashMapfiled(constant.REDIS_AIR_UPDATA_DEVICES,airData.getDeviceId(),String.valueOf(ts));
                avgDataAndCheck(airData,dataArray);
            }catch (Exception e){
                logger.error("项目运行报错：" + e.getMessage());
            }
        }
        // 把平均数写入txt
        fileService.yingjian(dataArray);
        //上报数据的设备，超24小时清除缓存
        Map<String,String> devices = redisCache.getHashMap(constant.REDIS_AIR_UPDATA_DEVICES);
        for(Map.Entry<String,String> ent : devices.entrySet()){
            long recTs =Long.valueOf(ent.getValue());
            if (ts-recTs>23*60*60*1000){
                redisCache.delHashMapFiled(constant.REDIS_AIR_UPDATA_DEVICES,ent.getKey());
            }
        }
    }

    /**
     * 数据丢失检查
     * @param airData
     */
    private void avgDataAndCheck(AirData airData, JSONArray array){
        String deviceId =  airData.getDeviceId();
        String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,deviceId);
        if (null==tagsVal){
            return;
        }
        JSONObject avgObject = JSONObject.parseObject(tagsVal);
        avgObject.remove("updateTime");
        avgObject.remove("status");
        BigDecimal num = new BigDecimal (9); //10分钟有10条数据,因为数据时间与时间时间可能不同，所以改为9条
        boolean isLoseData = false; //默认没有丢失数据
        for(Map.Entry<String, AirIndex> ent:airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            if("CO2".equals(airIndex.getName())){
                avgObject.put(airIndex.getName(),airIndex.getAvg(2));
            }else if("temperature".equals(airIndex.getName())){
                avgObject.put(airIndex.getName(),airIndex.getAvg(1));
            }else if("humidity".equals(airIndex.getName())){
                avgObject.put(airIndex.getName(),airIndex.getAvg(0));
            }else{
                avgObject.put(airIndex.getName(),airIndex.getAvg(3));
            }
            // 如果丢失数据则isLoseData为true
            if(num.compareTo(airIndex.getNum()) == 1){
                isLoseData = true;
            }
        }
        String timestamp = DateUtils.format(DateUtils.FORMAT_YYYY_MM_DD_HHMM,new Date()) + ":00.000";
        String timestampf = DateUtils.format(DateUtils.FORMAT_YYYY_MM_DD_HHMM,new Date()) + ":00";
        avgObject.put("timestamp",timestampf);
        array.add(avgObject);
        // 如果丢失数据则写入丢失数据异常文件,如果是第一次计算则不算数据丢失
        long firstTime = Long.parseLong(redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TIMESTAMP,deviceId));
        long tt = System.currentTimeMillis();
        if(isLoseData && (tt - firstTime > 10*60*1000)){
            //这里把数据写入txt
            JSONObject object = JSONObject.parseObject(tagsVal);
            object.remove("status");
            object.remove("updateTime");
            object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
            object.put("event","3");
            redisCache.pushList("javasay",object);
            fileService.yingjianbreakdown(object);
        }
    }

    /**
     * 数据从redis中读取10分钟平均值记录-补充数据
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void airRepairData2DbTask(){
        logger.info("开始计算补充数据移动平均值");
        // 创建保存数据的目录
        Date date = new Date();

        // 从缓存中取得缓存的数据
        Map<String,String> records = redisCache.getHashMap(constant.redis_air_data_repair_record);
        long ts = System.currentTimeMillis();
        JSONArray dataArray = new JSONArray();
        for(Map.Entry<String,String> ent : records.entrySet()){
            try{
                String filed = ent.getKey();
                String data = redisCache.getHashMapValue(constant.redis_air_data_repair_avg,filed);
                AirData airData = JSONObject.parseObject(data,AirData.class);
                String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,airData.getDeviceId());
                if (null==tagsVal){
                    continue;
                }
                JSONObject obj = JSONObject.parseObject(tagsVal);
                obj.remove("status");
                //tsdbService.airRepairData2TsdbSync(airData,obj.toJavaObject(Map.class));
                long recTs =Long.valueOf(ent.getValue());
                // 超过15分钟的数据删除且跳过
                if (ts-recTs>15*60*1000){
                    redisCache.delHashMapFiled(constant.redis_air_data_repair_record,filed);
                    redisCache.delHashMapFiled(constant.redis_air_data_repair_avg,filed);
                    continue;
                }
            }catch (Exception e){
                logger.error("项目运行报错：" + e.getMessage());
            }
        }
    }
}
