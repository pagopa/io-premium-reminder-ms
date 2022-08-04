package it.gov.pagopa.reminder.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dto.MessageContentType;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import it.gov.pagopa.reminder.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.repository.ReminderRepository;
import it.gov.pagopa.reminder.restclient.proxy.ApiClient;
import it.gov.pagopa.reminder.restclient.proxy.api.DefaultApi;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ReminderServiceImpl implements ReminderService {

	@Autowired ReminderRepository reminderRepository;
	@Autowired ObjectMapper mapper;
	@Autowired RestTemplate restTemplate;
	@Value("${interval.function}")
	private int intervalFunction;
	@Value("${attempts.max}")
	private int attemptsMax;
	@Value("${health.value}")
	private String health;	
	@Value("${kafka.send}")
	private String producerTopic;
	@Value("${max.read.message.notify}")
	private int maxReadMessageSend;
	@Value("${max.paid.message.notify}")
	private int maxPaidMessageSend;
	@Value("${start.day}")
	private String startDay;
	@Value("${reminder.day}")
	private int reminderDay;
	@Value("${payment.day}")
	private int paymentDay;
	@Value("${paymentupdater.url}")
	private String urlPayment;

	@Value("${payment.request}")
	private String urlProxy;
	@Value("${enable_rest_key}")
	private boolean enableRestKey;
	@Value("${proxy_endpoint_subscription_key}")
	private String proxyEndpointKey ;

	@Value("${test.active}")
	private boolean isTest;
	@Autowired
	ReminderProducer remProd;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplatePayments;

	@Override
	public Reminder findById(String id) {
		return reminderRepository.findById(id).orElse(null);
	}

	@Override
	public void save(Reminder reminder) {
		reminderRepository.save(reminder);
		log.info("Saved message: {}", reminder.getId());
	}


	@Override
	public void updateReminder(String reminderId, boolean isRead, boolean isPaid) {
		Reminder reminderToUpdate = findById(reminderId);
		if(null != reminderToUpdate) {
			reminderToUpdate.setPaidFlag(isPaid);
			reminderToUpdate.setReadFlag(isRead);
			if(isRead) {
				reminderToUpdate.setReadDate(LocalDateTime.now());
			}
			if(isPaid) {
				reminderToUpdate.setPaidDate(LocalDateTime.now());
			}	
			save(reminderToUpdate);
		}
	}

	@Override
	public Reminder getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode) {

		List<Reminder> listReminder = reminderRepository.getPaymentByNoticeNumberAndFiscalCode(noticeNumber, fiscalCode); 

		return listReminder.isEmpty() ? null : listReminder.get(0);
	}


	@Override
	public void getMessageToNotify() {

		LocalDateTime todayTime = LocalDateTime.now(ZonedDateTime.now().getZone());
		LocalDateTime dateTimeRead = isTest ? todayTime.minusMinutes(reminderDay) : todayTime.minusDays(reminderDay);
		LocalDateTime dateTimePayment = isTest ? todayTime.minusMinutes(paymentDay) : todayTime.minusDays(paymentDay);
		LocalDate today = LocalDate.now();
		LocalDate startDateReminder = today.plusDays(Integer.valueOf(startDay));

		List<Reminder> readMessageToNotify = reminderRepository.getReadMessageToNotify(maxReadMessageSend, dateTimeRead);
		log.info("readMessageToNotify: {}", readMessageToNotify.size());

		List<Reminder> paidMessageToNotify = reminderRepository.getPaidMessageToNotify(MessageContentType.PAYMENT.toString(), 
				Integer.valueOf(maxPaidMessageSend), dateTimePayment, startDateReminder);
		log.info("paidMessageToNotify: {}",paidMessageToNotify.size());

		readMessageToNotify.addAll(paidMessageToNotify);
		for (Reminder reminder : readMessageToNotify) {
			try {				
				if (isGeneric(reminder)) {
					sendReminderToProducer(reminder);
				} else {
					sendNotificationWithRetry(reminder);
				}
				reminderRepository.save(reminder);
				log.info("Update reminder with id: {}", reminder.getId());
			} catch (JsonProcessingException e) {
				log.error("Producer error sending notification {} to message-send queue", reminder.getId());
				log.error(e.getMessage());
			}
			catch (HttpServerErrorException e) {
				log.error("HttpServerErrorException for reminder with id {}, {}", reminder.getId());
				log.error(e.getMessage());
			}
		}
	}


	@Override
	public void deleteMessage() {

		int readMessage = reminderRepository.deleteReadMessage(maxReadMessageSend, MessageContentType.PAYMENT.toString());
		log.info("Delete: {} readMessage", readMessage);

		int paidMessage = reminderRepository.deletePaidMessage(maxPaidMessageSend, MessageContentType.PAYMENT.toString());
		log.info("Delete: {} paidMessage", paidMessage);
	}


	@Override
	public String healthCheck() {
		return health;
	}


	private String callPaymentCheck(Reminder reminder){

		Map<String, Boolean> map;
		map = callProxyCheck(reminder.getRptId());

		if (map.get("isPaid")) {
			reminder.setPaidFlag(true);
			reminder.setPaidDate(LocalDateTime.now());					
		} else {	
			try {
				sendReminderToProducer(reminder);
			} catch (JsonProcessingException e) {
				log.error("Producer error sending notification {} to message-send queue", reminder.getId());
				log.error(e.getMessage());
			}
		}	

		return "";
	}

	private Map<String, Boolean> callProxyCheck(String rptId){

		Map<String, Boolean> map = new HashMap<>();
		map.put("isPaid", false);
		try {
			
			ApiClient apiClient = new ApiClient();
			if (enableRestKey) {
				apiClient.setApiKey(proxyEndpointKey);
			}
			apiClient.setBasePath(urlProxy);
			
			DefaultApi defaultApi = new DefaultApi();
			defaultApi.setApiClient(apiClient);		
			defaultApi.getPaymentInfo(rptId, Constants.X_CLIENT_ID);
			
			
			return map;

		} catch (HttpServerErrorException errorException) {

			ProxyPaymentResponse res;
			try {
				res = mapper.readValue(errorException.getResponseBodyAsString(), ProxyPaymentResponse.class);
				if (res.getDetail_v2().equals("PPT_RPT_DUPLICATA") && errorException.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
					Reminder rem = reminderRepository.getPaymentByRptId(rptId);				
					if (Objects.nonNull(rem)) {					
						map.put("isPaid", true);
					}
				} else {
					throw errorException;
				}
			} catch (JsonMappingException e) {
				log.error(e.getMessage());
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			return map;	
		}
	}

	private void sendNotificationWithRetry(Reminder reminder) {
		IntervalFunction intervalFn = IntervalFunction.of(intervalFunction);
		RetryConfig retryConfig = RetryConfig.custom()
				.maxAttempts(attemptsMax)
				.intervalFunction(intervalFn)
				.build();
		Retry retry = Retry.of("sendNotificationWithRetry", retryConfig);
		Function<Object, Object> sendNotificationFn = Retry.decorateFunction(retry, 
				notObj -> callPaymentCheck((Reminder)notObj));
		sendNotificationFn.apply(reminder);
	}


	private boolean isGeneric(Reminder reminder) {
		return MessageContentType.GENERIC.toString().equalsIgnoreCase(reminder.getContent_type().toString());
	}

	private boolean isPayment(Reminder reminder) {
		return MessageContentType.PAYMENT.toString().equalsIgnoreCase(reminder.getContent_type().toString());
	}

	private void sendReminderToProducer(Reminder reminder) throws JsonProcessingException {
		kafkaTemplatePayments = (KafkaTemplate<String, String>) ApplicationContextProvider.getBean("kafkaTemplatePayments");
		remProd.sendReminder(reminder, kafkaTemplatePayments, mapper, producerTopic);	
		if(!reminder.isReadFlag()) {
			int countRead = reminder.getMaxReadMessageSend()+1;
			reminder.setMaxReadMessageSend(countRead);
		}
		if(reminder.isReadFlag() && !reminder.isPaidFlag() && isPayment(reminder)) {
			int countPaid = reminder.getMaxPaidMessageSend()+1;
			reminder.setMaxPaidMessageSend(countPaid);
		}
		reminder.setLastDateReminder(LocalDateTime.now());
		List<LocalDateTime> listDate = reminder.getDateReminder();
		listDate = Optional.ofNullable(listDate).orElseGet(ArrayList::new);
		listDate.add(LocalDateTime.now());
		reminder.setDateReminder(listDate);
	}

}
