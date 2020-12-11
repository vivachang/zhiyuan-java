package com.zy.iot;

import com.aliyun.hitsdb.client.TSDB;
import com.aliyun.hitsdb.client.TSDBClientFactory;
import com.aliyun.hitsdb.client.TSDBConfig;
import com.aliyun.hitsdb.client.value.request.MultiFieldPoint;
import com.aliyun.hitsdb.client.value.response.batch.DetailsResult;
import com.zy.iot.dto.ExcelDataVO;
import com.zy.iot.utils.DateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWrite {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 设定Excel文件所在路径
        String excelFileName = "F:\\至源\\写入数据格式.xlsx";
        // 读取Excel文件内容
        List<ExcelDataVO> readResult = ExcelReader.readExcel(excelFileName);
        // 创建 TSDB 对象
        TSDBConfig config = TSDBConfig.address("ts-wz9q4b40u613itu9c.hitsdb.rds.aliyuncs.com", 3242).config();
        TSDB tsdb = TSDBClientFactory.connect(config);
        for(ExcelDataVO data : readResult){
            // 构造数据并写入 TSDB
            Map<String,Object> fields = new HashMap<>();
            Map<String,String> tags = new HashMap<>();
            tags.put("deviceId",data.getDeviceId());
            tags.put("monitorId",data.getMonitorId());
            tags.put("projectId",data.getProjectId());
            fields.put("humidity",data.getHumidity());
            fields.put("temperature",data.getTemperature());
            fields.put("formaldehyde",data.getFormaldehyde());
            fields.put("CO2",data.getCO2());
            fields.put("PM25",data.getPM25());
            fields.put("TVOC",data.getTVOC());
            fields.put("TVOC2"," ");
            Long timestamp = DateUtils.toDate(DateUtils.FORMAT_BAR_LONG_DATETIME,data.getTimestamp()).getTime();
            MultiFieldPoint  point = MultiFieldPoint.metric("airData_v2").timestamp(timestamp).fields(fields).tags(tags).build();
            List<MultiFieldPoint> list = new ArrayList<>();
            list.add(point);
            Thread.sleep(1000);  // 1秒提交1次
            DetailsResult detailsResult = tsdb.multiFieldPutSync(list, DetailsResult.class);
        }
        // 安全关闭客户端，以防数据丢失。
        System.out.println("关闭");
        tsdb.close();
        Long end = System.currentTimeMillis();
        long hours = end / (1000 * 60 * 60);
        long minutes = (end-hours*(1000 * 60 * 60 ))/(1000* 60);
        System.out.println(minutes);
    }
}
