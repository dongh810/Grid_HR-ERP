package org.highfives.grid.vacation.command.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.highfives.grid.vacation.command.dto.VacationPolicyDTO;
import org.highfives.grid.vacation.command.entity.VacationPolicy;
import org.highfives.grid.vacation.command.repository.VacationPolicyRepository;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "VacationCommandService")
@Slf4j
public class VacationServiceImpl implements VacationService{

    private VacationPolicyRepository vacationPolicyRepository;
    private ModelMapper modelMapper;

    @Autowired
    public VacationServiceImpl(VacationPolicyRepository vacationPolicyRepository, ModelMapper modelMapper) {
        this.vacationPolicyRepository = vacationPolicyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void modifyVacationPolicy(ModifyPolicy policyInfo, int typeId) {
        VacationPolicy foundPolicy = vacationPolicyRepository.findById(Long.valueOf(typeId)).orElseThrow(IllegalAccessError::new);
        foundPolicy.setContent(policyInfo.getContent());
    }



}
