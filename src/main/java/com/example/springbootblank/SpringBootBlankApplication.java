package com.example.springbootblank;

import com.example.springbootblank.auth.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan({
        "com.example.springbootblank.auth.mapper",
        "com.example.springbootblank.employee.mapper",
        "com.example.springbootblank.dish.mapper",
        "com.example.springbootblank.cart.mapper",
        "com.example.springbootblank.order.mapper",
        "com.example.springbootblank.payment.mapper",
        "com.example.springbootblank.shop.mapper",
        "com.example.springbootblank.category.mapper",
        "com.example.springbootblank.rider.mapper",
        "com.example.springbootblank.log.mapper",
        "com.example.springbootblank.admin.mapper"
})
@EnableConfigurationProperties(JwtProperties.class)
public class SpringBootBlankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBlankApplication.class, args);
    }

}
