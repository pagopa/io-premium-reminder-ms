package it.gov.pagopa.reminder.util;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import it.gov.pagopa.reminder.dto.request.NotificationDTO;

public class RestTemplateUtils {
	
	public static void sendNotification(String url, NotificationDTO notification) {
		RestTemplate restTemplate = new RestTemplate();
//		restTemplate.setErrorHandler(new RestTemplateExceptionHandler());
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    List<MediaType> acceptedTypes = new ArrayList<MediaType>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    String jsonInString = new Gson().toJson(notification).toString();
	    HttpEntity<String> request = new HttpEntity<String>(jsonInString, requestHeaders);
		restTemplate.postForObject(url, request, Object.class);
	}
}
