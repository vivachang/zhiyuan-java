package com.zy.iot.datahandle.sevice.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.hitsdb.client.TSDB;
import com.aliyun.hitsdb.client.TSDBClientFactory;
import com.aliyun.hitsdb.client.TSDBConfig;
import com.aliyun.hitsdb.client.value.request.MultiFieldPoint;
import com.aliyun.hitsdb.client.value.response.batch.DetailsResult;
import com.zy.iot.datahandle.dao.TsdbCon;
import com.zy.iot.datahandle.model.AirData;
import com.zy.iot.datahandle.model.AirIndex;
import com.zy.iot.datahandle.model.TSDBQueryParam;
import com.zy.iot.datahandle.sevice.ITsdbService;
import com.zy.iot.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author AnGuangYing
 * @since 2019-12-15 15:56
 */
@Service
public class TsdbService implements ITsdbService {

    @Value("${aliyun.tsdb.host}")
    private String host;
    @Value("${aliyun.tsdb.port}")
    private int port;

    @Value("${aliyun.tsdb.original}")
    private String tbAirOriginal;
    @Value("${aliyun.tsdb.airdata}")
    private String tbAirData;

    private TSDB tsdb;
    @Autowired
    private TsdbCon tsdbCon;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init(){
        TSDBConfig config = TSDBConfig.address(host, port).config();
        tsdb = TSDBClientFactory.connect(config);
    }

