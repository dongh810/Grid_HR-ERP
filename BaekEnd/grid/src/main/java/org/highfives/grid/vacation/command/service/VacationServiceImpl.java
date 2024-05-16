package org.highfives.grid.vacation.command.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.highfives.grid.user.entity.Employee;
import org.highfives.grid.user.service.UserService;
import org.highfives.grid.vacation.command.dto.VacationInfoDTO;
import org.highfives.grid.vacation.command.entity.VacationHistory;
import org.highfives.grid.vacation.command.entity.VacationInfo;
import org.highfives.grid.vacation.command.entity.VacationPolicy;
import org.highfives.grid.vacation.command.entity.VacationType;
import org.highfives.grid.vacation.command.repository.VacationHistoryRepository;
import org.highfives.grid.vacation.command.repository.VacationInfoRepository;
import org.highfives.grid.vacation.command.repository.VacationPolicyRepository;
import org.highfives.grid.vacation.command.repository.VacationTypeRepository;
import org.highfives.grid.vacation.command.vo.GiveVacation;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.highfives.grid.vacation.command.vo.RegistPolicy;
import org.highfives.grid.vacation.command.vo.RegistVacationType;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service(value = "VacationCommandService")
@Slf4j
public class VacationServiceImpl implements VacationService {

    private VacationPolicyRepository vacationPolicyRepository;
    private VacationInfoRepository vacationInfoRepository;
    private VacationHistoryRepository vacationHistoryRepository;

    private VacationTypeRepository vacationTypeRepository;

    private UserService userService;
    private ModelMapper modelMapper;

    @Autowired
    public VacationServiceImpl(VacationPolicyRepository vacationPolicyRepository, ModelMapper modelMapper, UserService userService, VacationInfoRepository vacationInfoRepository, VacationHistoryRepository vacationHistoryRepository, VacationTypeRepository vacationTypeRepository) {
        this.vacationPolicyRepository = vacationPolicyRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.vacationInfoRepository = vacationInfoRepository;
        this.vacationHistoryRepository = vacationHistoryRepository;
        this.vacationTypeRepository = vacationTypeRepository;
    }

    @Override
    @Transactional
    public void modifyVacationPolicy(ModifyPolicy policyInfo, int id) {
        VacationPolicy foundPolicy = vacationPolicyRepository.findById(Long.valueOf(id)).orElseThrow(IllegalAccessError::new);
        foundPolicy.setContent(policyInfo.getContent());
    }

    @Override
    @Transactional
    public void deleteVacationPolicy(int id) {
        vacationPolicyRepository.deleteById(Long.valueOf(id));
    }

