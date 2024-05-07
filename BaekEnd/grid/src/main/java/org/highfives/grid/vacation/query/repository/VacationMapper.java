package org.highfives.grid.vacation.query.repository;

import org.apache.ibatis.annotations.Mapper;
import org.highfives.grid.vacation.query.entity.VacationInfo;

import java.util.List;

@Mapper
public interface VacationMapper {

    List<VacationInfo> selectAllVacationInfo();

}
