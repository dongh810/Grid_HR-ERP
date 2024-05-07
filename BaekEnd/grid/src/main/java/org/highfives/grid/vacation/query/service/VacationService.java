package org.highfives.grid.vacation.query.service;

import org.highfives.grid.vacation.query.dto.VacationInfoDTO;

import java.util.List;

public interface VacationService {
    List<VacationInfoDTO> getAllVacations();
}
