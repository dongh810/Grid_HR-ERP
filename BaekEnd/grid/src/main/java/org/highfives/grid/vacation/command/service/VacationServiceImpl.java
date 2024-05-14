package org.highfives.grid.vacation.command.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.highfives.grid.user.entity.Employee;
import org.highfives.grid.user.service.UserService;
import org.highfives.grid.vacation.command.dto.VacationInfoDTO;
import org.highfives.grid.vacation.command.entity.VacationHistory;
import org.highfives.grid.vacation.command.entity.VacationInfo;
import org.highfives.grid.vacation.command.entity.VacationPolicy;
import org.highfives.grid.vacation.command.repository.VacationHistoryRepository;
import org.highfives.grid.vacation.command.repository.VacationInfoRepository;
import org.highfives.grid.vacation.command.repository.VacationPolicyRepository;
import org.highfives.grid.vacation.command.vo.ModifyPolicy;
import org.highfives.grid.vacation.command.vo.RegistPolicy;
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

    private UserService userService;
    private ModelMapper modelMapper;

    @Autowired
    public VacationServiceImpl(VacationPolicyRepository vacationPolicyRepository, ModelMapper modelMapper, UserService userService, VacationInfoRepository vacationInfoRepository, VacationHistoryRepository vacationHistoryRepository) {
        this.vacationPolicyRepository = vacationPolicyRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.vacationInfoRepository = vacationInfoRepository;
        this.vacationHistoryRepository = vacationHistoryRepository;
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

    // 각각의 직원이 몇개의 연차를 받아야 하는지 계산된것을 바탕으로 insert하는 메서드 (입사 후 1년이후)
//    @Override
//    @Transactional
////    @Scheduled(cron = "0 0 0 1 1 ?")
//    public void giveAnnualVacationAfterYear() {
//        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
//        String firstDayString = firstDayOfYear.toString();
//        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
//        String lastDayString = lastDayOfYear.toString();
//        HashMap<Integer, Integer> userVacationInfo = getVacationInfo();
//
//        for (Map.Entry<Integer, Integer> entry : userVacationInfo.entrySet()) {
//            Integer userId = entry.getKey();
//            Integer vacationNum = entry.getValue();
//            VacationInfo inputVacationInfo = new VacationInfo();
//
//            inputVacationInfo.setVacationNum(vacationNum);
//            inputVacationInfo.setAddTime(firstDayString);
//            inputVacationInfo.setEndTime(lastDayString);
//            inputVacationInfo.setEmployeeId(userId);
//            inputVacationInfo.setTypeId(1);
//
//            vacationInfoRepository.save(inputVacationInfo);
//
//        }
//    }
//
//    // 입사후 일년 전에는 한달에 한개씩의 연차 제공
//    @Override
//    @Transactional
//    public void giveAnnualVacationBeforeYear() {
//        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
//        String firstDayString = firstDayOfYear.toString();
//        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
//        String lastDayString = lastDayOfYear.toString();
//        HashMap<Integer, Integer> userVacationInfo = getVacationInfoBeforeYear();
//
//        for (Map.Entry<Integer, Integer> entry : userVacationInfo.entrySet()) {
//            Integer userId = entry.getKey();
//            Integer vacationNum = entry.getValue();
//            VacationInfo inputVacationInfo = new VacationInfo();
//
//            inputVacationInfo.setVacationNum(vacationNum);
//            inputVacationInfo.setAddTime(firstDayString);
//            inputVacationInfo.setEndTime(lastDayString);
//            inputVacationInfo.setEmployeeId(userId);
//            inputVacationInfo.setTypeId(1);
//
//            vacationInfoRepository.save(inputVacationInfo);
//
//        }
//    }

    @Override
    @Transactional
    public void giveAnnualVacation() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        String firstDayString = firstDayOfYear.toString();
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        String lastDayString = lastDayOfYear.toString();
        List<Employee> employees = userService.getAllUserinfo();

        for (int i=1 ; i<employees.size() ; i++ ) {
            int day = countDays(i+1);
            System.out.println(day);
            int months = countMonths(i+1);
            System.out.println(months);
            int valiNum = countVacationInfo(i+1);
            System.out.println(valiNum);

            if(day < 365 && months >= 1) {
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(1);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(i+1);
                inputVacationInfo.setTypeId(1);

                vacationInfoRepository.save(inputVacationInfo);
            } else if(day > 365 && valiNum != 0) {
                VacationInfo inputVacationInfo = new VacationInfo();

                inputVacationInfo.setVacationNum(valiNum);
                inputVacationInfo.setAddTime(firstDayString);
                inputVacationInfo.setEndTime(lastDayString);
                inputVacationInfo.setEmployeeId(i+1);
                inputVacationInfo.setTypeId(1);

                vacationInfoRepository.save(inputVacationInfo);
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
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        String firstDayString = firstDayOfYear.toString();
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        String lastDayString = lastDayOfYear.toString();
        List<Employee> employees = userService.getAllUserinfo();

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
            }
        }
    }

    // HashMap을 활용하여 유저 id와 받아야 할 휴가의 개수를 저장하는 메서드
