package com.forest.dataacquisitionserver.controller;

import com.forest.dataacquisitionserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/client")
@Scope("prototype")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @RequestMapping(method = RequestMethod.GET, value = "/kick-local/{clientId}/{sessionId}")
    public String kickLocal(@PathVariable("clientId") String clientId,
                       @PathVariable("sessionId") String sessionId) throws Exception {
        log.info("Kick out local {}#{}", clientId, sessionId);
        clientService.logout(clientId, sessionId);
        return "ok";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/kick/{clientId}")
    public String kickAll(@PathVariable("clientId") String clientId) throws Exception {
        log.info("Kick out all {}", clientId);
        clientService.kick(clientId);
        return "ok";
    }
}