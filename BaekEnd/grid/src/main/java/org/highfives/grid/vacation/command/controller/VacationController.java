package org.highfives.grid.vacation.command.controller;

import org.highfives.grid.vacation.command.service.VacationService;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(value = "VacationCommandController")
@RequestMapping("/vacation")
public class VacationController {

    private VacationService vacationService;
    private ModelMapper modelMapper;

    @Autowired
    public VacationController(VacationService vacationService, ModelMapper modelMapper) {
        this.vacationService = vacationService;
        this.modelMapper = modelMapper;
    }

    @PutMapping("/policy/{typeId}")
    public void modifyVacationPolicy(@RequestBody ModifyPolicy policyInfo, @PathVariable int typeId) {
        vacationService.modifyVacationPolicy(policyInfo, typeId);

    }
}