    @Override
    @Transactional
    public void registVacationPolicy(RegistPolicy policyInfo) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        VacationPolicy vacationPolicy = modelMapper.map(policyInfo, VacationPolicy.class);
        vacationPolicyRepository.save(vacationPolicy);
    }

    // 입사한지 1년이 안된 직원들에게 달에 한개씩 월차를 제공하는 메서드
    @Override
    @Transactional
    public void giveAnnualVacationBeforeYear() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        String firstDayString = firstDayOfMonth.toString();
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        String lastDayString = lastDayOfMonth.toString();
        List<Employee> employees = userService.getAllUserinfo();

        // 1년은 안되고, 1달은 지난 직원이 사용안한 월차가 있으면 삭제하고 그 기록을 vacation_history에 저장
        for (int i=1 ; i<employees.size() ; i++ ) {
            int userId = employees.get(i).getId();
            int day = countDays(userId);
            int months = countMonths(userId);

            if(day < 365 && months >= 1) {
                if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 4) != null) {
                    if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 4).getVacationNum() != 0) {
                        vacationInfoRepository.deleteByTypeIdAndEmployeeId(4,userId);
                        VacationHistory inputVacationHistory = new VacationHistory();

                        inputVacationHistory.setChangeTime(firstDayString);
                        inputVacationHistory.setChangeReason("사용기한 만료로 인한 월차 소멸");
                        inputVacationHistory.setTypeId(4);
                        inputVacationHistory.setChangeTypeId(2);
                        inputVacationHistory.setEmployeeId(userId);

                        vacationHistoryRepository.save(inputVacationHistory);
                    } else {
                        vacationInfoRepository.deleteByTypeIdAndEmployeeId(4,userId);
                    }
                }
            }
        }

        // 1년은 안되고, 1달은 지난 직원에게 월차를 제공하고 그 기록을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            int userId = employees.get(i).getId();
            int day = countDays(userId);
            int months = countMonths(userId);

            if(day < 365 && months >= 1) {
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(1);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(userId);
                inputVacationInfo.setTypeId(4);

                vacationInfoRepository.save(inputVacationInfo);

                VacationHistory inputVacationHistory = new VacationHistory();

                inputVacationHistory.setChangeTime(firstDayString);
                inputVacationHistory.setChangeReason("월 갱신에 따른 월차 자동 부여");
                inputVacationHistory.setTypeId(4);
                inputVacationHistory.setChangeTypeId(1);
                inputVacationHistory.setEmployeeId(userId);

                vacationHistoryRepository.save(inputVacationHistory);
            }

        }
    }

    // 입사한지 1년이 지난 직원들에게 연차를 제공하는 메서드
    @Override
    @Transactional
    public void giveAnnualVacationAfterYear() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        String firstDayString = firstDayOfYear.toString();
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        String lastDayString = lastDayOfYear.toString();
        List<Employee> employees = userService.getAllUserinfo();

        // 입사 후 1년이 지난 직원들이 사용안한 연차가 있다면 삭제하고 그 기록을 vacation_history에 저장
        for (int i=1 ; i<employees.size() ; i++ ) {
            int userId = employees.get(i).getId();
            int day = countDays(userId);

            if(day >= 365) {
                if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 1) != null) {
                    if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 1).getVacationNum() != 0) {
                        vacationInfoRepository.deleteByTypeIdAndEmployeeId(1,userId);
                        VacationHistory inputVacationHistory = new VacationHistory();

                        inputVacationHistory.setChangeTime(firstDayString);
                        inputVacationHistory.setChangeReason("사용기한 만료로 인한 연차 소멸");
                        inputVacationHistory.setTypeId(1);
                        inputVacationHistory.setChangeTypeId(2);
                        inputVacationHistory.setEmployeeId(userId);

                        vacationHistoryRepository.save(inputVacationHistory);
                    } else {
                        vacationInfoRepository.deleteByTypeIdAndEmployeeId(1,userId);
                    }
                }
            }
        }

        // 입사 후 1년이 지난 직원들에게 연차를 제공하고 그 기록을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            int userId = employees.get(i).getId();
            int day = countDays(userId);
            int months = countMonths(userId);
            double vacationNum = countVacation(day);

            if(day >= 365) {
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(vacationNum);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(userId);
                inputVacationInfo.setTypeId(1);

                vacationInfoRepository.save(inputVacationInfo);

                VacationHistory inputVacationHistory = new VacationHistory();

                inputVacationHistory.setChangeTime(firstDayString);
                inputVacationHistory.setChangeReason("연도 갱신에 따른 연차 자동 부여");
                inputVacationHistory.setTypeId(1);
                inputVacationHistory.setChangeTypeId(1);
                inputVacationHistory.setEmployeeId(userId);

                vacationHistoryRepository.save(inputVacationHistory);
            // 1년이 안된 직원들은 작년에 얼마나 다녔는지에 따라 당해 연차를 제공
            } else if (day < 365 && months >= 1) {
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(months);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(userId);
                inputVacationInfo.setTypeId(1);

                vacationInfoRepository.save(inputVacationInfo);

                VacationHistory inputVacationHistory = new VacationHistory();

                inputVacationHistory.setChangeTime(firstDayString);
                inputVacationHistory.setChangeReason("연도 갱신에 따른 연차 자동 부여");
                inputVacationHistory.setTypeId(1);
                inputVacationHistory.setChangeTypeId(1);
                inputVacationHistory.setEmployeeId(userId);

                vacationHistoryRepository.save(inputVacationHistory);
            }

        }
    }

    // 각각의 직원에게 정기휴가를 insert하는 메서드
    @Override
    @Transactional
