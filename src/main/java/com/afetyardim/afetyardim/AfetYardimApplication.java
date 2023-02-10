package com.afetyardim.afetyardim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = {"com.afetyardim.afetyardim"})
@EnableScheduling
public class AfetYardimApplication {

  public static void main(String[] args) {
    SpringApplication.run(AfetYardimApplication.class, args);
  }

}
