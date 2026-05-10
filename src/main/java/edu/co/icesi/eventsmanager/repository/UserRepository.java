package edu.co.icesi.eventsmanager.repository;

import edu.co.icesi.eventsmanager.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByAuthEmail(String email);
    Optional<User> findByInstitutionRefId(String institutionRefId);
}
