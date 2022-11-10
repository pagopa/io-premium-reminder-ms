package it.gov.pagopa.reminder.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import it.gov.pagopa.reminder.dto.ProxyResponse;
import it.gov.pagopa.reminder.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.repository.ReminderRepository;
import it.gov.pagopa.reminder.restclient.pagopaproxy.model.PaymentRequestsGetResponse;
import it.gov.pagopa.reminder.restclient.servicemessages.model.NotificationInfo;
import it.gov.pagopa.reminder.restclient.servicemessages.model.NotificationType;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
import it.gov.pagopa.reminder.util.Constants;
import it.gov.pagopa.reminder.util.DateUtils;
import it.gov.pagopa.reminder.util.ReminderUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ReminderServiceImpl implements ReminderService {

	@Autowired
	ReminderRepository reminderRepository;
	@Autowired
	ObjectMapper mapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	it.gov.pagopa.reminder.restclient.pagopaproxy.api.DefaultApi defaultApi;
	@Autowired
	it.gov.pagopa.reminder.restclient.pagopaproxy.ApiClient apiClient;

	@Autowired
	it.gov.pagopa.reminder.restclient.servicemessages.api.DefaultApi defaultServiceMessagesApi;

	@Autowired
	it.gov.pagopa.reminder.restclient.servicemessages.ApiClient serviceMessagesApiClient;

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
	private String proxyEndpointKey;
	@Value("#{'${error_statuscode.values}'.split(',')}")
	private String[] errorStatusCodeValues;

	@Value("${test.active}")
	private boolean isTest;

	@Value("${notification.request}")
	private String serviceMessagesUrl;
	@Value("${notification_endpoint_subscription_key}")
	private String notifyEndpointKey;

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
	public void updateReminder(String reminderId, boolean isRead) {
		Reminder reminderToUpdate = findById(reminderId);
		if (null != reminderToUpdate) {
			reminderToUpdate.setReadFlag(isRead);
			if (isRead) {
				reminderToUpdate.setReadDate(LocalDateTime.now());
			}
			save(reminderToUpdate);
		}
	}

	@Override
	public void getMessageToNotify() {

		LocalDateTime todayTime = LocalDateTime.now(ZonedDateTime.now().getZone());
		LocalDateTime dateTimeRead = isTest ? todayTime.minusMinutes(reminderDay) : todayTime.minusDays(reminderDay);
		LocalDateTime dateTimePayment = isTest ? todayTime.minusMinutes(paymentDay) : todayTime.minusDays(paymentDay);
		LocalDate today = LocalDate.now();
		LocalDate startDateReminder = today.plusDays(Integer.valueOf(startDay));

		List<Reminder> readMessageToNotify = reminderRepository.getReadMessageToNotify(maxReadMessageSend,
				dateTimeRead);
		log.info("readMessageToNotify: {}", readMessageToNotify.size());

		List<Reminder> paidMessageToNotify = reminderRepository.getPaidMessageToNotify(
				MessageContentType.PAYMENT.toString(), Integer.valueOf(maxPaidMessageSend), dateTimePayment,
				startDateReminder);
		log.info("paidMessageToNotify: {}", paidMessageToNotify.size());

		readMessageToNotify.addAll(paidMessageToNotify);

		Map<String, Boolean> rptidMap = new HashMap<>();

		for (Reminder reminder : readMessageToNotify) {
			try {
				if (isGeneric(reminder)) {
					sendReminderToProducer(reminder);
					updateCounter(reminder);
					reminderRepository.save(reminder);
				} else if (!rptidMap.containsKey(reminder.getRptId())) {
					/*
					 * If rptId is not present in rptidMap, we send the notification to the IO
					 * backend. This avoids sending the same message multiple times.
					 */
					sendNotificationWithRetry(reminder);
					rptidMap.put(reminder.getRptId(), true);
				}
				log.info("Update reminder with id: {}", reminder.getId());
			} catch (JsonProcessingException e) {
				log.error("Producer error sending notification {} to message-send queue", reminder.getId());
				log.error(e.getMessage());
			} catch (HttpServerErrorException e) {
				log.error("HttpServerErrorException for reminder with id {}, {}", reminder.getId());
				log.error(e.getMessage());
			}
		}

	}

	@Override
	public void deleteMessage() {

		int readMessage = reminderRepository.deleteReadMessage(maxReadMessageSend,
				MessageContentType.PAYMENT.toString());
		log.info("Delete: {} readMessage", readMessage);

		int paidMessage = reminderRepository.deletePaidMessage(maxPaidMessageSend,
				MessageContentType.PAYMENT.toString(), LocalDate.now());
		log.info("Delete: {} paidMessage", paidMessage);
	}

	@Override
	public String healthCheck() {
		return health;
	}

	private String callPaymentCheck(Reminder reminder) {

		ProxyResponse proxyResp = callProxyCheck(reminder.getRptId());

		LocalDate localDateProxyDueDate = proxyResp.getDueDate();
		LocalDate reminderDueDate = Optional.ofNullable(reminder.getDueDate()).map(dueDate -> dueDate.toLocalDate())
				.orElse(null);
		List<Reminder> reminders = reminderRepository.getPaymentByRptId(reminder.getRptId());

		if (localDateProxyDueDate != null && localDateProxyDueDate.equals(reminderDueDate)) {
			if (proxyResp.isPaid()) {
				for (Reminder rem : reminders) {
					rem.setPaidFlag(true);
					reminderRepository.save(rem);
				}
			} else {
				try {
					Reminder rem = Collections.min(reminders, Comparator.comparing(c -> c.getInsertionDate()));
					sendReminderToProducer(rem);

					for (Reminder reminderToUpdate : reminders) {
						updateCounter(reminderToUpdate);
						reminderRepository.save(reminderToUpdate);
					}
				} catch (JsonProcessingException e) {
					log.error("Producer error sending notification {} to message-send queue", reminder.getId());
					log.error(e.getMessage());
				}
			}
		} else {
			for (Reminder reminderToUpdate : reminders) {
				reminderToUpdate.setDueDate(ReminderUtil.getLocalDateTime(localDateProxyDueDate));
				reminderRepository.save(reminderToUpdate);
			}
		}

		return "";
	}

	public void sendReminderNotification(Reminder reminder) {
		try {

			NotificationInfo notificationInfoBody = new NotificationInfo();
			notificationInfoBody.setFiscalCode(reminder.getFiscalCode());
			notificationInfoBody.setMessageId(reminder.getId());
			NotificationType notificationType = Optional.of(reminder).filter(rem -> isPayment(rem))
					.map(r -> DateUtils.resetLocalDateTimeToSimpleDate(r.getDueDate()))
					.map(dueDate -> dueDate.minusDays(1)
							.isEqual(DateUtils.resetLocalDateTimeToSimpleDate(LocalDateTime.now()))
									? NotificationType.REMINDER_PAYMENT_LAST
									: NotificationType.REMINDER_PAYMENT)
					.orElse(NotificationType.REMINDER_READ);

			notificationInfoBody.setNotificationType(notificationType);

			serviceMessagesApiClient.addDefaultHeader("Ocp-Apim-Subscription-Key", notifyEndpointKey);
			serviceMessagesApiClient.setBasePath(serviceMessagesUrl);

			defaultServiceMessagesApi.setApiClient(serviceMessagesApiClient);
			defaultServiceMessagesApi.notify(notificationInfoBody);

		} catch (HttpServerErrorException errorException) {
			switch (errorException.getStatusCode()) {
				case NOT_FOUND:
					return;
				default:
					throw errorException;
			}
		}
	}

	private ProxyResponse callProxyCheck(String rptId) {

		ProxyResponse proxyResp = new ProxyResponse();
		try {
			if (enableRestKey) {
				apiClient.addDefaultHeader("Ocp-Apim-Subscription-Key", proxyEndpointKey);
			}
			apiClient.setBasePath(urlProxy);

			defaultApi.setApiClient(apiClient);
			PaymentRequestsGetResponse resp = defaultApi.getPaymentInfo(rptId, Constants.X_CLIENT_ID);

			LocalDate dueDate = ReminderUtil.getLocalDateFromString(resp.getDueDate());
			proxyResp.setDueDate(dueDate);

			return proxyResp;

		} catch (HttpServerErrorException errorException) {

			ProxyPaymentResponse res;
			try {
				res = mapper.readValue(errorException.getResponseBodyAsString(), ProxyPaymentResponse.class);

				if (res.getDetail_v2() != null) {
					int code = errorException.getStatusCode().value();

					if ((code == 400 || code == 404 || code == 409)
							&& Arrays.asList(errorStatusCodeValues).contains(res.getDetail_v2())) {

						LocalDate dueDate = ReminderUtil.getLocalDateFromString(res.getDuedate());
						proxyResp.setPaid(true);
						proxyResp.setDueDate(dueDate);

					}

					proxyResp.setPaid(false);

				} else {
					throw errorException;
				}
			} catch (JsonMappingException e) {
				log.error(e.getMessage());
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}

			return proxyResp;
		}
	}

	private void sendNotificationWithRetry(Reminder reminder) {
		IntervalFunction intervalFn = IntervalFunction.of(intervalFunction);
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(attemptsMax).intervalFunction(intervalFn).build();
		Retry retry = Retry.of("sendNotificationWithRetry", retryConfig);
		Function<Object, Object> sendNotificationFn = Retry.decorateFunction(retry,
				notObj -> callPaymentCheck((Reminder) notObj));
		sendNotificationFn.apply(reminder);
	}

	private boolean isGeneric(Reminder reminder) {
		return MessageContentType.GENERIC.toString().equalsIgnoreCase(reminder.getContent_type().toString());
	}

	private boolean isPayment(Reminder reminder) {
		return MessageContentType.PAYMENT.toString().equalsIgnoreCase(reminder.getContent_type().toString());
	}

	private void sendReminderToProducer(Reminder reminder) throws JsonProcessingException {
		kafkaTemplatePayments = (KafkaTemplate<String, String>) ApplicationContextProvider
				.getBean("kafkaTemplatePayments");
		remProd.sendReminder(reminder, kafkaTemplatePayments, mapper, producerTopic);
	}

	private void updateCounter(Reminder reminder) {

		if (!reminder.isReadFlag()) {
			int countRead = reminder.getMaxReadMessageSend() + 1;
			reminder.setMaxReadMessageSend(countRead);
		}
		if (reminder.isReadFlag() && !reminder.isPaidFlag() && isPayment(reminder)) {
			int countPaid = reminder.getMaxPaidMessageSend() + 1;
			reminder.setMaxPaidMessageSend(countPaid);
		}
		reminder.setLastDateReminder(LocalDateTime.now());
		List<LocalDateTime> listDate = reminder.getDateReminder();
		listDate = Optional.ofNullable(listDate).orElseGet(ArrayList::new);
		listDate.add(LocalDateTime.now());
		reminder.setDateReminder(listDate);
	}

	@Override
	public List<Reminder> getPaymentsByRptid(String rptId) {
		List<Reminder> reminders = reminderRepository.getPaymentByRptId(rptId);
		return reminders == null ? new ArrayList<>() : reminders;
	}

	@Override
	public int countById(String id) {
		return reminderRepository.countById(id);
	}

}
