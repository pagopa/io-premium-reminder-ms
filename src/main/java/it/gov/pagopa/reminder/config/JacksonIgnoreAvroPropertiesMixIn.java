package it.gov.pagopa.reminder.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class JacksonIgnoreAvroPropertiesMixIn {

  @JsonIgnore
  public abstract org.apache.avro.Schema getSchema();

  @JsonIgnore
  public abstract org.apache.avro.specific.SpecificData getSpecificData();
}