package com.zy.iot.datahandle.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计算数据用对象
    {
    "name": "PM1.0",
    "sum": 15,
    "num": 23,
    "avg": 0.31,
    }
 */

public class AirIndex implements Serializable {

    private String name;
    private BigDecimal sum;
    private BigDecimal num;
    private BigDecimal avg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    // TODO 修改小数点保留位数
    public BigDecimal getAvg(int scale) {
        return sum.divide(num,scale,BigDecimal.ROUND_HALF_UP);
    }

}
