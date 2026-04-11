package tn.esprit.pidev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration for serving static files from htdocs
 * Allows direct HTTP access to uploaded documents and photos
 * 
 * Example URLs:
 * - http://localhost:8081/pidev-uploads/1/document.pdf
 * - http://localhost:8081/pidev-uploads/2/photo.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload.dir:C:/xampp/htdocs/pidev-uploads}")
    private String uploadDir;

    /**
     * Configure resource handlers to serve static files from htdocs directory
     * This allows users to directly access files via HTTP URLs
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from pidev-uploads directory
        // Maps: /pidev-uploads/** -> file:C:/xampp/htdocs/pidev-uploads/
        registry.addResourceHandler("/pidev-uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver());

        // Alternative: You can also serve from root if needed
        // registry.addResourceHandler("/**")
        //        .addResourceLocations("file:" + uploadDir + "/");
    }
}

