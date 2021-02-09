package com.zy.iot.datahandle.dao;

import com.alibaba.fastjson.JSON;
import com.zy.iot.datahandle.model.AirQueryData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyu
 * @since 2020-08-15 15:56
 */
@Service
@RequiredArgsConstructor
public class TsdbCon {
    @Value("${aliyun.tsdb.tsql.host}")
    private String tsqlHost;
    @Value("${aliyun.tsdb.tsql.port}")
    private int tsqlport;

    private Connection connection = null;

    /**
     * 获取tsdb连接
     * @return
     */
    private Connection getTsdbCon() {
        try {
            if (connection==null || connection.isValid(0) || connection.isClosed()){
                Class.forName("org.apache.drill.jdbc.Driver");
                String jdbcUrl = String.format("jdbc:drill:drillbit=%s:%s", tsqlHost, tsqlport);
                System.out.println("Connecting to database @ " + jdbcUrl + "  ...");
                connection = DriverManager.getConnection(jdbcUrl);
            }
        }catch (Exception e){
           e.printStackTrace();
        }
        return connection;
    }

    /**
     * 查询原始数据，本应该是泛型，由于此处数据统一则先写定返回对象
     * @param sql
     * @return
     */
    public List<AirQueryData> querySql(String sql) {
        List<AirQueryData> resList = new ArrayList<>();
        Statement stmt = null;
        try{
            stmt = getTsdbCon().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                AirQueryData airQueryData = new AirQueryData();
                airQueryData.setDeviceId(rs.getString(1));
                airQueryData.setMonitorId(rs.getString(2));
                airQueryData.setProjectId(rs.getString(3));
                airQueryData.setTimestamp(rs.getString(4));
                airQueryData.setHumidity(rs.getString(5));
                airQueryData.setTemperature(rs.getString(6));
                airQueryData.setFormaldehyde(rs.getString(7));
                airQueryData.setCO2(rs.getString(8));
                airQueryData.setPM25(rs.getString(9));
                airQueryData.setTVOC(rs.getString(10));
                airQueryData.setRed(JSON.parseArray(rs.getString(11)));
                resList.add(airQueryData);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException se){
            }
        }
        return resList;
    }

    /**
     * 查询平均数据，本应该是泛型，由于此处数据统一则先写定返回对象
     * @param sql
     * @return
     */
    public List<AirQueryData> queryAvgSql(String sql) {
        List<AirQueryData> resList = new ArrayList<>();
        Statement stmt = null;
        try{
            stmt = getTsdbCon().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                AirQueryData airQueryData = new AirQueryData();
                airQueryData.setDeviceId(rs.getString(1));
                airQueryData.setMonitorId(rs.getString(2));
                airQueryData.setProjectId(rs.getString(3));
                airQueryData.setTimestamp(rs.getString(4));
                airQueryData.setHumidity(rs.getString(5));
                airQueryData.setTemperature(rs.getString(6));
                airQueryData.setFormaldehyde(rs.getString(7));
                airQueryData.setCO2(rs.getString(8));
                airQueryData.setPM25(rs.getString(9));
                airQueryData.setTVOC(rs.getString(10));
                airQueryData.setStatus(rs.getString(11));
                airQueryData.setStageId(rs.getString(12));
                resList.add(airQueryData);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException se){
            }
        }
        return resList;
    }

    /**
     * 查询数据，本应该是泛型，由于此处数据统一则先写定返回对象
     * @param sql
     * @return
     */
    public Map<String,List<AirQueryData>> queryKVSql(String sql) {
        Map<String,List<AirQueryData>> map = new HashMap<>();
        Statement stmt = null;
        try{
            stmt = getTsdbCon().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                AirQueryData airQueryData = new AirQueryData();
                airQueryData.setDeviceId(rs.getString(1));
                airQueryData.setMonitorId(rs.getString(2));
                airQueryData.setProjectId(rs.getString(3));
                airQueryData.setTimestamp(rs.getString(4));
                airQueryData.setHumidity(rs.getString(5));
                airQueryData.setTemperature(rs.getString(6));
                airQueryData.setFormaldehyde(rs.getString(7));
                airQueryData.setCO2(rs.getString(8));
                airQueryData.setPM25(rs.getString(9));
                airQueryData.setTVOC(rs.getString(10));
                if(map.containsKey(airQueryData.getMonitorId())){
                    map.get(airQueryData.getMonitorId()).add(airQueryData);
                }else{
                    List<AirQueryData> resList = new ArrayList<>();
                    resList.add(airQueryData);
                    map.put(airQueryData.getMonitorId(),resList);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException se){
            }
        }
        return map;
    }

    public Integer querySqlCount(String sql) {
        int count = 0;
        Statement stmt = null;
        try{
            stmt = getTsdbCon().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                count =  rs.getInt(1);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException se){
            }
        }
        return count;
    }
}
