package com.zy.iot.datahandle.sevice;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.hitsdb.client.value.response.MultiFieldQueryLastResult;
import com.aliyun.hitsdb.client.value.response.MultiFieldQueryResult;
import com.zy.iot.datahandle.model.AirData;
import com.zy.iot.datahandle.model.AirQueryData;
import com.zy.iot.datahandle.model.TSDBQueryParam;

import java.util.List;
import java.util.Map;

/**
 * tsdb接口类
 */

public interface ITsdbService {

    /**
     * 保存原始数据
     * @param airData
     * @param tags
     */
    void airOriginal2TsdbSync(AirData airData, Map<String,String> tags);

    /**
     * 保存计算数据
     * @param airData
     * @param tags
     */
    void airData2TsdbSync(AirData airData, Map<String,String> tags);

    /**
     * 查询数据
     * @param tsdbQP
     * @return
     */
    List<AirQueryData> queryOriginData(TSDBQueryParam tsdbQP);

    /**
     * 查询数据条数
     * @param tsdbQP
     * @return
     */
    Integer queryOriginDataCount(TSDBQueryParam tsdbQP);

    /**
     * 查询数据
     * @param tsdbQP
     * @return
     */
    List<AirQueryData> queryAvgData(TSDBQueryParam tsdbQP);

    /**
     * 查询数据条数
     * @param tsdbQP
     * @return
     */
    Integer queryAvgDataCount(TSDBQueryParam tsdbQP);

    Boolean deleteData(JSONObject json);

    /**
     * 查询传入监测点的第一条原始数据
     * @param tsdbQP
     * @return
     */
    List<AirQueryData> queryFirstData(TSDBQueryParam tsdbQP);

}
