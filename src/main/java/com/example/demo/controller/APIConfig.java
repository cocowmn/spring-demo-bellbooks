package com.example.demo.controller;

import com.example.demo.controller.v1.APIv1;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class APIConfig implements WebMvcConfigurer {

    public static String getV1RoutePrefix() {
        return "/api/v1";
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(getV1RoutePrefix(), HandlerTypePredicate.forAnnotation(APIv1.class));
    }

}