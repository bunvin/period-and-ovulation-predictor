package predictor.demo;

import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import predictor.demo.AppModules.user.User;
import predictor.demo.AppModules.user.UserServiceImp;
import predictor.demo.Error.AppException;

@Configuration
@EnableWebSecurity
public class AppConfiguration {
    @Autowired
    private UserServiceImp userServiceImp;

    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);


    @Bean
    public ModelMapper modelmapper(){
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/index.html", "/static/**", "/*.js", "/*.json", "/*.ico").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("http://localhost:3000", true)  // Redirect to React app
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService())))
            .cors(Customizer.withDefaults())  // Enable CORS
            .csrf(csrf -> csrf.disable());  // Disable CSRF for API calls
        return http.build();
    }


    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    
        return (userRequest) -> {
            // Get Google user info
            OAuth2User oauth2User = delegate.loadUser(userRequest);
    
            // Extract user details
            String googleId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");
    
            logger.info("Processing OAuth2 login for: {}", email);
    
            // Initialize user as null
            User user = null;
    
            try {
                // Try to find existing user
                user = userServiceImp.getUserByGoogleSubject(googleId);
                logger.info("Found existing user: {}", email);
            } catch (AppException ex) {
                logger.info("User not found, will create new user for: {}", email);
                // User not found - continue to creation block
            }
    
            // Create new user if doesn't exist
            if (user == null) {
                try {
                    User newUser = new User.UserBuilder()
                        .googleSubject(googleId)
                        .email(email)
                        .name(name)
                        .picture(picture)
                        .build();
                    
                    user = userServiceImp.createUser(newUser);
                    logger.info("Created new user with Google ID: {}", googleId);
                } catch (AppException e) {
                    logger.error("Error creating user with Google ID {}: {}", googleId, e.getMessage());
                    // Even if user creation fails, we still return oauth2User to allow authentication
                }
            }
    
            // Return the OAuth2User regardless of database operations
            return oauth2User;
        };
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
