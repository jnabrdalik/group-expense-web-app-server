package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.*;
import com.example.groupexpensewebapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final ExpenseHistoryRepository expenseHistoryRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<ExpenseChange> getExpenseHistory(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense doesn't exist!"));

        Group group = expense.getGroup();
        if (group.isForRegisteredOnly() && !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this expense!");
        }

        ExpenseDetails currentVersion = modelMapper.map(expense, ExpenseDetails.class);

        List<ExpenseDetails> history = Stream.concat(expense.getPreviousVersions().stream()
                .map(e -> modelMapper.map(e, ExpenseDetails.class)), Stream.of(currentVersion))
                .sorted(Comparator.comparingLong(ExpenseDetails::getTimeCreated))
                .collect(Collectors.toCollection(ArrayList::new));

        List<ExpenseChange> changes = new ArrayList<>();
        for (int i = 0; i < history.size() - 1; i++) {
            ExpenseDetails beforeChange = history.get(i);
            ExpenseDetails afterChange = history.get(i + 1);

            UserSummary creator = afterChange.getCreator();
            MemberSummary changedBySummary = null;
            if (creator != null) {
                Member changedBy = memberRepository.findByRelatedUserName_AndGroup_Id(creator.getName(), group.getId())
                         .orElse(null);
                changedBySummary = modelMapper.map(changedBy, MemberSummary.class);
            }

            long changeTimestamp = afterChange.getTimeCreated();

            List<ExpenseChange.FieldChange> fieldChanges = new ArrayList<>();
            ExpenseChange expenseChange = new ExpenseChange(changedBySummary, changeTimestamp, fieldChanges);

            if (beforeChange.getAmount() != afterChange.getAmount()) {
                fieldChanges.add(new ExpenseChange.FieldChange( "amount",
                        Integer.toString(beforeChange.getAmount()),
                        Integer.toString(afterChange.getAmount())));
            }

            if (!beforeChange.getDescription().equals(afterChange.getDescription())) {
                fieldChanges.add(new ExpenseChange.FieldChange("description",
                        beforeChange.getDescription(),
                        afterChange.getDescription()));
            }

            if (beforeChange.getTimestamp() != afterChange.getTimestamp()) {
                fieldChanges.add(new ExpenseChange.FieldChange("timestamp",
                        Long.toString(beforeChange.getTimestamp()),
                        Long.toString(afterChange.getTimestamp())));
            }

            if (beforeChange.getPayer().getId() != afterChange.getPayer().getId()) {
                fieldChanges.add(new ExpenseChange.FieldChange("payer",
                        beforeChange.getPayer().getName(),
                        afterChange.getPayer().getName()));
            }

            if (!beforeChange.getInvolvements().equals(afterChange.getInvolvements())) {
                fieldChanges.add(new ExpenseChange.FieldChange("payees",
                        getPayeesNames(beforeChange.getInvolvements()),
                        getPayeesNames(afterChange.getInvolvements())));

//                fieldChanges.addAll(getWeightChanges(beforeChange.getInvolvements(), afterChange.getInvolvements()));
            }

            changes.add(expenseChange);
        }

        Collections.reverse(changes);
        return changes;
    }

    private String getPayeesNames(Set<ExpenseDetails.Involvement> involvements) {
        StringBuilder sb = new StringBuilder();
        involvements.stream()
                .sorted(Comparator.comparing(i -> i.getPayee().getName()))
                .forEachOrdered(i -> {
                    sb.append(i.getPayee().getName());

                    if (i.getWeight() > 1) {
                        sb
                                .append(" (")
                                .append(i.getWeight())
                                .append(")");
                    }
                    sb.append(", ");
                });

        String concatenated = sb.toString();
        return concatenated.substring(0, concatenated.length() - 2);
    }

