package it.gov.pagopa.microservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.microservice.api.PingApi;

@RestController
public class PingController implements PingApi {

    @Override
    public ResponseEntity<String> sendPingRequest() {
        return ResponseEntity.ok("PONG");
    }

}
