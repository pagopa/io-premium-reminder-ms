package it.gov.pagopa.reminder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import it.gov.pagopa.reminder.dto.MessageStatus;
import it.gov.pagopa.reminder.dto.PaymentMessage;
import it.gov.pagopa.reminder.dto.avro.MessageContentType;
import it.gov.pagopa.reminder.model.Reminder;
import it.gov.pagopa.reminder.repository.ReminderRepository;
import it.gov.pagopa.reminder.service.ReminderServiceImpl;

public class AbstractTest {

	private static final String EMPTY = "empty";
	private static final String FULL = "full";
	private static final String NULL = "null";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @MockBean
    protected RestTemplate restTemplate;
    
	@MockBean
	protected ReminderRepository mockRepository;
	
	@InjectMocks
	protected ReminderServiceImpl service;

	protected void mockSaveWithResponse(Reminder returnReminder) {
		Mockito.when(mockRepository.save(Mockito.any(Reminder.class))).thenReturn(returnReminder);
	}

	protected void mockFindIdWithResponse(Reminder returnReminder1) {
		Mockito.when(mockRepository.findById(Mockito.anyString())).thenReturn(Optional.of(returnReminder1));
	}

	public void mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(Reminder reminder) {
		Mockito.when(mockRepository.getPaymentByNoticeNumberAndFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(reminder);
	}

	protected void mockFindReminderAndPaymentNotifyWithResponse(List<Reminder> listReturnReminder) {
//		Mockito.when(mockRepository.findReminderAndPaymentToNotify(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyInt(), Mockito.any(LocalDateTime.class), Mockito.anyString(), 
//				Mockito.anyBoolean(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class))).thenReturn(listReturnReminder);
	}


	protected void mockFindRemindersToNotifyWithResponse(List<Reminder> listReturnReminder) {
//		Mockito.when(mockRepository.findRemindersToNotify(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyInt(), Mockito.any(LocalDateTime.class), Mockito.anyString())).thenReturn(listReturnReminder);
	}


	protected void mockFindPaymentsToNotifyWithResponse( List<Reminder> listReturnReminder) {
//		Mockito.when(mockRepository.findPaymentsToNotify(Mockito.anyBoolean(), Mockito.any(LocalDateTime.class), Mockito.anyString(), Mockito.any(LocalDateTime.class))).thenReturn(listReturnReminder);
	}

	protected void mockDeleteRemindersWithResponse(int retValue) {
//		Mockito.when(mockRepository.deleteReminders(Mockito.anyInt(), Mockito.any(LocalDateTime.class), Mockito.anyString())).thenReturn(retValue);
	}

	protected List<Reminder>  selectListReminderMockObject(String type) {
		List<Reminder> retList = null;
		Reminder returnReminder1 = null;

		switch (type){
		case EMPTY:
			retList = new ArrayList<Reminder>();
			break;
		case FULL:
			retList = new ArrayList<Reminder>();
			returnReminder1 = selectReminderMockObject(type, "1","GENERIC","AAABBB77Y66A444A",3);
			retList.add(returnReminder1);
			returnReminder1 = selectReminderMockObject(type, "2","PAYMENT","CCCDDD77Y66A444A",3);
			retList.add(returnReminder1);
			break;
		case NULL:
			retList = null;
			break;
		default:
			retList = new ArrayList<Reminder>();
			break;
		};

		return retList;

	}

	protected Reminder selectReminderMockObject(String type, String id, String contentType, String fiscalCode, int numReminder) {
		Reminder returnReminder1 = null;

		switch (type){
		case EMPTY:
			returnReminder1 = new Reminder();
		default:
			returnReminder1 = new Reminder();
			returnReminder1.setId(id);
			returnReminder1.setContent_type(MessageContentType.valueOf(contentType));
			returnReminder1.setFiscal_code(fiscalCode);
		};

		return returnReminder1;

	}
	
	protected MessageStatus selectMessageStatusMockObject(String type, String messageId, boolean isRead, boolean isPaid) {
		MessageStatus messageStatus = null;
		switch (type){
		case EMPTY:
			messageStatus = new MessageStatus();
		default:
			messageStatus = new MessageStatus(messageId, isRead, isPaid);
		};
		return messageStatus;
	}	

	protected PaymentMessage getPaymentMessage(String noticeNumber, String fiscalCode, boolean paid, LocalDate d, Double amount, String source) {

		PaymentMessage pm = new PaymentMessage(noticeNumber, fiscalCode, paid, d, amount, source);
		return pm;
	}

	protected void before() {
		service = new ReminderServiceImpl();
	}

}
