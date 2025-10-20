package com.aid.train.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * SpringDoc(Swagger)을 사용한 OpenAPI 3.0 설정을 담당하는 클래스입니다.
 * <p>
 * API 문서의 기본 정보를 정의하고, JWT Bearer 토큰 인증을 Swagger UI에서 사용할 수 있도록 설정합니다.
 * 운영 환경(prod)에서는 보안을 위해 이 설정이 비활성화됩니다.
 * </p>
 *
 * @author 왕택준
 * @since 1.0.0
 */
@Configuration
@Profile("!prod")  // 'prod' 프로파일이 아닐 때만 이 Bean 설정을 활성화
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * OpenAPI 전역 설정을 정의하는 Bean을 생성합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 문서의 제목, 버전, 설명 등을 담은 Info 객체를 생성합니다.
     *
     * @return Info 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("trAIn Backend API Documentation")
                .version("v1.0.0")
                .description("trAIn 프로젝트의 백엔드 API 명세서입니다.");
    }

    /**
     * JWT Bearer 토큰 인증을 위한 SecurityScheme 객체를 생성합니다.
     *
     * @return SecurityScheme 객체
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }
}