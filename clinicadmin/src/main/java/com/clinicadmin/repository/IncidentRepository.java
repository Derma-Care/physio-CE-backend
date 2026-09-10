package com.clinicadmin.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.clinicadmin.entity.Incident;
import com.clinicadmin.enumclasses.IncidentStatus;

public interface IncidentRepository extends MongoRepository<Incident, String> {
List<Incident>findByStatus(IncidentStatus status);
}
