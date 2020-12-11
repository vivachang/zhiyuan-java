package com.forest.dataacquisitionserver.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Scope("prototype")
public class HealthCheckController {
    @RequestMapping(method = RequestMethod.GET, value = "/check")
    public String check() throws Exception {
        return "ok";
    }
}
