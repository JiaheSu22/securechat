package com.eric.securechat;

import com.eric.securechat.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class

})
public class SecurechatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurechatApplication.class, args);
        System.out.println("***************************SEVER STARTED*****************************");
    }
}
