package it.gov.pagopa.reminder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class ReminderServiceImpl implements ReminderService{

	@Autowired
	ReminderRepository reminderRepository;

	@Value("${reminder.max}")
	private String reminderMax;

	@Value("${reminder.day}")
	private String reminderDay;


	@Override
	public Reminder findById(String id) {
		Reminder rr = reminderRepository.findById(id).orElse(null);
		return rr;
	}

	@Override
	public void save(Reminder reminder) {
		reminderRepository.save(reminder);		
	}

	@Override
	public void deleteById(String id, String partitionKey) {
		reminderRepository.deleteById(id);
	}


	@Override
	public void updateReminder(String reminderId, boolean isRead) {
		Reminder reminderToUpdate = findById(reminderId);
		//TODO: verificare logica per i messaggi di pagamento
		reminderToUpdate.setReadFlag(isRead);	
		save(reminderToUpdate);
	}

	@Override
	public void getRemindersToNotify(){

		log.info("Lettura dei reminder");	
		
		int reminderNumMax = Integer.valueOf(reminderMax);
		int reminderDayNum = Integer.valueOf(reminderDay);
		LocalDateTime dateTime = LocalDateTime.now().minusDays(reminderDayNum);


		List<Reminder> remindersToNotify = reminderRepository.findByReadFlagAndReminderFlagAndNumReminderLessThanAndDataUltimoReminderLessThan(false, true, reminderNumMax, dateTime);
		List<Reminder> paymentsToNotify = reminderRepository.findByPaidFlagAndDataUltimoReminderLessThanAndContent_type(false, dateTime, MessageContentType.PAYMENT.toString());
		ReminderProducer producer = (ReminderProducer) ApplicationContextProvider.getBean("getReminderProducer");
		
		for (Reminder reminder : remindersToNotify) {

			byte[] byteReminder = new Gson().toJson(reminder).getBytes();
			producer.insertReminder(byteReminder);
			

			Reminder updateReminder = reminderRepository.findById(reminder.getId()).orElse(new Reminder());
			int contatore = updateReminder.getNumReminder()+1;
			updateReminder.setNumReminder(contatore);
			if(contatore==10) {
				updateReminder.setReminderFlag(false);
			}

			updateReminder.setDataUltimoReminder(LocalDateTime.now());

			List<LocalDateTime> listDate = updateReminder.getDateReminder();

			if(listDate==null) {
				listDate = new ArrayList<LocalDateTime>();
			}
			listDate.add(LocalDateTime.now());

			updateReminder.setDateReminder(listDate);
			reminderRepository.save(updateReminder);

		}
		for (Reminder payment : paymentsToNotify) {

			byte[] byteReminder = new Gson().toJson(payment).getBytes();
			producer.insertReminder(byteReminder);

			Reminder updatePayment = reminderRepository.findById(payment.getId()).orElse(new Reminder());
			int contatore = updatePayment.getNumReminder()+1;
			updatePayment.setNumReminder(contatore);


			updatePayment.setDataUltimoReminder(LocalDateTime.now());

			List<LocalDateTime> listDate = updatePayment.getDateReminder();

			if(listDate==null) {
				listDate = new ArrayList<LocalDateTime>();
			}
			listDate.add(LocalDateTime.now());

			updatePayment.setDateReminder(listDate);
			reminderRepository.save(updatePayment);
		}	
	}

	@Override
	public void deleteReminders() {
		log.info("Cancellazione dei reminder");
		reminderRepository.deleteByPaidFlag(true);
		reminderRepository.deleteByReadFlagOrNumReminder(true, Integer.valueOf(reminderMax));
	}
}
