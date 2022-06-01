package it.gov.pagopa.reminder.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.dto.avro.MessageContentType;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.producer.ReminderProducer;
import it.gov.pagopa.reminder.repository.ReminderRepository;
import it.gov.pagopa.reminder.util.ApplicationContextProvider;
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
	private String reminderDay;
	@Value("${payment.day}")
	private String paymentDay;
	@Value("${paymentupdater.url}")
	private String urlPayment;


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
		return reminderRepository.getPaymentByNoticeNumberAndFiscalCode(noticeNumber, fiscalCode);
	}


	@Override
	public void getMessageToNotify() {

		LocalDateTime todayTime = LocalDateTime.now(ZonedDateTime.now().getZone());
		LocalDateTime dateTimePayment = todayTime.minusDays(Integer.valueOf(paymentDay));
		LocalDate today = LocalDate.now();
		LocalDate startDateReminder = today.plusDays(Integer.valueOf(startDay));

		List<Reminder> readMessageToNotify = reminderRepository.getReadMessageToNotify(maxReadMessageSend);
		log.info("readMessageToNotify: {}", readMessageToNotify.size());

		List<Reminder> paidMessageToNotify = reminderRepository.getPaidMessageToNotify(MessageContentType.PAYMENT.toString(), 
				Integer.valueOf(maxPaidMessageSend), dateTimePayment, startDateReminder, today);
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
	
		int paidMessage = reminderRepository.deletePaidMessage(maxPaidMessageSend, LocalDate.now(), MessageContentType.PAYMENT.toString());
		log.info("Delete: {} paidMessage", paidMessage);
	}


	@Override
	public String healthCheck() {
		return health;
	}

	
	private String callPaymentCheck(Reminder reminder) {
		ResponseEntity<MessageStatus> response = restTemplate.getForEntity(urlPayment.concat(reminder.getContent_paymentData_noticeNumber()), MessageStatus.class);
		if (response.getBody() != null && response.getBody().getIsPaid().booleanValue()) {
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
		ReminderProducer remProd = new ReminderProducer();
		kafkaTemplatePayments = (KafkaTemplate<String, String>) ApplicationContextProvider.getBean("KafkaTemplatePayments");
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
