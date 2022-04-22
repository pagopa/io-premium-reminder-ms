package it.gov.pagopa.reminder.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;

import it.gov.pagopa.reminder.dto.request.NotificationDTO;
import reactor.core.publisher.Mono;

public class WebClientUtils {

	public static void sendPostRequest(String uri, MediaType contentType, MediaType acceptableDataType, NotificationDTO notification) {
		Mono<String> responseA =  WebClient.create().post()
	    .uri(uri)
	    .contentType(contentType)
	    .accept(acceptableDataType)
	    .bodyValue(new Gson().toJson(notification).toString())
	    .retrieve()
	    .onStatus(HttpStatus::isError, response -> {
	    	System.out.println("Errore nella richietsa");
           return Mono.error(new Exception("ERRORE nell'invio della notifica"));
        })
        .bodyToMono(String.class);
		System.out.println(responseA.toString());
	}
}
