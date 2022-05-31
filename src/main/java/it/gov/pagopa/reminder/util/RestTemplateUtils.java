package it.gov.pagopa.reminder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestTemplateUtils {
	
	private RestTemplateUtils() {}
	
	public static void sendNotification(String url, NotificationDTO notification) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    List<MediaType> acceptedTypes = new ArrayList<>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    String jsonInString = new Gson().toJson(notification);
	    HttpEntity<String> request = new HttpEntity<>(jsonInString, requestHeaders);
		ResponseEntity<Object> response = restTemplate
				  .exchange(url, HttpMethod.POST, request, Object.class);
		if (Objects.nonNull(notification.getMessage()) && !response.getStatusCode().isError()) {
			log.info("Notification {} sent successfully", notification.getMessage().getId());
		}
	}
}
