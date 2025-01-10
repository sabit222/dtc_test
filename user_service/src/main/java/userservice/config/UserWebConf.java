package userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserWebConf implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Разрешить все пути
                .allowedOrigins("http://localhost:3000") // Разрешить только с этого домена
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");

    }
}
