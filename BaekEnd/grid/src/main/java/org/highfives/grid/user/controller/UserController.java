package org.highfives.grid.user.controller;

import org.highfives.grid.user.entity.Employee;
import org.highfives.grid.user.service.UserService;
import org.highfives.grid.user.vo.UserAnnualResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public List<Employee> getAllUserinfo() {
        return userService.getAllUserinfo();
    }
}
