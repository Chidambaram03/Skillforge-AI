package com.skillforge.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Serve the `frontend/` directory when running the backend.
     *
     * The working directory can vary depending on how the app is launched (IDE, script, mvnw),
     * so we register both common relative locations:
     * - backend/ as cwd  -> ../frontend/
     * - repo root as cwd -> frontend/
     */
    private static final String[] FRONTEND_LOCATIONS = {
            "file:../frontend/",
            "file:frontend/"
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only map frontend asset paths, let Spring Boot handle everything else (including /h2-console)
        registry.addResourceHandler("/index.html", "/login.html", "/register.html", "/dashboard.html", "/quiz.html", "/diagnostic.html", "/analytics.html",
                                   "/js/**", "/css/**", "/images/**", "/assets/**")
                .addResourceLocations(FRONTEND_LOCATIONS);
    }
}
