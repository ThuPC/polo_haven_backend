package com.datn.backend.configuration;
import com.datn.backend.contants.RoutesConstant;
import com.datn.backend.service.auth.CustomOAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.datn.backend.contants.RoutesConstant.PREFIX_API_CUSTOMER;
import static com.datn.backend.contants.RoutesConstant.PREFIX_API_CUSTOMER_PUBLIC;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String signer_key ="dPwfR7cghCw7V6xFm47kJcbftwri5sX2qjpnfHOTC3lyp7D5TsEhRNs40RWXbgiZ";
    // lỗi nên để tạm signerkey ở đây
    private final String[] PUBLIC_ENDPOINTS = {
            // Endpoints xác thực và OAuth2
            "/login-google",
            "/login-form",
            "/login-form-for-customer",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/auth/login",
            "/api/v1/payment/vnpay-payment-return",
            "/api/v1/payment/vnpay-ipn",

            // Endpoints cho tài nguyên tĩnh và thanh toán
            "/uploads/**",
            "/api/v1/payment/**",
            "/api/auth/login",
            "/api/chat/**",
            "/ws/**",
            "/topic/**",
            "/queue/**",
            "/app/**",
            PREFIX_API_CUSTOMER +"/online/**",
            PREFIX_API_CUSTOMER_PUBLIC + "/**",

            // Endpoint công khai cho khách hàng (xem sản phẩm, danh mục...)
            RoutesConstant.PREFIX_API_CUSTOMER_PUBLIC + "/**", // Tương đương /api/v1/khach-hang/cong-khai/**
            "/api/v1/khach-hang/online",

            // === DÒNG QUAN TRỌNG NHẤT ===
            // Cho phép truy cập công khai vào ĐÚNG endpoint xác nhận đơn hàng
            "/api/v1/khach-hang/online/confirm"
    };
    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
//    @Autowired
//    CustomJwtDecoder jwtDecoder;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
                        .successHandler(customOAuth2SuccessHandler)
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                        .decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new JwtAuthenticationEntrypoint())
                )

                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(){
        SecretKeySpec secretKeySpec = new SecretKeySpec(signer_key.getBytes(),"HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }



    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }



    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:5174", // Thêm port 5174 cho frontend admin
            "http://localhost:3000",
            "https://dbffa975fd97.ngrok-free.app",
            "null" // Allow file:// protocol for testing
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
