package predictor.demo;

import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class AppConfiguration {
    @Bean
    public ModelMapper modelmapper(){
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http
    //             .authorizeHttpRequests(auth -> auth
    //                     .anyRequest().authenticated())
    //             .oauth2Login(oauth2 -> oauth2
    //                     .defaultSuccessUrl("/", true)
    //                     .failureUrl("/login?error=true"))
    //             .csrf(Customizer.withDefaults());
    //     return http.build();
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/static/**", "/*.js", "/*.json", "/*.ico").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("http://localhost:3000", true)  // Redirect to React app
                .failureUrl("/login?error=true"))
            .cors(Customizer.withDefaults())  // Enable CORS
            .csrf(csrf -> csrf.disable());  // Disable CSRF for API calls
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
