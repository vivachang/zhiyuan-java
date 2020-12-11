package com.zy.iot.datahandle.model;

import com.alibaba.fastjson.JSONObject;
import com.zy.iot.utils.DateUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 将收到的json数据对象化
 */
public class AirData implements Serializable {

    private String avgId;
    private String deviceId;
    private Long timestamp;
    private Map<String,AirIndex> airIndexMap = new HashMap<>();

    public AirData(){

    }

    /**
     * json数据对象化
     * @param json
     */
    public AirData(JSONObject json){
       try{
           this.setDeviceId(json.getString("clientId"));
           this.setTimestamp(json.getLong("timestamp"));
           Date date = new Date(timestamp+10*60*1000);
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
           this.setAvgId(hour+min);
           JSONObject obj = json.getJSONObject("air_info");
           if (obj.isEmpty()){
               return;
           }
           Iterator<Map.Entry<String, Object>> iterator = obj.entrySet().iterator();

           while(iterator.hasNext()){
               Map.Entry<String, Object> map = iterator.next();
               String k = map.getKey();
               Object o = map.getValue();
               if (o!=null&& !"65535".equals(o.toString())){
                   BigDecimal v = BigDecimal.valueOf(Double.valueOf(map.getValue().toString()));
                   AirIndex airIndex = new AirIndex();
                   airIndex.setName(k);
                   airIndex.setNum(BigDecimal.ONE);
                   airIndex.setSum(v);
                   airIndexMap.put(k,airIndex);
               }
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public  String toJsonString(){
        return  JSONObject.toJSONString(this);
    }

    public String getAvgId() {
        return avgId;
    }

    public void setAvgId(String avgId) {
        this.avgId = avgId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, AirIndex> getAirIndexMap() {
        return airIndexMap;
    }

    public void setAirIndexMap(Map<String, AirIndex> airIndexMap) {
        this.airIndexMap = airIndexMap;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
