package it.gov.pagopa.reminder.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import it.gov.pagopa.reminder.model.Reminder;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String>{
	
	@Query(value="{$or:[{{$or:[{readFlag:true}, {numReminder:{$gt:?0}}]}}, {$or:[{paidFlag:true}, {content_paymentData_dueDate:{$lte:?1}}]}]}", delete = true)
	void deleteReminders(int numReminder, LocalDateTime paymentDate);
	
//	@Query(value="{$or:[{{$or:[{readFlag:true}, {numReminder:{$gt:?0}}]}}, {$or:[{paidFlag:true}, {content_paymentData_dueDate:{$lte:?1}}]}]}", delete = true)
	@Query("{$or:[{{$or:[{readFlag:true}, {numReminder:{$gt:?0}}]}}, {$or:[{paidFlag:true}, {content_paymentData_dueDate:{$exists: true}, content_paymentData_dueDate:{$lte:?1}}]}]}")
	List<Reminder> getdeleteReminders(int numReminder, LocalDateTime paymentDate);

	@Query("{readFlag:?0, reminderFlag:?1, numReminder:{$lt:?2}, $or:[ {dataUltimoReminder:{$exists: false}}, {dataUltimoReminder:{$lt:?3}}]}")
	List<Reminder> findRemindersToNotify(boolean readFlag, boolean reminderFlag, int reminderMax, LocalDateTime dataScheduler);

    @Query("{paidFlag:?0, $or:[{dataUltimoReminder:{$exists: false}, dataUltimoReminder:{$lt:?1}}], content_type:?2, content_paymentData_dueDate:{$gte:?3}}")
	List<Reminder> findPaymentsToNotify(boolean paidFlag, LocalDateTime dataScheduler, String contentType, LocalDateTime paymentDate);
}
