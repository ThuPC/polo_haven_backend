package com.datn.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Biến cấu hình cho ảnh sản phẩm (san_pham)
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Root uploads folder (projectRoot/uploads)
        String uploadsRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().toUri().toString();
        String uploadDir = Paths.get("uploads").toAbsolutePath().toString();

        // Ensure the path ends with a slash for proper directory mapping
        if (!uploadsRoot.endsWith("/")) {
            uploadsRoot += "/";
        }

        // Resolve product upload path from property; support absolute or relative values
        String productPathLocation;
        try {
            java.nio.file.Path p = Paths.get(uploadPath);
            if (!p.isAbsolute()) {
                p = Paths.get(System.getProperty("user.dir")).resolve(p).toAbsolutePath();
            }
            productPathLocation = p.toUri().toString();
        } catch (Exception ex) {
            // Fallback to uploadsRoot if uploadPath invalid
            productPathLocation = uploadsRoot;
        }

        // Register specific handler for product images if upload.path points to a nested folder
        // e.g. /uploads/san_pham/** -> file:.../backend/uploads/san_pham/
        registry.addResourceHandler("/uploads/san_pham/**")
                .addResourceLocations(productPathLocation.endsWith("/") ? productPathLocation : productPathLocation + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));


        // Cấu hình cụ thể cho subfolder nhan_vien
        registry.addResourceHandler("/uploads/nhan_vien/**")
                .addResourceLocations("file:" + uploadDir + "/nhan_vien/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        System.out.println("Resource handlers configured successfully!");
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cấu hình này cho phép frontend (localhost:5173, 5174, và 5175) thực hiện yêu cầu GET
        // tới BẤT KỲ URL nào bắt đầu bằng /uploads/.
        // Điều này bao gồm cả /uploads/image.jpg và /uploads/san_pham/product.png.
        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000") // URL của frontend Vue.js
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true);

    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
