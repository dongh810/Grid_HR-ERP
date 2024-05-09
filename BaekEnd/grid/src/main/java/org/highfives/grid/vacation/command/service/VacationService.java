package org.highfives.grid.vacation.command.service;

import org.highfives.grid.vacation.command.vo.ModifyPolicy;

public interface VacationService {
    void modifyVacationPolicy(ModifyPolicy policyInfo, int typeId);
}
