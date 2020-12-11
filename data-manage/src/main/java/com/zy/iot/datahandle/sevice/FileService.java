package com.zy.iot.datahandle.sevice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 与PHP文件接口
 */
public interface FileService {

    /**
     * 硬件异常数据及丢失数据写入
     * @param fileJson
     */
    void yingjianbreakdown(JSONObject fileJson);

    /**
     * 10分钟平均数据写入
     * @param fileJson
     */
    void yingjian(JSONArray fileJson);
}
