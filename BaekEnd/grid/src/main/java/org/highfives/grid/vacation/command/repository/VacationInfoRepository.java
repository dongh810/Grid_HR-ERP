package org.highfives.grid.vacation.command.repository;

import org.highfives.grid.vacation.command.entity.VacationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacationInfoRepository extends JpaRepository<VacationInfo, Long> {
}