//    @Scheduled(cron = "0 0 0 1 1 ?")
    public void giveRegularVacation() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        String firstDayString = firstDayOfYear.toString();
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        String lastDayString = lastDayOfYear.toString();
        List<Employee> employees = userService.getAllUserinfo();

        // 휴가를 새로 insert 하기 전, 기존의 휴가가 남아있다면 삭제하고, 그 기록을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            int userId = employees.get(i).getId();

            if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 3).getVacationNum() != 0) {
                vacationInfoRepository.deleteByTypeIdAndEmployeeId(3,userId);
                VacationHistory inputVacationHistory = new VacationHistory();

                inputVacationHistory.setChangeTime(firstDayString);
                inputVacationHistory.setChangeReason("사용기한 만료로 인한 정기휴가 소멸");
                inputVacationHistory.setTypeId(3);
                inputVacationHistory.setChangeTypeId(2);
                inputVacationHistory.setEmployeeId(userId);

                vacationHistoryRepository.save(inputVacationHistory);
            } else {
                vacationInfoRepository.deleteByTypeIdAndEmployeeId(3,userId);
            }
        }

        // 휴가를 새로 insert 하고, 그 기록을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            int userId = employees.get(i).getId();
            VacationInfo inputVacationInfo = new VacationInfo();

            inputVacationInfo.setVacationNum(4);
            inputVacationInfo.setAddTime(firstDayString);
            inputVacationInfo.setEndTime(lastDayString);
            inputVacationInfo.setEmployeeId(userId);
            inputVacationInfo.setTypeId(3);

            vacationInfoRepository.save(inputVacationInfo);

            VacationHistory inputVacationHistory = new VacationHistory();

            inputVacationHistory.setChangeTime(firstDayString);
            inputVacationHistory.setChangeReason("연도 갱신에 따른 정기휴가 자동 부여");
            inputVacationHistory.setTypeId(3);
            inputVacationHistory.setChangeTypeId(1);
            inputVacationHistory.setEmployeeId(userId);

            vacationHistoryRepository.save(inputVacationHistory);
        }
    }

    // 여자직원에게 보건휴가를 insert하는 메서드
    @Override
    @Transactional
