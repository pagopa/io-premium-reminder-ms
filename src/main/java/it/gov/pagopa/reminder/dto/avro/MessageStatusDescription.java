/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package it.gov.pagopa.reminder.dto.avro;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public enum MessageStatusDescription {
  ACCEPTED, THROTTLED, FAILED, PROCESSED, REJECTED  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"MessageStatusDescription\",\"namespace\":\"it.gov.pagopa.reminder.dto\",\"symbols\":[\"ACCEPTED\",\"THROTTLED\",\"FAILED\",\"PROCESSED\",\"REJECTED\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}
