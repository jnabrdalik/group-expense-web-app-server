package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.repository.ExpenseRepository;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @MockBean
    ExpenseRepository expenseRepository;
    @MockBean
    GroupRepository groupRepository;
    @MockBean
    UserRepository userRepository;

    ExpenseService expenseService;


    @Test
    void getExpenseHistory() {
    }

    @Test
    void addExpense() {
    }

    @Test
    void editExpense() {
    }

    @Test
    void revertLastChange() {
    }

    @Test
    void deleteExpense() {
    }
}