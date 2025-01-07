package com.pdfapplication.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwagConfig {

    @Bean
    GroupedOpenApi publicApi(){

       return  GroupedOpenApi.builder()
               .group("public-apis")
               .pathsToMatch("/**")
               .build();

    }

//    @Bean
//    OpenAPI customAPI(){
//        return new OpenAPI()
   // }
//"""
//https://medium.com/@berktorun.dev/swagger-like-a-pro-with-spring-boot-3-and-java-17-49eed0ce1d2f
//"""

}
