package predictor.demo.AppModules.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class CalendarConfig {

    public static Calendar getCalendarService(OAuth2AuthorizedClientService clientService) {
        try {
            HttpRequestInitializer requestInitializer = request -> {
                OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null) {
                    OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                            authentication.getAuthorizedClientRegistrationId(),
                            authentication.getName()
                    );

                    if (client != null) {
                        String accessToken = client.getAccessToken().getTokenValue();
                        request.getHeaders().setAuthorization("Bearer " + accessToken);
                    }
                }
            };

            return new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName("Period & Ovulation Predictor")
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create Calendar service", e);
        }
    }
}