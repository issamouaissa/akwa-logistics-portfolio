package org.sid.camionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@FeignClient
public class CamionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamionServiceApplication.class, args);
    }

}
