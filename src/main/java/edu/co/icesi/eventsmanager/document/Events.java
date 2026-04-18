package edu.co.icesi.eventsmanager.document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection ="events")
@Data
public class Events {
}
