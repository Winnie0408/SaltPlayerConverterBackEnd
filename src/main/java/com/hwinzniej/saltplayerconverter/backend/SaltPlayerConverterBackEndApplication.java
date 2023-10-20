package com.hwinzniej.saltplayerconverter.backend;

import com.hwinzniej.saltplayerconverter.backend.utils.Cron;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SaltPlayerConverterBackEndApplication {

    public static void main(String[] args) {
        Cron.startJob();
        SpringApplication.run(SaltPlayerConverterBackEndApplication.class, args);
    }

}
