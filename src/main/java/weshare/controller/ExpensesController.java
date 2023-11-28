package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.Expense;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.amountOf;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn)
                .stream()
                .filter(expense -> !expense.isFullyPaidByOthers())
                .collect(Collectors.toList());

        MonetaryAmount totalNettExpense = amountOf(0);

        for (Expense ex : expenses) {
            totalNettExpense = totalNettExpense.add(ex.amountLessPaymentsReceived());
        }

        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("expenses", expenses);
        viewModel.put("totalNettExpense", totalNettExpense);
        context.render("expenses.html", viewModel);
    };

    public static final Handler saveExpense = context -> {
        String description = context.formParam("description");
        MonetaryAmount amount = amountOf(Integer.parseInt(context.formParamAsClass("amount", String.class).get()));
        LocalDate date = LocalDate.parse(Objects.requireNonNull(context.formParamAsClass("date", String.class).get()));
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        Expense newExpense = new Expense(personLoggedIn, description, amount, date);

        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        expensesDAO.save(newExpense);

        context.redirect("/expenses");
    };

    public static final Handler showNewExpenseForm = context -> {
        context.render("new_expense.html");
    };
}
