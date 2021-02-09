package com.zy.iot.datahandle.sevice.impl;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.cache.RedisCache;
import com.zy.iot.config.Constant;
import com.zy.iot.datahandle.model.AirData;
import com.zy.iot.datahandle.model.AirIndex;
import com.zy.iot.datahandle.sevice.FileService;
import com.zy.iot.datahandle.sevice.ICalcData;
import com.zy.iot.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据计算及处理
 */
@Component
public class CalcData implements ICalcData {

    @Autowired
    RedisCache redisCache;
    @Autowired
    Constant constant;
    @Autowired
    TsdbService tsdbService;
    @Autowired
    private FileService fileService;

    private Map<String,String> devIdxs = new HashMap<>();

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 初始化固定值
     */
    private void devIdxsInit(){
        devIdxs.put("formaldehyde","{\"min\":\"0\",\"max\":\"3\"}"); //甲醛
        devIdxs.put("TVOC","{\"min\":\"0\",\"max\":\"10\"}");
        devIdxs.put("PM2.5","{\"min\":\"0\",\"max\":\"1000\"}");
        devIdxs.put("CO2","{\"min\":\"0\",\"max\":\"15000\"}");
        devIdxs.put("temperature","{\"min\":\"-50\",\"max\":\"500\"}"); // 温度
        devIdxs.put("humidity","{\"min\":\"0\",\"max\":\"1000\"}"); // 湿度
    }

