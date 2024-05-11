package org.highfives.grid.vacation.command.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.highfives.grid.user.entity.Employee;
import org.highfives.grid.user.service.UserService;
import org.highfives.grid.vacation.command.dto.VacationInfoDTO;
import org.highfives.grid.vacation.command.entity.VacationInfo;
import org.highfives.grid.vacation.command.entity.VacationPolicy;
import org.highfives.grid.vacation.command.repository.VacationInfoRepository;
import org.highfives.grid.vacation.command.repository.VacationPolicyRepository;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.highfives.grid.vacation.command.vo.RegistPolicy;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "VacationCommandService")
@Slf4j
public class VacationServiceImpl implements VacationService {

    private VacationPolicyRepository vacationPolicyRepository;
    private VacationInfoRepository vacationInfoRepository;

    private UserService userService;
    private ModelMapper modelMapper;

    @Autowired
    public VacationServiceImpl(VacationPolicyRepository vacationPolicyRepository, ModelMapper modelMapper, UserService userService, VacationInfoRepository vacationInfoRepository) {
        this.vacationPolicyRepository = vacationPolicyRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.vacationInfoRepository = vacationInfoRepository;
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

    // 각각의 직원이 몇개의 연차를 받아야 하는지 계산된것을 바탕으로 insert하는 메서드
    @Override
    @Transactional
//    @Scheduled(cron = "0 0 0 1 1 ?")
    public void giveAnnualVacation() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        String firstDayString = firstDayOfYear.toString();
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        String lastDayString = lastDayOfYear.toString();
        HashMap<Integer, Integer> userVacationInfo = getVacationInfo();

        for(Map.Entry<Integer, Integer> entry : userVacationInfo.entrySet()) {
            Integer userId = entry.getKey();
            Integer vacationNum = entry.getValue();
            VacationInfo inputVacationInfo = new VacationInfo();

            inputVacationInfo.setVacationNum(vacationNum);
            inputVacationInfo.setAddTime(firstDayString);
            inputVacationInfo.setEndTime(lastDayString);
            inputVacationInfo.setEmployeeId(userId);
            inputVacationInfo.setTypeId(1);

            vacationInfoRepository.save(inputVacationInfo);

        }
    }

    // HashMap을 활용하여 유저 id와 받아야 할 휴가의 개수를 저장하는 메서드
    private HashMap<Integer, Integer> getVacationInfo() {
        List<Integer> annuals = countDays();
        HashMap<Integer, Integer> userVacationInfo = new HashMap<>();

        for (int i = 0; i < annuals.size(); i++) {
            System.out.println(annuals.get(i));
            if (annuals.get(i) >= 365) {

                int vacationNum = countVacation(annuals.get(i));
                userVacationInfo.put(i+2, vacationNum);
            }
        }

        return userVacationInfo;

    }

    // 입사이후 총 몇일이 지났는지 계산하는 메서드
    private List<Integer> countDays() {
        List<Employee> employees = userService.getAllUserinfo();
        List<Integer> annuals = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();

        for (int i = 1; i < employees.size(); i++) {
            String day = employees.get(i).getJoinTime();
            LocalDate localDate = LocalDate.parse(day);
            LocalDateTime joinDay = localDate.atStartOfDay();
            Duration duration = Duration.between(joinDay, today);
            long days = duration.toDays();
            annuals.add((int) days);
        }

        return annuals;
    }

    // 몇년차 인지에 따라 연차 개수를 부여하는 메서드
    private int countVacation(int days) {
        int year = days / 365;
        int vacation = 0;
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
