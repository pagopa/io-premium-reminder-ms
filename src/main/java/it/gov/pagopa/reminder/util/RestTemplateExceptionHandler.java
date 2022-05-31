package it.gov.pagopa.reminder.util;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestTemplateExceptionHandler implements ResponseErrorHandler {

	@Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
		return (httpResponse.getStatusCode().series() == Series.CLIENT_ERROR 
				|| httpResponse.getStatusCode().series() == Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
        	log.debug("Errore 500");
        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
        	log.debug("Errore 400");
        }
    }
}
