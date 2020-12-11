package com.zy.iot.datahandle.sevice.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.hitsdb.client.TSDB;
import com.aliyun.hitsdb.client.TSDBClientFactory;
import com.aliyun.hitsdb.client.TSDBConfig;
import com.aliyun.hitsdb.client.value.request.MultiFieldPoint;
import com.aliyun.hitsdb.client.value.response.batch.DetailsResult;
import com.zy.iot.datahandle.dao.TsdbCon;
import com.zy.iot.datahandle.model.AirData;
import com.zy.iot.datahandle.model.AirIndex;
import com.zy.iot.datahandle.model.AirQueryData;
import com.zy.iot.datahandle.model.TSDBQueryParam;
import com.zy.iot.datahandle.sevice.ITsdbService;
import com.zy.iot.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

/**
 * @author liyu
 * @since 2020-08-15 15:56
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

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TSDB tsdb;
    @Autowired
    private TsdbCon tsdbCon;

    public void init(){
        TSDBConfig config = TSDBConfig.address(host, port).config();
        tsdb = TSDBClientFactory.connect(config);
    }

    @Override
    public void airOriginal2TsdbSync(AirData airData, Map<String,String> tags){
        Map<String,Object> fields = new HashMap<>();
        for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            fields.put(airIndex.getName(),airIndex.getAvg());
        }
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirOriginal).timestamp(airData.getTimestamp()).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
    }

    @Override
    public void airData2TsdbSync(AirData airData, Map<String,String> tags){
        Date date = DateUtils.toDate("yyyyMMddHHmmss",airData.getAvgId()+"00");
        Long timestamp = date.getTime();
        Map<String,Object> fields = new HashMap<>();
        for(Map.Entry<String,AirIndex> ent: airData.getAirIndexMap().entrySet()){
            AirIndex airIndex = ent.getValue();
            fields.put(airIndex.getName(),airIndex.getAvg());
        }
        MultiFieldPoint  point = MultiFieldPoint.metric(tbAirData).timestamp(timestamp).fields(fields).tags(tags).build();
        // 同步写入
        List<MultiFieldPoint> list = new ArrayList<>();
        list.add(point);
        multiFieldPutSync(list);
    }

    @Override
    public List<AirQueryData> queryOriginData(TSDBQueryParam tsdbQP) {
        String sql = "SELECT deviceId,monitorId,projectId,TO_CHAR(localtime(`timestamp`, '+0800'), 'yyyy-MM-dd HH:mm:ss') as ltime,humidity,temperature,formaldehyde,CO2,PM25,TVOC,red" +
                " FROM `tsdb`.`"+tsdbQP.getMetric()+"` WHERE `timestamp` BETWEEN "+tsdbQP.getStartTime()+" and "+tsdbQP.getEndTime()+" ";

        if(tsdbQP.getTagKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` = '"+ent.getValue()+"'";
            }
        }
        if(tsdbQP.getTagInKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagInKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` in ("+String.join(",",ent.getValue())+")";
            }
        }

        if(tsdbQP.getSortBy()!=null){
            sql = sql + " ORDER BY `"+tsdbQP.getSortBy()+"` "+tsdbQP.getSortOrder();
        }

        if(tsdbQP.getLimit()!=null){
            sql = sql + " LIMIT "+tsdbQP.getLimit()+" offset "+tsdbQP.getOffset()+" ";
        }
        logger.debug("查询sql:" + sql);
        return tsdbCon.querySql(sql);
    }

    @Override
    public Integer queryOriginDataCount(TSDBQueryParam tsdbQP) {
        String sql = "SELECT count(*) as total FROM `tsdb`.`"+tsdbQP.getMetric()+"` WHERE `timestamp` BETWEEN "+tsdbQP.getStartTime()+" and "+tsdbQP.getEndTime()+" ";

        if(tsdbQP.getTagKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` = '"+ent.getValue()+"'";
            }
        }
        return tsdbCon.querySqlCount(sql);
    }

    @Override
    public List<AirQueryData> queryAvgData(TSDBQueryParam tsdbQP) {
        String sql = "SELECT deviceId,monitorId,projectId,TO_CHAR(localtime(`timestamp`, '+0800')," +
                " 'yyyy-MM-dd HH:mm:ss') as ltime,humidity,temperature,formaldehyde,CO2,PM25,TVOC,status,stageId" +
                " FROM `tsdb`.`"+tsdbQP.getMetric()+"` WHERE `timestamp` BETWEEN "+tsdbQP.getStartTime()+" and "+tsdbQP.getEndTime()+" ";

        if(tsdbQP.getTagKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` = '"+ent.getValue()+"'";
            }
        }
        if(tsdbQP.getTagInKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagInKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` in ("+String.join(",",ent.getValue())+")";
            }
        }

        if(tsdbQP.getSortBy()!=null){
            sql = sql + " ORDER BY `"+tsdbQP.getSortBy()+"` "+tsdbQP.getSortOrder();
        }

        if(tsdbQP.getLimit()!=null){
            sql = sql + " LIMIT "+tsdbQP.getLimit()+" offset "+tsdbQP.getOffset()+" ";
        }
        logger.debug("查询sql:" + sql);
        return tsdbCon.queryAvgSql(sql);
    }

    @Override
    public Integer queryAvgDataCount(TSDBQueryParam tsdbQP) {
        String sql = "SELECT count(*) as total FROM `tsdb`.`"+tsdbQP.getMetric()+"` WHERE `timestamp` BETWEEN "+tsdbQP.getStartTime()+" and "+tsdbQP.getEndTime()+" ";

        if(tsdbQP.getTagKVMap()!=null){
            for(Map.Entry<String,String> ent:tsdbQP.getTagKVMap().entrySet()){
                sql = sql +" and `"+ent.getKey()+"` = '"+ent.getValue()+"'";
            }
        }
        return tsdbCon.querySqlCount(sql);
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
        // 同步写入
        //tsdb.multiFieldPutSync(multiValuedPoint);
        // 同步写入并且获取写入成功失败总结
        //SummaryResult summaryResult = tsdb.multiFieldPutSync(multiValuedPoint, SummaryResult.class);
        // System.out.println(summaryResult.toJSON());
        // 同步写入并且获取详细的写入成功或者失败信息
        DetailsResult detailsResult = tsdb.multiFieldPutSync(multiValuedPoint, DetailsResult.class);
        System.out.println(detailsResult.toJSON());
    }

    @PreDestroy
    protected void close() throws IOException {
        tsdb.close();
    }



}
