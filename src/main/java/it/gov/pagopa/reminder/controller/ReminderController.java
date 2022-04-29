package it.gov.pagopa.reminder.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import it.gov.pagopa.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;

@Api(tags = "API  Reminder")
@RestController
@Validated
@RequestMapping(value = "/", produces = APPLICATION_JSON_VALUE, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE }, method = RequestMethod.OPTIONS)
@RequiredArgsConstructor
public class ReminderController {

	@Autowired
	ReminderService reminderService;  
	
	
	@DeleteMapping("removeReminders")
	public void deleteReminders(){
		reminderService.deleteReminders();
	}
	
	@GetMapping("remindersToNotify")
	public void getRemindersToNotify(){
		reminderService.getRemindersToNotify();
	}
	
	@GetMapping("health")
	public ResponseEntity getHealtCheck(){
		return new ResponseEntity<>(HttpStatus.OK).ok().body(reminderService.healthCheck());
	}
}
