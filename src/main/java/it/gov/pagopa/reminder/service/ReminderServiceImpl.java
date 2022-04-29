package it.gov.pagopa.reminder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

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

	@Autowired
	ReminderRepository reminderRepository;

	@Value("${reminder.max}")
	private String reminderMax;

	@Value("${reminder.day}")
	private String reminderDay;

	@Value("${health.value}")
	private String health;

	@Override
	public Reminder findById(String id) {
		return reminderRepository.findById(id).orElse(null);
	}

	@Override
	public void save(Reminder reminder) {
		reminderRepository.save(reminder);		
	}


	@Override
	public void updateReminder(String reminderId, boolean isRead, boolean isPaid) {
		Reminder reminderToUpdate = findById(reminderId);
		reminderToUpdate.setPaidFlag(isPaid);
		reminderToUpdate.setReadFlag(isRead);
		save(reminderToUpdate);
	}

	@Override
	public void getRemindersToNotify(){

		log.info("Lettura dei reminder");	
		
		int reminderNumMax = Integer.valueOf(reminderMax);
		int reminderDayNum = Integer.valueOf(reminderDay);
		LocalDateTime dateTime = LocalDateTime.now().minusDays(reminderDayNum);

		List<Reminder> remindersToNotify = reminderRepository.findRemindersToNotify(false, true, reminderNumMax, dateTime, MessageContentType.PAYMENT.toString());
		List<Reminder> paymentsToNotify = reminderRepository.findPaymentsToNotify(false, dateTime, MessageContentType.PAYMENT.toString(), LocalDateTime.now());
		remindersToNotify.addAll(paymentsToNotify);
		ReminderProducer producer = (ReminderProducer) ApplicationContextProvider.getBean("getReminderProducer");
		
		for (Reminder reminder : remindersToNotify) {
			byte[] byteReminder = new Gson().toJson(reminder).getBytes();
			producer.insertReminder(byteReminder);
			Reminder updateReminder = reminderRepository.findById(reminder.getId()).orElse(new Reminder());
			int count = updateReminder.getNumReminder()+1;
			updateReminder.setNumReminder(count);
			if(count==10) {
				updateReminder.setReminderFlag(false);
			}
			updateReminder.setLastDateReminder(LocalDateTime.now());
			List<LocalDateTime> listDate = updateReminder.getDateReminder();
			listDate = Optional.ofNullable(listDate).orElseGet(ArrayList::new);
			listDate.add(LocalDateTime.now());
			updateReminder.setDateReminder(listDate);
			reminderRepository.save(updateReminder);
		}


	}

	@Override
	public void deleteReminders() {
		log.info("Cancellazione dei reminder");
		reminderRepository.deleteReminders(Integer.valueOf(reminderMax), LocalDateTime.now(), MessageContentType.PAYMENT.toString());
		log.info("Fine cancellazione dei reminder");
	}
	
	@Override
	public String healthCheck() {
		return health;
	}
}
