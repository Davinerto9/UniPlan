package edu.co.icesi.eventsmanager.service;

import edu.co.icesi.eventsmanager.document.Event;
import edu.co.icesi.eventsmanager.document.EventRegistration;
import edu.co.icesi.eventsmanager.entity.EventStatistic;
import edu.co.icesi.eventsmanager.repository.EventRegistrationRepository;
import edu.co.icesi.eventsmanager.repository.EventStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private EventStatisticRepository statisticRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    public void refreshEventStatistics(Event event) {
        if (event == null) {
            return;
        }

        EventStatistic statistic = statisticRepository.findById(event.getId()).orElse(new EventStatistic());
        statistic.setEventId(event.getId());
        statistic.setEventTitle(event.getTitle());
        statistic.setEventType(event.getType());

        List<EventRegistration> activeRegistrations = registrationRepository.findByEventIdAndStatus(event.getId(), "ACTIVE");
        List<EventRegistration> cancelRegistrations = registrationRepository.findByEventIdAndStatus(event.getId(), "CANCELLED");

        statistic.setTotalRegistrations(activeRegistrations.size() + cancelRegistrations.size());
        statistic.setTotalCancellations(cancelRegistrations.size());
        statistic.setActualAttendees(activeRegistrations.size());

        if (event.getCapacity() != null && event.getCapacity().getMax() != null && event.getCapacity().getMax() > 0) {
            statistic.setOccupancyPercentage(activeRegistrations.size() * 100.0 / event.getCapacity().getMax());
        } else {
            statistic.setOccupancyPercentage(0.0);
        }

        statisticRepository.save(statistic);
    }
}
