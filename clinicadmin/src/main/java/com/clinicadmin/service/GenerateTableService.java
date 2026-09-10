package com.clinicadmin.service;

import com.clinicadmin.dto.PhysiotherapyRecordDTO;
import com.clinicadmin.dto.Response;

public interface GenerateTableService {

    Response generateTable(PhysiotherapyRecordDTO request);
}
