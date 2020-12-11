package com.zy.iot.datahandle.sevice;

import com.alibaba.fastjson.JSONObject;

/**
 * @author AnGuangYing
 * @since 2019-11-16 16:43
 */

public interface ICalcData {

    /**
     * 实时数据
     * @param obj
     */
    void caclData(JSONObject obj);

    /**
     * 补充数据
     * @param obj
     */
    void repairCaclData(JSONObject obj);
}
