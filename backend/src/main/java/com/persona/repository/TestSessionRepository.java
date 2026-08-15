package com.persona.repository;

import com.persona.model.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    Optional<TestSession> findBySessionCode(String sessionCode);
}
