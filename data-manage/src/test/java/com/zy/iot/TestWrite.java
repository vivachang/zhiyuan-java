package com.zy.iot;

import com.aliyun.hitsdb.client.TSDB;
import com.aliyun.hitsdb.client.TSDBClientFactory;
import com.aliyun.hitsdb.client.TSDBConfig;
import com.aliyun.hitsdb.client.value.request.MultiFieldPoint;
import com.aliyun.hitsdb.client.value.request.Point;
import com.aliyun.hitsdb.client.value.response.batch.DetailsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWrite {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 创建 TSDB 对象
        TSDBConfig config = TSDBConfig.address("ts-wz9q4b40u613itu9c.hitsdb.rds.aliyuncs.com", 3242).config();
        TSDB tsdb = TSDBClientFactory.connect(config);
        // 构造数据并写入 TSDB
        Map<String,Object> fields = new HashMap<>();
        fields.put("ceshi","123");
        for (int i = 0; i < 5; i++) {
            MultiFieldPoint point = MultiFieldPoint.metric("airOriginal_v2").timestamp(System.currentTimeMillis()).fields(fields).build();
            //Point point = Point.metric("test").tag("V", "1.0").value(System.currentTimeMillis(), 123.4567).build();
            List<MultiFieldPoint> list = new ArrayList<>();
            list.add(point);
            Thread.sleep(1000);  // 1秒提交1次
            //tsdb.put(point);
            DetailsResult detailsResult = tsdb.multiFieldPutSync(list, DetailsResult.class);
        }
        // 安全关闭客户端，以防数据丢失。
        System.out.println("关闭");
        tsdb.close();
    }
}
