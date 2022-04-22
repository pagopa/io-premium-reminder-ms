package it.gov.pagopa.reminder.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import it.gov.pagopa.reminder.model.Reminder;

@Repository
public interface ReminderRepository extends MongoRepository<Reminder, String>{
	
	
	void deleteByPaidFlag(@Param(value = "paidFlag") boolean paidFlag);
	
	void deleteByReadFlagOrNumReminder(@Param(value = "readFlag") boolean readFlag, 
			@Param(value = "numReminder") int numReminder);

	List<Reminder> findByReadFlagAndReminderFlagAndNumReminderLessThanAndDataUltimoReminderLessThan(boolean readFlag,
			boolean reminderFlag, int reminderMax, LocalDateTime dataScheduler);

    @Query("{'paidFlag':paidFlag, 'dataUltimoReminder':{'$lt':dataScheduler}, 'content_type': contentType}")
	List<Reminder> findByPaidFlagAndDataUltimoReminderLessThanAndContent_type(boolean paidFlag, LocalDateTime dataScheduler, String contentType);
}