//    @Scheduled(cron = "0 0 0 1 1 ?")
    public void giveHealthVacation() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        String firstDayString = firstDayOfMonth.toString();
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        String lastDayString = lastDayOfMonth.toString();
        List<Employee> employees = userService.getAllUserinfo();

        // 기존의 보건휴가가 남아있으면 지우고, 그 이력을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            if (employees.get(i).getGender().equals("F")) {
                int userId = employees.get(i).getId();
                try{
                    if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 2) != null) {
                        if(vacationInfoRepository.findByEmployeeIdAndTypeId(userId, 2).getVacationNum() != 0) {
                            vacationInfoRepository.deleteByTypeIdAndEmployeeId(2,userId);
                            VacationHistory inputVacationHistory = new VacationHistory();

                            inputVacationHistory.setChangeTime(firstDayString);
                            inputVacationHistory.setChangeReason("사용기한 만료로 인한 보건휴가 소멸");
                            inputVacationHistory.setTypeId(2);
                            inputVacationHistory.setChangeTypeId(2);
                            inputVacationHistory.setEmployeeId(userId);

                            vacationHistoryRepository.save(inputVacationHistory);
                        } else {
                            vacationInfoRepository.deleteByTypeIdAndEmployeeId(3,userId);
                        }
                    }
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }

        // 보건휴가를 새로 insert 하고, 그 이력을 vacation_history에 저장
        for (int i = 1; i < employees.size(); i++) {
            if (employees.get(i).getGender().equals("F")) {
                int userId = employees.get(i).getId();
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(1);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(userId);
                inputVacationInfo.setTypeId(2);

                vacationInfoRepository.save(inputVacationInfo);

                VacationHistory inputVacationHistory = new VacationHistory();

                inputVacationHistory.setChangeTime(firstDayString);
                inputVacationHistory.setChangeReason("월 갱신에 따른 보건휴가 자동 부여");
                inputVacationHistory.setTypeId(2);
                inputVacationHistory.setChangeTypeId(1);
                inputVacationHistory.setEmployeeId(userId);

                vacationHistoryRepository.save(inputVacationHistory);
            }
        }
    }

    @Override
    @Transactional
    public void registVacationType(RegistVacationType typeInfo) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        VacationType vacationType = modelMapper.map(typeInfo, VacationType.class);
        vacationTypeRepository.save(vacationType);
    }

    @Override
    @Transactional
    public void giveVacationByManager(GiveVacation vacationInfo) {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        VacationInfo inputVacationInfo = modelMapper.map(vacationInfo, VacationInfo.class);
        LocalDate today = LocalDate.now();
        String day = today.toString();
        inputVacationInfo.setAddTime(day);
        vacationInfoRepository.save(inputVacationInfo);

    }

    @Override
    @Transactional
    public void plusVacationNum(int employeeId, int typeId) {
        VacationInfo vacationInfo = new VacationInfo();
        if(typeId == 5 || typeId == 6) {
            vacationInfo = vacationInfoRepository.findByEmployeeIdAndTypeId(employeeId, 1);
        } else {
            vacationInfo = vacationInfoRepository.findByEmployeeIdAndTypeId(employeeId, typeId);
        }
        VacationHistory inputVacationHistory = new VacationHistory();
        LocalDate today = LocalDate.now();
        String vacationTypeName = vacationTypeRepository.findById((long) typeId).get().getTypeName();

        if(typeId == 5) {
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()+0.5);
        } else if(typeId == 6) {
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()+0.25);
        } else
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()+1);

        inputVacationHistory.setChangeTime(today.toString());
        inputVacationHistory.setChangeReason(vacationTypeName + " 사용취소로 인한 " + vacationTypeName + " 개수 증가");
        inputVacationHistory.setTypeId(typeId);
        inputVacationHistory.setChangeTypeId(4);
        inputVacationHistory.setEmployeeId(employeeId);

        vacationHistoryRepository.save(inputVacationHistory);

    }

    @Override
    @Transactional
    public void minusVacationNum(int employeeId, int typeId) {
        VacationInfo vacationInfo = new VacationInfo();
        if(typeId == 5 || typeId == 6) {
            vacationInfo = vacationInfoRepository.findByEmployeeIdAndTypeId(employeeId, 1);
        } else {
            vacationInfo = vacationInfoRepository.findByEmployeeIdAndTypeId(employeeId, typeId);
        }
        VacationHistory inputVacationHistory = new VacationHistory();
        LocalDate today = LocalDate.now();
        String vacationTypeName = vacationTypeRepository.findById((long) typeId).get().getTypeName();

        if(typeId == 5) {
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()-0.5);
        } else if(typeId == 6) {
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()-0.25);
        } else
            vacationInfo.setVacationNum(vacationInfo.getVacationNum()-1);

        inputVacationHistory.setChangeTime(today.toString());
        inputVacationHistory.setChangeReason(vacationTypeName + " 사용으로 인한 " + vacationTypeName + " 개수 감소");
        inputVacationHistory.setTypeId(typeId);
        inputVacationHistory.setChangeTypeId(3);
        inputVacationHistory.setEmployeeId(employeeId);

        vacationHistoryRepository.save(inputVacationHistory);
    }

    // 입사이후 총 몇일이 지났는지 계산하는 메서드
    private int countDays(int userId) {
        Optional<Employee> employees = userService.getUserInfo(userId);
        LocalDateTime today = LocalDateTime.now();

        String day = employees.get().getJoinTime();
        LocalDate localDate = LocalDate.parse(day);
        LocalDateTime joinDay = localDate.atStartOfDay();
        Duration duration = Duration.between(joinDay, today);
        long days = duration.toDays();
        int annuals = ((int) days);


        return annuals;
    }

    // 입사이후 총 몇달이 지났는지 계산하는 메서드
    private int countMonths(int userId) {
        Optional<Employee> employee= userService.getUserInfo(userId);
        LocalDate today = LocalDate.now();

        String day = employee.get().getJoinTime();
        LocalDate joinDate = LocalDate.parse(day);

        Period period = Period.between(joinDate, today);
        int months = period.getMonths(); // 차이를 월 단위로 얻기

        return months;

    }

    // 몇년차 인지에 따라 연차 개수를 부여하는 메서드
    private double countVacation(int days) {
        int year = days / 365;
        double vacation = 0;
        if (year >= 1 && year < 3) {
            vacation = 15;
        } else if (year < 5) {
            vacation = 16;
        } else if (year < 7) {
            vacation = 17;
        } else if (year < 9) {
            vacation = 18;
        } else if (year < 11) {
            vacation = 19;
        } else
            vacation = 20;

        return vacation;
    }


}
