package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.entity.EventStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStatisticRepository extends JpaRepository<EventStatistic, String> {
}