    @Override
    public void airOriginal2TsdbSync(AirData airData, Map<String,String> tags,JSONObject prothreshold){
        logger.debug("开始组装tsdb保存数据,prothreshold={}",prothreshold);
        try{
            Map<String,Object> fields = new HashMap<>();
            JSONArray red = new JSONArray();
            for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
                AirIndex airIndex = ent.getValue();
                if(prothreshold != null && prothreshold.getString(airIndex.getName()) != null
                        && !prothreshold.getString(airIndex.getName()).equals("~")) {
                    String max = prothreshold.getString(airIndex.getName()).split("~")[1];
                    BigDecimal bmax = new BigDecimal(max);
                    int result = airIndex.getSum().compareTo(bmax);
                    if(result >= 0){ //表示超出了阈值
                        red.add(airIndex.getName());
                    }
                }
                if("CO2".equals(airIndex.getName())){
                    fields.put(airIndex.getName(),airIndex.getAvg(2));
                }else if("temperature".equals(airIndex.getName())){
                    fields.put(airIndex.getName(),airIndex.getAvg(1));
                }else if("humidity".equals(airIndex.getName())){
                    fields.put(airIndex.getName(),airIndex.getAvg(0));
                }else{
                    fields.put(airIndex.getName(),airIndex.getAvg(3));
                }
            }
            logger.debug("写入数据red={}",red.size() != 0?red.toJSONString(): "[]");
            fields.put("red",red.size() != 0?red.toJSONString(): "[]");
            fields.put("updateTime",prothreshold != null ? prothreshold.getString("updateTime"):"not found");
            logger.debug("写入tsdb数据组装完成");
            MultiFieldPoint  point = MultiFieldPoint.metric(tbAirOriginal).timestamp(airData.getTimestamp()).fields(fields).tags(tags).build();
            // 同步写入
            List<MultiFieldPoint> list = new ArrayList<>();
            list.add(point);
            multiFieldPutSync(list);
        }catch (Exception e){
            logger.error("项目运行报错：" + e.getMessage());
        }
    }

    @Override
    public void airRepairOriginal2TsdbSync(AirData airData, Map<String,String> tags){
        Map<String,Object> fields = new HashMap<>();
        JSONArray red = new JSONArray();
        for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            if("CO2".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(2));
            }else if("temperature".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(1));
            }else if("humidity".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(0));
            }else{
                fields.put(airIndex.getName(),airIndex.getAvg(3));
            }
        }
        fields.put("offline","1");
        fields.put("red","[]");
        fields.put("updateTime","/data/prokz/offlinedata");
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirOriginal).timestamp(airData.getTimestamp()).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
    }

    @Override
    public void airData2TsdbSync(AirData airData, Map<String,String> tags,JSONObject stage){
        Date date = DateUtils.toDate("yyyyMMddHHmmss",airData.getAvgId()+"00");
        Long timestamp = date.getTime();
        Map<String,Object> fields = new HashMap<>();
        for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            if("CO2".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(2));
            }else if("temperature".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(1));
            }else if("humidity".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(0));
            }else{
                fields.put(airIndex.getName(),airIndex.getAvg(3));
            }
        }
        if(stage != null){
            fields.put("status",stage.get("status").toString());
            fields.put("stageId",stage.get("stage_id").toString());
        }
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirData).timestamp(timestamp).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
    }

    @Override
    public void airRepairData2TsdbSync(AirData airData, Map<String,String> tags){
        if(airData.getNum() < 10){ //表示之前有数据
            TSDBQueryParam tsqp = new TSDBQueryParam();
            tsqp.setMetric(tbAirOriginal);

            tsqp.setStartTime(airData.getStartTime());
            tsqp.setEndTime(airData.getEndTime());
            Map<String,String> kv = new HashMap<>();
            kv.put("deviceId",airData.getDeviceId());
            if (kv.size()>0){
                tsqp.setTagKVMap(kv);
            }
            JSONArray jsonArray = this.queryData(tsqp);
            AirData airDatar = new AirData();
            if(jsonArray != null && jsonArray.size() > 0){
                airDatar = new AirData(jsonArray.getJSONObject(0));
            }
            for(int i= 1;i<jsonArray.size();i++){
                AirData airDataOld = new AirData(jsonArray.getJSONObject(i));
                this.plusRepairData(airDatar,airDataOld);
            }
            airData = airDatar;
        }
        Date date = DateUtils.toDate("yyyyMMddHHmmss",airData.getAvgId()+"00");
        Long timestamp = date.getTime();
        Map<String,Object> fields = new HashMap<>();
        for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            if("CO2".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(2));
            }else if("temperature".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(1));
            }else if("humidity".equals(airIndex.getName())){
                fields.put(airIndex.getName(),airIndex.getAvg(0));
            }else{
                fields.put(airIndex.getName(),airIndex.getAvg(3));
            }
        }
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirData).timestamp(timestamp).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
    }

    @Override
    public Boolean deleteData(JSONObject json) {
        return null;
    }

    /**
     * 数据写入tsdb
     * @param multiValuedPoint
     */
    private void multiFieldPutSync(List<MultiFieldPoint> multiValuedPoint){
        logger.debug("数据开始写入tsdb");
        // 同步写入
        //tsdb.multiFieldPutSync(multiValuedPoint);
        // 同步写入并且获取写入成功失败总结
        //SummaryResult summaryResult = tsdb.multiFieldPutSync(multiValuedPoint, SummaryResult.class);
        // System.out.println(summaryResult.toJSON());
        // 同步写入并且获取详细的写入成功或者失败信息
        DetailsResult detailsResult = tsdb.multiFieldPutSync(multiValuedPoint, DetailsResult.class);
        logger.debug("数据写入tsdb返回:{}",detailsResult.toJSON());
    }

    @PreDestroy
    protected void close() throws IOException {
        tsdb.close();
    }

    @Override
    public JSONArray queryData(TSDBQueryParam tsdbQP) {
        String sql = "SELECT deviceId,monitorId,projectId,TO_CHAR(localtime(`timestamp`, '+0800'), 'yyyy-MM-dd HH:mm:ss') as ltime,humidity,temperature,formaldehyde,CO2,PM25,TVOC" +
                " FROM `tsdb`.`"+tsdbQP.getMetric()+"` WHERE `timestamp` BETWEEN "+tsdbQP.getStartTime()+" and "+tsdbQP.getEndTime()+" ";

        if(tsdbQP.getTagKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` = '"+ent.getValue()+"'";
            }
        }
        return tsdbCon.querySql(sql);
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
        avg.setNum(avg.getNum() + 1);
        return avg;
    }

    @Override
    public void airDataRepair2TsdbSync(Map<String,Object> map){
        logger.info("快照写入移动平均值开始：{}",map.toString());
        Date date = DateUtils.toDate(DateUtils.FORMAT_BAR_LONG_DATETIME,map.get("timestamp").toString());
        Long timestamp = date.getTime();
        Map<String,Object> fields = new HashMap<>();
        fields.put("TVOC",map.get("TVOC"));
        fields.put("formaldehyde",map.get("formaldehyde"));
        fields.put("PM25",map.get("PM25"));
        fields.put("CO2",map.get("CO2"));
        fields.put("temperature",map.get("temperature"));
        fields.put("humidity",map.get("humidity"));
        fields.put("red",map.get("red"));
        Map<String,String> tags = new HashMap<>();
        tags.put("monitorId",map.get("monitorId").toString());
        tags.put("projectId",map.get("projectId").toString());
        tags.put("deviceId",map.get("deviceId").toString());
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirOriginal).timestamp(timestamp).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
        logger.info("快照写入移动平均值结束");
    }

}
