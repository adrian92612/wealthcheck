package com.adrvil.wealthcheck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.adrvil.wealthcheck.mapper")
@EnableCaching
public class WealthcheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(WealthcheckApplication.class, args);
    }

}
