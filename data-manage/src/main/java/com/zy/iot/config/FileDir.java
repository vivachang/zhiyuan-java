package com.zy.iot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileDir {
    @Value("${file.average}")
    public String yingjianOriginal;
    @Value("${file.break-down}")
    public String yingjianbreakdownOriginal;

    public String yingjian = "";
    public String yingjianbreakdown = "";

}
