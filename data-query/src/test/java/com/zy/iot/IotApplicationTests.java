package com.zy.iot;

import com.zy.iot.utils.DateUtils;
import org.assertj.core.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class IotApplicationTests {

    public static String getInitialTime(int interval, Date date){
        //String timestr = DateUtils.format("yyyy-MM-dd HH-mm-ss",date);
        String timestr = "2020-09-14 19-01-00";
        String minute = timestr.split("-")[3];
        String initMinute = minute+"";
        int m = Integer.parseInt(minute);
        if(m < 10){
            initMinute = "00";
        }else if (10 <= m && m < 20){
            initMinute = "10";
        }else if (20 <= m && m < 30){
            initMinute = "20";
        }else if (30 <= m && m < 40){
            initMinute = "30";
        }else if (40 <= m && m < 50){
            initMinute = "40";
        }else {
            initMinute = "50";
        }
        String nextTime = getTimeByMinute(interval,date);
        if(initMinute.length()<2){
            return nextTime+":0"+initMinute+":00";
        }
        return nextTime+":"+initMinute+":00";
    }

    /**
     * 获取指定N分钟前/后的时间(精确到小时)
     * @param minute
     * @return
     */
    public static String getTimeByMinute(int minute,Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minute);
        return new SimpleDateFormat("yyyy-MM-dd HH").format(calendar.getTime());
    }

    public static void main(String[] args) {
        Date date = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2019,0,1,00,00,01);
//        Date date = calendar.getTime();
        System.out.println(getInitialTime(10, date));
    }


    }
