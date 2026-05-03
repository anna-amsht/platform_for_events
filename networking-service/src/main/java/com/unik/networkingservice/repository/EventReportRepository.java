package com.unik.networkingservice.repository;

import com.unik.networkingservice.entity.EventReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventReportRepository extends JpaRepository<EventReport, UUID> {
}
