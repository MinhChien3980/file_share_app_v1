package com.fileshareappv1.myapp.config;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class WebConfigFile implements WebMvcConfigurer {

    private final Path rootLocation;

    public WebConfigFile(StorageProperties storageProperties) {
        // normalize & resolve to absolute path so we don’t get any surprises
        this.rootLocation = storageProperties.getLocation().toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // anything under /uploads/** will be served from your FS folder
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + rootLocation + "/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
