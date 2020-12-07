package com.example.groupexpensewebapp;

import com.example.groupexpensewebapp.dto.ExpenseInput;
import com.example.groupexpensewebapp.dto.GroupInput;
import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.model.User;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import com.example.groupexpensewebapp.service.ExpenseService;
import com.example.groupexpensewebapp.service.GroupService;
import com.example.groupexpensewebapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DatabasePopulator implements CommandLineRunner {

    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    private static final List<String> groupNames = List.of("Wyjazd", "Wakacje", "Impreza", "Wyjście do baru", "Mieszkanie");
    private static final List<String> names = List.of("Jan", "Michał", "Mateusz", "Piotr", "Kamil", "Marcin", "Anna", "Julia", "Karolina", "A", "B", "C", "D", "E", "F");
    private static final List<String> expenseDescriptions = List.of("Zakupy", "Jedzenie", "Piwo", "Bilety", "Napoje", "Paliwo", "Kino", "Pociąg", "Taxi", "Chipsy");

    @Override
    public void run(String... args) throws Exception {
        groupRepository.deleteAll();
        userRepository.deleteAll();

        String adminUsername = "Jakub";
        String password = "admin";

        UserInput admin = new UserInput();
        admin.setName(adminUsername);
        admin.setPassword(password);
        long adminId = userService.addUser(admin).getId();

        List<Long> userIds = names.stream()
                .map(name -> {
                    UserInput user = new UserInput();
                    user.setName(name);
                    user.setPassword(password);
                    return userService.addUser(user).getId();
                }).collect(Collectors.toList());

        List<Long> userIdsWithAdmin = new ArrayList<>(userIds);
        userIdsWithAdmin.add(adminId);

        groupNames.forEach(groupName -> {
            GroupInput group = new GroupInput();
            group.setName(groupName);

            long groupId = groupService.addGroup(group, adminUsername).getId();

            userIds.forEach(id -> groupService.addUserToGroup(id, groupId, adminUsername));


            getRandomExpenseDescriptions().forEach(expenseDescription -> {
                for (int i = 0; i < 2; i++) {
                    ExpenseInput expense = new ExpenseInput();
                    expense.setGroupId(groupId);
                    expense.setAmount(getRandomAmount());
                    expense.setDescription(expenseDescription + (i > 0 ? " " + (i+1) : ""));
                    expense.setTimestamp(getRandomTimestamp());
                    expense.setPayerId(selectRandomId(userIdsWithAdmin));
                    expense.setPayeesIds(selectRandomIds(userIdsWithAdmin));

                    expenseService.addExpense(expense, adminUsername);
                }
            });

        });
    }

    public List<String> getRandomExpenseDescriptions() {
        List<String> arrayOfDescriptions = new ArrayList<>(expenseDescriptions);
        Collections.shuffle(arrayOfDescriptions);

        int maxIndex = ThreadLocalRandom.current().nextInt(4, expenseDescriptions.size());
        return arrayOfDescriptions.subList(0, maxIndex);
    }

    public int getRandomAmount() {
        return ThreadLocalRandom.current().nextInt(1, 110000);
    }

    public long getRandomTimestamp() {
        return ThreadLocalRandom.current().nextLong(1546300800000L, System.currentTimeMillis());
    }

    public static long selectRandomId(List<Long> ids) {
        int id = ThreadLocalRandom.current().nextInt(ids.size());

        return ids.get(id);
    }

    public static Set<Long> selectRandomIds(List<Long> ids) {
        ArrayList<Long> listCopy = new ArrayList<>(ids);
        Collections.shuffle(listCopy);
        int maxIndex = ThreadLocalRandom.current().nextInt(2, listCopy.size());

        return new HashSet<>(listCopy.subList(0, maxIndex));
    }
}
