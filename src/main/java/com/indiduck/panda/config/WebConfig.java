package com.indiduck.panda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//C:\Users\recon\Desktop\panda\dev\back\panda-back\f…\admin@gmail.com\e7ec53a84cce4834021ecc41db6f59b9', fileName: 'e7ec53a84cce4834021ecc41db6f59b9.jpg

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
//        registry.addResourceHandler("/images/**").addResourceLocations("file:/c:/resource/");
        registry.addResourceHandler("/upload/**").addResourceLocations("file:/");

    }


}
