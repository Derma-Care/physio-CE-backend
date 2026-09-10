package com.AdminService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String videoPath = Paths.get("videos").toAbsolutePath().toUri().toString();
        String imagePath = Paths.get("images").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/derma-care/media/videos/**")
                .addResourceLocations(videoPath)
                .setCachePeriod(3600)
                .resourceChain(true);

        registry.addResourceHandler("/derma-care/media/images/**")
                .addResourceLocations(imagePath)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
