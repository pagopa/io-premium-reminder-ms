package it.gov.pagopa.reminder.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.reminder.model.Reminder;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String>{


	@Query("{'content_paymentData_noticeNumber':?0, 'content_paymentData_payeeFiscalCode':?1}")
	List<Reminder> getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode);

	@Query(value="{content_type:{'$ne':?1}, $or:[{readFlag:true}, {maxReadMessageSend:{$gte:?0}}]}",delete = true)
	int deleteReadMessage(int maxReadMessageSend, String typeMessage);
	
	
	@Query(value="{content_type:?1, $or:[{paidFlag:true}, {maxPaidMessageSend:{$gte:?0}}]}",delete = true)
	int deletePaidMessage(int maxPaidMessageSend, String typeMessage);

	/**
	 * Retrieval of unread, unpaid reminders that have not exceeded the maximum number of notifications.
	 * @param maxReadMessageSend
	 * @return Reminder list
	 */
	@Query("{readFlag:false, paidFlag:false, maxReadMessageSend:{$lt:?0}, $or:[{lastDateReminder:{$exists: false}}, {lastDateReminder:{$lt:?1}}]}")
	List<Reminder> getReadMessageToNotify(int maxReadMessageSend, LocalDateTime dateTimeRead);
	
	/**
	 * Recovery of payment reminders, read, unpaid and that have not passed
     * the maximum number of notifications with expiration in the predetermined range.
	 * @param typeMessage
	 * @param maxPaidMessageSend
	 * @param dateTimePayment
	 * @param startDateReminder
	 * @param today
	 * @return Reminder list
	 */
	@Query("{readFlag:true, paidFlag:false, content_type:?0, maxPaidMessageSend:{$lt:?1}, $or:[{lastDateReminder:{$exists: false}}, {lastDateReminder:{$lt:?2}}], $or:[{content_paymentData_dueDate:{$exists: false}}, {content_paymentData_dueDate: {$lt:?3}}]}")
	List<Reminder> getPaidMessageToNotify(String typeMessage, Integer maxPaidMessageSend, LocalDateTime dateTimePayment, LocalDate startDateReminder);

	@Query("{rptId:?0}")
	Reminder getPaymentByRptId(String rptId);



}
