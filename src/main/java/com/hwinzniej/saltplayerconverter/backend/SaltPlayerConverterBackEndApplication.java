package com.hwinzniej.saltplayerconverter.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class SaltPlayerConverterBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaltPlayerConverterBackEndApplication.class, args);
    }

}
