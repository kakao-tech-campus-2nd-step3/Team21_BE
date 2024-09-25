package com.potatocake.everymoment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potatocake.everymoment.repository.MemberRepository;
import com.potatocake.everymoment.security.MemberAuthenticationService;
import com.potatocake.everymoment.security.filter.JwtFilter;
import com.potatocake.everymoment.security.filter.LoginFilter;
import com.potatocake.everymoment.security.handler.Http401Handler;
import com.potatocake.everymoment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final String filterProcessesUrl = "/api/members/login";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(auth -> auth.disable())
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable());

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/login", "/h2-console/**", "/error").permitAll()
                        .anyRequest().authenticated());

        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(http401Handler()));

        http
                .addFilterBefore(jwtFilter(), LoginFilter.class)
                .addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(provider);
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil);
    }

    @Bean
    public LoginFilter loginFilter() {
        return new LoginFilter(filterProcessesUrl, objectMapper, jwtUtil, memberRepository, authenticationManager());
    }

    @Bean
    public Http401Handler http401Handler() {
        return new Http401Handler(objectMapper);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new MemberAuthenticationService(memberRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
