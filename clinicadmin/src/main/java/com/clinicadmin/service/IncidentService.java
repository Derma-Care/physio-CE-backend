package com.clinicadmin.service;

import com.clinicadmin.dto.IncidentDTO;
import com.clinicadmin.dto.Response;

public interface IncidentService {
 Response createIncident(IncidentDTO dto);
 Response getAllIncidents();
 Response UpdateIncidentStatu(String id, String status);
 Response deleteIncident(String id);
}
