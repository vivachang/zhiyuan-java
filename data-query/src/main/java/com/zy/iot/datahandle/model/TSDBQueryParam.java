package com.zy.iot.datahandle.model;

import lombok.Data;

import java.util.Map;

@Data
public class TSDBQueryParam {
    private String metric ;
    private String filterTagK ;
    private Long startTime;
    private Long endTime;
    private Integer offset;
    private Integer limit;
    private Map<String,String> tagKVMap ;
    private Map<String,String> tagInKVMap ;
    private String[] filterVals;
    private Integer step;
    private String sortBy;
    private String sortOrder;
}
