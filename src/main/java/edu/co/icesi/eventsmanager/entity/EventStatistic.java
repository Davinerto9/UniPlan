package edu.co.icesi.eventsmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "EVENT_STATISTICS")
public class EventStatistic {
    @Id
    private String eventId;
    
    private String eventTitle;
    private String eventType;
    private Integer totalRegistrations;
    private Integer totalCancellations;
    private Integer actualAttendees;
    private Double occupancyPercentage;
}
