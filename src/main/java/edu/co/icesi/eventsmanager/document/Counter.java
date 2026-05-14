package edu.co.icesi.eventsmanager.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Counters")
public class Counter {
    @Id
    private String id;
    private Long sequence;

    public Counter(String id) {
        this.id = id;
        this.sequence = 0L;
    }
}
