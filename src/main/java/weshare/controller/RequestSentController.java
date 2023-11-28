package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PersonDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.amountOf;

public class RequestSentController {
    public static UUID ExpenseId = null;

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        Collection<PaymentRequest> requests = expensesDAO.findPaymentRequestsSent(personLoggedIn);

        Map<String, Object> viewModel = new HashMap<>();
        MonetaryAmount total = amountOf(0);

        for (PaymentRequest pays : requests) {
            total = total.add(pays.getAmountToPay());
        }

        viewModel.put("requests", requests);
        viewModel.put("total", total);
        context.render("paymentrequests_sent.html", viewModel);
    };

    public static final Handler addRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);

        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String email = context.formParam("email");
        MonetaryAmount amount = amountOf(Integer.parseInt(context.formParamAsClass("amount", String.class).get()));
        LocalDate dueDate = LocalDate.parse(Objects.requireNonNull(context.formParamAsClass("due_date", String.class).get()));

        Expense expense = expensesDAO.findExpensesForPerson(personLoggedIn)
                .stream()
                .filter(expensee -> expensee.getId().equals(ExpenseId))
                .collect(Collectors.toList())
                .get(0);

        Optional<Person> personpaying = personDAO.findPersonByEmail(email);
        expense.requestPayment(personpaying.get(), amount, dueDate);
        expensesDAO.save(expense);

        context.redirect("/paymentrequest?expenseId=" + ExpenseId.toString());
    };

    public static final Handler showdetails = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String expenseID = context.queryParam("expenseId");
        UUID expenseId = UUID.fromString(Objects.requireNonNull(expenseID));
        setExpenseId(expenseId);

        Expense expense = expensesDAO.findExpensesForPerson(personLoggedIn)
                .stream()
                .filter(expensee -> expensee.getId().equals(expenseId))
                .collect(Collectors.toList())
                .get(0);

        Collection<PaymentRequest> requests = expense.listOfPaymentRequests();
        MonetaryAmount total = amountOf(0);

        for (PaymentRequest req : requests) {
            total = total.add(req.getAmountToPay());
        }

        HashMap<String, Object> details = new HashMap<>();
        details.put("expense", expense);
        details.put("requests", requests);
        details.put("total", total);

        context.render("requestsfrom.html", details);
    };

    public static void setExpenseId(UUID expenseId) {
        ExpenseId = expenseId;
    }
}