//    private List<ExpenseChange.FieldChange> getWeightChanges(
//            Set<ExpenseDetails.Involvement> before,
//            Set<ExpenseDetails.Involvement> after) {
//
//        Set<ExpenseDetails.Involvement> added = new HashSet<>(after);
//        added.removeAll(before);
//
//        Set<ExpenseDetails.Involvement> removed = new HashSet<>(before);
//        removed.removeAll(after);
//
//        E
//
//        return List.of(
//                new ExpenseChange.FieldChange("payees"),
//                new ExpenseChange.FieldChange("payees"))
//    }

    public ExpenseDetails addExpense(ExpenseInput input, String username) {
        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (group.isArchived() || group.isForRegisteredOnly() &&
                !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to add expenses to this group!");
        }

        if (input.getDescription() == null || input.getAmount() < 0 || input.getInvolvements() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Member payer = memberRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        UserEntity creator = userRepository.findByName(username);

        Expense expense = new Expense();
        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setPayer(payer);
        expense.setInvolvements(getInvolvements(input, expense));
        expense.setGroup(group);
        expense.setTimeCreated(System.currentTimeMillis());
        expense.setCreator(creator);

        Expense addedExpense = repository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public ExpenseDetails addDebtPayment(DebtPaymentInput input, String username) {
        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (group.isArchived() || group.isForRegisteredOnly() &&
                !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to add payments to this group!");
        }

        if (input.getAmount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Member payer = memberRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Member payee = memberRepository.findById(input.getPayeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        UserEntity creator = userRepository.findByName(username);

        Expense expense = new Expense();
        expense.setAmount(input.getAmount());
        expense.setTimestamp(System.currentTimeMillis());
        expense.setPayer(payer);
        Involvement involvement = new Involvement();
        involvement.setExpense(expense);
        involvement.setWeight(1);
        involvement.setPayee(payee);
        expense.setInvolvements(Collections.singleton(involvement));
        expense.setGroup(group);
        expense.setTimeCreated(System.currentTimeMillis());
        expense.setCreator(creator);

        Expense addedExpense = repository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public ExpenseDetails editExpense(long expenseId, ExpenseInput input, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = expense.getGroup();
        if (group.isArchived() || group.isForRegisteredOnly() &&
                !memberRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to edit this expense!");
        }

        Member payer = memberRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (input.getDescription() == null || input.getAmount() < 0 || input.getInvolvements() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        UserEntity creator = userRepository.findByName(username);

        ExpenseHistory oldExpense = new ExpenseHistory();
        oldExpense.setOriginalExpense(expense);
        oldExpense.setDescription(expense.getDescription());
        oldExpense.setAmount(expense.getAmount());
        oldExpense.setTimestamp(expense.getTimestamp());
        oldExpense.setPayer(expense.getPayer());
        oldExpense.setInvolvements(expense.getInvolvements().stream()
                .map(i -> {
                    InvolvementHistory involvementHistory = new InvolvementHistory();
                    involvementHistory.setExpense(oldExpense);
                    involvementHistory.setPayee(i.getPayee());
                    involvementHistory.setWeight(i.getWeight());

                    return involvementHistory;
                })
                .collect(Collectors.toSet()));
        oldExpense.setCreator(expense.getCreator());
        oldExpense.setTimeCreated(expense.getTimeCreated());
        expenseHistoryRepository.save(oldExpense);

        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setPayer(payer);
        expense.getInvolvements().forEach(i -> i.setExpense(null));
        expense.setInvolvements(getInvolvements(input, expense));
        expense.setCreator(creator);
        expense.setTimeCreated(System.currentTimeMillis());
        repository.save(expense);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    private Set<Involvement> getInvolvements(ExpenseInput input, Expense expense) {
        Set<Involvement> involvements = new HashSet<>();
        HashMap<Long, Integer> payeeToWeightMapping = new HashMap<>();

        input.getInvolvements().forEach(
                i -> {
                    if (i.getWeight() < 1) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                    }
                    payeeToWeightMapping.put(i.getPayeeId(), i.getWeight());
                }
        );

        Set<Long> payeesIds = input.getInvolvements().stream()
                .map(InvolvementInput::getPayeeId)
                .collect(Collectors.toSet());

        memberRepository.findAllById(payeesIds).forEach(
                member -> {
                    Involvement involvement = new Involvement();
                    involvement.setPayee(member);
                    involvement.setExpense(expense);
                    involvement.setWeight(payeeToWeightMapping.get(member.getId()));
                    involvements.add(involvement);
                }
        );

        if (involvements.size() != payeesIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, involvements.size() + " != " + payeesIds.size());
        }

        return involvements;
    }

    public ExpenseDetails revertLastChange(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ExpenseHistory previousVersion = expenseHistoryRepository.findFirstByOriginalExpense_IdOrderByTimestampDesc(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        Group group = expense.getGroup();
        String lastEditorName = previousVersion.getCreator() != null ? previousVersion.getCreator().getName() : null;
        String groupCreatorUsername = group.getCreator().getName();

        if (group.isArchived() ||
                group.isForRegisteredOnly() && lastEditorName!= null && !lastEditorName.equals(username) && !groupCreatorUsername.equals(username) ||
                lastEditorName != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        expense.setDescription(previousVersion.getDescription());
        expense.setAmount(previousVersion.getAmount());
        expense.setTimestamp(previousVersion.getTimestamp());
        expense.setPayer(previousVersion.getPayer());
        expense.setInvolvements(previousVersion.getInvolvements().stream()
                .map(i -> {
                    Involvement involvement = new Involvement();
                    involvement.setPayee(i.getPayee());
                    involvement.setWeight(i.getWeight());

                    return involvement;
                })
                .collect(Collectors.toSet()));
        expense.setCreator(previousVersion.getCreator());
        expense.setTimeCreated(previousVersion.getTimeCreated());

        repository.save(expense);
        expenseHistoryRepository.delete(previousVersion);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    public void deleteExpense(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (expense.getGroup().isArchived()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        ExpenseHistory firstVersion = expenseHistoryRepository.findFirstByOriginalExpense_IdOrderByTimestampAsc(expenseId);
        UserEntity originalCreator = firstVersion == null ? expense.getCreator() : firstVersion.getCreator();
        String groupCreatorUsername = expense.getGroup().getCreator().getName();

        if (originalCreator != null && !originalCreator.getName().equals(username) && !groupCreatorUsername.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        repository.deleteById(expenseId);
    }


}
