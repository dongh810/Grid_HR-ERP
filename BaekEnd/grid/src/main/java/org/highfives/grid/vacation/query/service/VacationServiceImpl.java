package org.highfives.grid.vacation.query.service;

import lombok.extern.slf4j.Slf4j;
import org.highfives.grid.vacation.query.dto.VacationInfoDTO;
import org.highfives.grid.vacation.query.entity.VacationInfo;
import org.highfives.grid.vacation.query.repository.VacationMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service(value = "VacationQueryService")
@Slf4j
public class VacationServiceImpl implements VacationService{
    private VacationMapper vacationMapper;
    private ModelMapper modelMapper;

    @Autowired
    public VacationServiceImpl(VacationMapper vacationMapper, ModelMapper modelMapper) {
        this.vacationMapper = vacationMapper;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<VacationInfoDTO> getAllVacations() {
        List<VacationInfo> vacations = vacationMapper.selectAllVacationInfo();

        return vacations.stream().map(vacation -> modelMapper.map(vacation, VacationInfoDTO.class)).collect(Collectors.toList());
    }
}
