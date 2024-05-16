package org.highfives.grid.vacation.command.controller;

import org.highfives.grid.vacation.command.service.VacationService;
import org.highfives.grid.vacation.command.vo.GiveVacation;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.highfives.grid.vacation.command.vo.RegistPolicy;
import org.highfives.grid.vacation.command.vo.RegistVacationType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(value = "VacationCommandController")
@RequestMapping("/vacation")
public class VacationController {

    private VacationService vacationService;

    @Autowired
    public VacationController(VacationService vacationService) {
        this.vacationService = vacationService;
    }

    @PutMapping("/policy/{id}")
    public void modifyVacationPolicy(@RequestBody ModifyPolicy policyInfo, @PathVariable int id) {
        vacationService.modifyVacationPolicy(policyInfo, id);
    }

    @DeleteMapping("/policy/{id}")
    public void deleteVacationPolicy(@PathVariable int id) {
        vacationService.deleteVacationPolicy(id);
    }

    @PostMapping("/policy")
    public void registVacationPolicy(@RequestBody RegistPolicy policyInfo) {
        vacationService.registVacationPolicy(policyInfo);
    }

    @GetMapping("/test")
    public void test() {
//        vacationService.giveAnnualVacationAfterYear();
//        vacationService.giveAnnualVacation();
//        vacationService.giveRegularVacation();
        vacationService.giveHealthVacation();
    }

    @GetMapping("/countTest")
    public void countTest(@RequestParam("employeeId") int employeeId, @RequestParam("typeId") int typeId) {
        vacationService.minusVacationNum(employeeId, typeId);
    }


    @PostMapping("/type")
    public void registVacationType(@RequestBody RegistVacationType typeInfo) {
        vacationService.registVacationType(typeInfo);
    }

    @PostMapping("/payments")
    public void giveVacationByManager(@RequestBody GiveVacation vacationInfo) {
        vacationService.giveVacationByManager(vacationInfo);
    }
}
