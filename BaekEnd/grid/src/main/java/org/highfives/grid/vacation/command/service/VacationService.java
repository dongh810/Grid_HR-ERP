package org.highfives.grid.vacation.command.service;

import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.highfives.grid.vacation.command.vo.RegistPolicy;

public interface VacationService {
    void modifyVacationPolicy(ModifyPolicy policyInfo, int id);

    void deleteVacationPolicy(int id);

    void registVacationPolicy(RegistPolicy policyInfo);

//    void giveAnnualVacationAfterYear();

    void giveRegularVacation();

    void giveHealthVacation();

//    void giveAnnualVacationBeforeYear();

    void giveAnnualVacation();
}