    /**
     * 数据进行验证和存储
     * @param obj
     */
    @Async
    public void caclData(JSONObject obj){
        // 设备状态为禁用直接丢弃数据
//        String status = redisCache.getHashMapValue(constant.AUTH_CLIENT_REDIS_PREFIX,obj.getString("clientId"));
//        if("0".equals(status)){
//            logger.debug("硬件没有上线，丢弃数据：" + obj);
//            return;
//        }
        // json数据转成bean
        AirData airData = new AirData(obj);
        String avgId = airData.getAvgId();
        String deviceId = airData.getDeviceId();
        // 上一次接收数据的时间和本次数据时间间隔不能小于55秒
        String lastTimestamp = redisCache.getHashMapValue("zhiyuan_database_air:devices:lasttimestamp",deviceId);
        if(!StringUtils.isEmpty(lastTimestamp) && (airData.getTimestamp() - Long.parseLong(lastTimestamp)) < 59000){
            logger.info("{}一分钟内丢弃重复数据{}",deviceId,airData.getTimestamp());
            return;
        }else{
            logger.info("{}保存正常数据{}",deviceId,airData.getTimestamp());
            redisCache.setHashMapfiled("zhiyuan_database_air:devices:lasttimestamp", deviceId, airData.getTimestamp().toString());
        }
        // 检查数据
        if (checkAbnormal(airData)){
            return;
        }
        // 保存数据
        saveAirOriginalData(airData);

        // 确定十分钟内同一设备唯一id
        String filed = avgId.toString()+"_"+deviceId.toString();
        String data = redisCache.getHashMapValue(constant.redis_air_data_avg,filed);
        //不为空则进行数据计算，为空则将数据放入缓存
        if (!StringUtils.isEmpty(data)){
           AirData avgData = JSONObject.parseObject(data,AirData.class);
           AirData newAvg = plusDataAvg(avgData,airData);
           HashMap<String,String>  mapAvg = new HashMap<>();
           mapAvg.put(filed,newAvg.toJsonString());
           redisCache.setHashMap(constant.redis_air_data_avg,mapAvg);
        }else{
           HashMap<String,String>  mapAvg = new HashMap<>();
           mapAvg.put(filed,airData.toJsonString());
           redisCache.setHashMap(constant.redis_air_data_avg,mapAvg);
           HashMap<String,String>  mapRec = new HashMap<>();
           mapRec.put(filed,System.currentTimeMillis()+"");
           redisCache.setHashMap(constant.redis_air_data_record,mapRec);
        }
        // 确定第一次接收数据的时间
        String timestamp = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TIMESTAMP,deviceId);
        if(StringUtils.isEmpty(timestamp)){
            redisCache.setHashMapfiled(constant.REDIS_AIR_DEVICES_TIMESTAMP, deviceId, airData.getTimestamp().toString());
        }
    }

    @Override
    public void repairCaclData(JSONObject obj) {
        // json数据转成bean
        AirData airData = new AirData(obj);
        // 此处不去匹配设备是否启用
//        String status = redisCache.getHashMapValue(constant.AUTH_CLIENT_REDIS_PREFIX,airData.getDeviceId());
//        if("0".equals(status)){
//            return;
//        }
        // 保存数据
        saveRepairAirOriginalData(airData);
        String avgId = airData.getAvgId();
        String deviceId = airData.getDeviceId();
        String filed = avgId.toString()+"_"+deviceId.toString();
        String data = redisCache.getHashMapValue(constant.redis_air_data_repair_avg,filed);
        //不为空则进行数据计算，为空则将数据放入缓存
        if (!StringUtils.isEmpty(data)){
            AirData avgData = JSONObject.parseObject(data,AirData.class);
            AirData newAvg = plusRepairData(avgData,airData);
            HashMap<String,String>  mapAvg = new HashMap<>();
            mapAvg.put(filed,newAvg.toJsonString());
            redisCache.setHashMap(constant.redis_air_data_repair_avg,mapAvg);
        }else{
            HashMap<String,String>  mapAvg = new HashMap<>();
            mapAvg.put(filed,airData.toJsonString());
            redisCache.setHashMap(constant.redis_air_data_repair_avg,mapAvg);
            HashMap<String,String>  mapRec = new HashMap<>();
            mapRec.put(filed,System.currentTimeMillis()+"");
            redisCache.setHashMap(constant.redis_air_data_repair_record,mapRec);
        }
    }

    /**
     * 保存数据到tsdb
     * @param airData
     */
    private void saveAirOriginalData(AirData airData){
        // 根据硬件id从缓存中加载该硬件对应的监测点和项目
        String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,airData.getDeviceId());
        if (null==tagsVal){
            logger.debug("硬件对应项目检测点为空，数据无法保存，直接丢弃。");
            return;
        }
        JSONObject obj = JSONObject.parseObject(tagsVal);
        obj.put("type","receive");
        obj.put("status","normal");
        String pro = redisCache.getHashMapValue(constant.redis_air_data_prothreshold,obj.getString("projectId"));
        JSONObject prothreshold = null;
        if(pro != null){
            JSONObject proObj = JSONObject.parseObject(pro);
            prothreshold = proObj.getJSONObject("thresholdinfo");
            if(prothreshold == null){
                prothreshold = new JSONObject();
            }
            prothreshold.put("updateTime",proObj.getString("updateTime"));
        }
        logger.debug("取得硬件对应阈值数据并加载进数据");
        tsdbService.airOriginal2TsdbSync(airData,obj.toJavaObject(Map.class),prothreshold);
    }

    /**
     * 保存离线数据到tsdb
     * @param airData
     */
    private void saveRepairAirOriginalData(AirData airData){
        // 根据硬件id从缓存中加载该硬件对应的监测点和项目
        String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,airData.getDeviceId());
        if (null==tagsVal){
            return;
        }
        JSONObject obj = JSONObject.parseObject(tagsVal);
        obj.put("type","receive");
        obj.put("status","normal");

        tsdbService.airRepairOriginal2TsdbSync(airData,obj.toJavaObject(Map.class));
    }

    /**
     * 检查数据是否异常（异常标准为超出最高值10倍或异常值）
     * @param airData
     * @return
     */
    private  Boolean checkAbnormal(AirData airData){
        Long mTime =  3600*24*7*1000L - Math.abs(System.currentTimeMillis()- airData.getTimestamp());
        if (mTime<0){
            logger.debug("数据时间非法，数据直接扔掉：" + airData.getDeviceId());
            return true;
        }
        if(devIdxs.size() == 0){
            devIdxsInit();
        }
        //检查数据
        for(Map.Entry<String, AirIndex> ent:airData.getAirIndexMap().entrySet()){
            String key = ent.getValue().getName();
            BigDecimal val = new BigDecimal("0");
            if("CO2".equals(key)){
                val = ent.getValue().getAvg(2);
            }else if("temperature".equals(key)){
                val = ent.getValue().getAvg(1);
            }else if("humidity".equals(key)){
                val = ent.getValue().getAvg(0);
            }else{
                val = ent.getValue().getAvg(3);
            }
            JSONObject  obj = JSONObject.parseObject(devIdxs.get(key));
            //未知指标
            if (obj==null){
                break;
            }
            BigDecimal min = obj.getBigDecimal("min");
            BigDecimal max = obj.getBigDecimal("max");
            if (val.compareTo(min)== -1 || val.compareTo(max)== 1){
                //这里把数据写入txt
                String tagsVal = redisCache.getHashMapValue(constant.REDIS_AIR_DEVICES_TAGS,airData.getDeviceId());
                if (null==tagsVal){
                    logger.debug("数据写入文件出错，没有从redis去的硬件监测点项目关系");
                    continue;
                }
                // 写入异常日志
                JSONObject object = JSONObject.parseObject(tagsVal);
                object.remove("status");
                object.remove("updateTime");
                object.put("timestamp", DateUtils.format(DateUtils.FORMAT_BAR_LONG_DATETIME,new Date()));
                object.put("event","2");
                redisCache.pushList("javasay",object);
                fileService.yingjianbreakdown(object);
                break;
            }
        }
        return  false;
    }


    /**
     * 将从redis去处的数据和新加入的数据进行计算
     * 计算累加值和累计接收数据次数，用于计算平均值和检测数据是否丢失
     * @param avg
     * @param lastData
     * @return
     */
    private AirData plusDataAvg(AirData avg,AirData lastData){
        for(Map.Entry<String,AirIndex> ent : lastData.getAirIndexMap().entrySet()){
            String k = ent.getKey();
            AirIndex lastVal = ent.getValue();
            AirIndex avgVal = avg.getAirIndexMap().get(k);
            if (avgVal ==null){
                avg.getAirIndexMap().put(k,lastVal);
            }else {
                avgVal.setSum(avgVal.getSum().add(lastVal.getSum()));
                avgVal.setNum(avgVal.getNum().add(lastVal.getNum()));
            }
        }
        return avg;
    }

    /**
     * 将从redis去处的数据和新加入的数据进行计算
     * 计算累加值和累计接收数据次数，用于计算平均值和检测数据是否丢失
     * @param avg
     * @param lastData
     * @return
     */
    private AirData plusRepairData(AirData avg,AirData lastData){
        for(Map.Entry<String,AirIndex> ent : lastData.getAirIndexMap().entrySet()){
            String k = ent.getKey();
            AirIndex lastVal = ent.getValue();
            AirIndex avgVal = avg.getAirIndexMap().get(k);
            if (avgVal ==null){
                avg.getAirIndexMap().put(k,lastVal);
            }else {
                avgVal.setSum(avgVal.getSum().add(lastVal.getSum()));
                avgVal.setNum(avgVal.getNum().add(lastVal.getNum()));
            }
        }

        avg.setNum(avg.getNum() == null ? 0 :avg.getNum() + 1);
        return avg;
    }
}
