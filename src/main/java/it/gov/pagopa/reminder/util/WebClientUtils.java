package it.gov.pagopa.reminder.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;

import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
public class WebClientUtils {

	private WebClientUtils() {}
	
	public static void sendPostRequest(String uri, MediaType contentType, MediaType acceptableDataType, NotificationDTO notification) {
		Mono<String> responseA =  WebClient.create().post()
	    .uri(uri)
	    .contentType(contentType)
	    .accept(acceptableDataType)
	    .bodyValue(new Gson().toJson(notification))
	    .retrieve()
	    .onStatus(HttpStatus::isError, response -> {
	    	log.error("Request error");
           return Mono.error(new Exception("ERROR in sending notification"));
        })
        .bodyToMono(String.class);
		log.info(responseA.toString());
	}
}