//    private HashMap<Integer, Integer> getVacationInfo() {
//        List<Integer> annuals = countDays();
//        HashMap<Integer, Integer> userVacationInfo = new HashMap<>();
//
//        for (int i = 0; i < annuals.size(); i++) {
//
//            if (annuals.get(i) >= 365) {
//                int vacationNum = countVacation(annuals.get(i));
//                userVacationInfo.put(i + 2, vacationNum);
//            }
//        }
//
//        return userVacationInfo;
//
//    }

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

//    private HashMap<Integer, Integer> getVacationInfoBeforeYear() {
//        List<Employee> employees = userService.getAllUserinfo();
//        List<Integer> annuals = countDays();
//        LocalDate today = LocalDate.now();
//        HashMap<Integer, Integer> userVacationInfo = new HashMap<>();
//
//        for (int i = 1; i < employees.size(); i++) {
//            String day = employees.get(i).getJoinTime();
//            LocalDate joinDate = LocalDate.parse(day);
//
//            Period period = Period.between(joinDate, today);
//            int months = period.getMonths(); // 차이를 월 단위로 얻기
//            int valiNum = countVacationInfo(i);
//
//            if (months >= 1 && annuals.get(i - 1) < 365) {
//                userVacationInfo.put(i + 1, 1);
//            } else if (annuals.get(i - 1) > 365 && valiNum != 0) {
//                userVacationInfo.put(i+1, valiNum);
//            }
//
//        }
//
//        return userVacationInfo;
//
//    }



    // 입사 이후 1년이 지났지만, 1월1일 이후에 1년을 채워 1월1일에 연차를 못받았을때
    // 입사 이후 1년이 된 시점부터 연말까지 남은 달의 개수를 체크하여 연차를 제공하는 기능
//    private int countVacationInfo(int userId) {
//        Optional<VacationInfo> vacationInfo = vacationInfoRepository.findById(Long.valueOf(userId));
//        LocalDate today = LocalDate.now();
//        HashMap<Integer, Integer> userVacationInfo = getVacationInfoBeforeYear();
//        int currentYear = Year.now().getValue();
//        int vacationNumResult = 0;
//
//        LocalDate startDate = LocalDate.of(currentYear, 1, 1);
//
//        Period period = Period.between(startDate, today);
//        int months = period.getMonths();
//
//        int vacationNum = userVacationInfo.get(userId);
//        if (vacationInfo.get().getAddTime().contains(String.valueOf(currentYear)) && vacationInfo.get().getTypeId() == 1) {
//            vacationNumResult = vacationNum - months;
//        }
//
//        return vacationNumResult;
//    }

    private int countVacationInfo(int userId) {
        Optional<VacationInfo> vacationInfo = vacationInfoRepository.findByEmployeeId(Long.valueOf(userId));
        int vacationNumResult = 0;
        if(vacationInfo.isPresent()) {
            LocalDate today = LocalDate.now();
            int currentYear = Year.now().getValue();


            LocalDate startDate = LocalDate.of(currentYear, 1, 1);

            Period period = Period.between(startDate, today);
            int months = period.getMonths();

            int vacationNum = countVacation(countDays(userId));
            if (vacationInfo.get().getAddTime().contains(String.valueOf(currentYear)) && vacationInfo.get().getTypeId() == 1) {
                vacationNumResult = vacationNum - months;
            }
        }
        return vacationNumResult;
    }

}
