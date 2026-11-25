package com.example.sneakershop.security;

import com.example.sneakershop.security.jwt.JwtAuthFilter;
import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    @Value("${checkout.frontend-url}")
    private String checkoutFrontendUrl;
    @Value("${sneakershop.local-demo.enabled}")
    private boolean localDemo;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username -> {
      User user = userRepository.findByEmail(username); // username is a String
      if (user == null) {
        throw new UsernameNotFoundException("User not found");
      }
      return user; // User implements UserDetails
    };
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder).and().build();
  }

  @Bean
  @ConditionalOnProperty(name = {"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET"})
  ClientRegistrationRepository googleClientRegistration(
          @Value("${GOOGLE_CLIENT_ID}") String clientId,
          @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
          @Value("${sneakershop.google.redirect-uri}") String redirectUri) {
    return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE
            .getBuilder("google").clientId(clientId).clientSecret(clientSecret).redirectUri(redirectUri).build());
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
          ObjectProvider<ClientRegistrationRepository> oauthClients, GoogleSignIn google) throws Exception {

    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> {
                auth
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/catalogs/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/checkout/sessions", "/api/v1/checkout/webhook").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/checkout/sessions/{sessionId}").permitAll();
                if (!localDemo) {
                    auth.requestMatchers("/api/v1/products/**", "/api/v1/catalogs/**", "/api/v1/users/**",
                            "/api/v1/orders/**", "/api/v1/basket/**").hasRole("ADMIN");
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

    ClientRegistrationRepository clients = oauthClients.getIfAvailable();
    if (clients != null) {
        http.oauth2Login(oauth2 -> oauth2.clientRegistrationRepository(clients)
                .authorizationEndpoint(endpoint -> endpoint
                        .authorizationRequestRepository(new GoogleAuthorizationRequests()))
                .successHandler(google::success)
                .failureHandler((request, response, error) -> google.failure(response, "google_sign_in_failed")));
    }
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    String checkoutOrigin = checkoutFrontendUrl.replaceFirst("/+$", "");
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    if (!"http://localhost:3000".equals(checkoutOrigin)) {
        configuration.addAllowedOrigin(checkoutOrigin);
    }
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration checkoutCors = new CorsConfiguration(configuration);
    checkoutCors.setAllowedOrigins(List.of(checkoutOrigin));
    checkoutCors.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    checkoutCors.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Checkout-Guest-Token"));
    checkoutCors.setAllowCredentials(false);
    source.registerCorsConfiguration("/api/v1/checkout/sessions", checkoutCors);
    source.registerCorsConfiguration("/api/v1/checkout/sessions/{sessionId}", checkoutCors);
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }
}
