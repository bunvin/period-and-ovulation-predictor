package predictor.demo.AppModules.calendar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserCalendarClientFactory {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public Calendar buildForUser(String refreshToken) throws GeneralSecurityException, IOException {
        String accessToken = refreshAccessToken(refreshToken);
        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
            .setApplicationName("Period & Ovulation Predictor")
            .build();
    }

    private String refreshAccessToken(String refreshToken) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        var response = new RestTemplate().postForEntity(
            "https://oauth2.googleapis.com/token",
            new HttpEntity<>(body, headers),
            Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Failed to refresh Google access token");
        }

        String accessToken = (String) response.getBody().get("access_token");
        if (accessToken == null) {
            throw new IOException("No access_token in Google token response");
        }
        return accessToken;
    }
}
