package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static weshare.model.DateHelper.TODAY;
import static weshare.model.MoneyHelper.amountOf;

public class RequestReceivedController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        Collection<PaymentRequest> requests = expensesDAO.findPaymentRequestsReceived(personLoggedIn);
        
        Map<String, Object> viewModel = new HashMap<>();
        MonetaryAmount total = amountOf(0);
        
        for (PaymentRequest pays : requests) {
            total = total.add(pays.getAmountToPay());
        }
        
        viewModel.put("requests", requests);
        viewModel.put("total", total);
        
        context.render("paymentrequests_received.html", viewModel);
    };

    public static final Handler pay = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String id = context.formParam("hidden");
        UUID pid = UUID.fromString(id);
        
        PaymentRequest request = expensesDAO.findPaymentRequestsReceived(personLoggedIn)
                .stream()
                .filter(req -> req.getId().equals(pid))
                .collect(Collectors.toList())
                .get(0);
        
        expensesDAO.save(request.pay(personLoggedIn, TODAY).getExpenseForPersonPaying());
        context.redirect(Routes.received);
    };
}
