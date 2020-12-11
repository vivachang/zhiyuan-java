package com.zy.iot.datahandle.sevice.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zy.iot.config.FileDir;
import com.zy.iot.datahandle.sevice.FileService;
import com.zy.iot.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;

/**
 * 与PHP通信文件实现类
 */
@Component
public class FileServiceImpl implements FileService {

    @Autowired
    private FileDir fileDir;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 硬件异常数据及丢失数据写入
     * @param fileJson
     */
    @Override
    public void yingjianbreakdown(JSONObject fileJson) {
        // 创建文件写入buffer
        BufferedWriter out = null;
        try{
            File file = new File(fileDir.yingjianbreakdown +"/" + DateUtils.format(DateUtils.FORMAT_YYYYMMDDHHMM,new Date()).substring(0,11) + "000.txt");
            //判断文件是否存在，若不存在则新建
            if(!file.exists())
            {
                file.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            // 将文件追加到txt文件末尾并换行
            out.write(fileJson+"\r\n");
            out.close();
        } catch (IOException e) {
            logger.error("项目运行报错：" + e.getMessage());
        }
    }

    /**
     * 10分钟平均数据写入
     * @param fileJson
     */
    @Override
    public void yingjian(JSONArray fileJson) {
        // 创建文件写入buffer
        BufferedWriter out = null;
        try{
            File file = new File(fileDir.yingjian +"/" + DateUtils.format(DateUtils.FORMAT_YYYYMMDDHHMM,new Date()).substring(0,11) + "000.txt");
            //判断文件是否存在，若不存在则新建
            if(!file.exists())
            {
                file.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            // 将文件追加到txt文件末尾并换行
            out.write(fileJson+"\r\n");
            out.close();
        } catch (IOException e) {
            logger.error("项目运行报错：" + e.getMessage());
        }
    }
}
