package predictor.demo.AppModules.calendar;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.http.HttpRequestInitializer;

@Configuration
public class CalendarConfig {
    
    @Bean
    public Calendar googleCalendarClient(OAuth2AuthorizedClientService clientService) {
        try {
            return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                createRequestInitializer(clientService))
                .setApplicationName("Period & Ovulation Predictor")
                .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create Calendar service", e);
        }
    }
    
    private HttpRequestInitializer createRequestInitializer(OAuth2AuthorizedClientService clientService) {
        return request -> {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null) {
                OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName());
                
                if (client != null && client.getAccessToken() != null) {
                    request.getHeaders().setAuthorization("Bearer " + client.getAccessToken().getTokenValue());
                }
            }
        };
    }
}