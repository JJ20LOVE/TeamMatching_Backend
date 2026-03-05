package club.boyuan.official.teammatching.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 文档配置 (Spring Boot 3 + springdoc-openapi)
 * 
 * 访问地址：http://localhost:8080/swagger-ui.html
 * 或：http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TeamMatching API")
                .version("1.0.0")
                .description("团队匹配平台 API 接口文档")
                .contact(new Contact()
                    .name("TeamMatch Team")
                    .email("support@teammatching.club"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", createBearerAuthScheme()));
    }
    
    /**
     * 配置 Bearer Token 认证方案 (JWT)
     */
    private SecurityScheme createBearerAuthScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("请输入 JWT Token，格式：Bearer {token}");
    }
}
