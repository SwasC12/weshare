package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";
    public static final String EXPENSES = "/expenses";
    public static final String SAVE_EXPENSE = "/save-expense";
    public static final String NEW_EXPENSE = "/newexpense";
    public static final String sent = "/paymentrequests_sent";
    public static final String received = "/paymentrequests_received";
    public static final String pay = "/paymentrequests_received.action";
    public static final String REDIRECT_TO_REQUEST = "/paymentrequest";
    public static final String SEND_REQUEST = "/paymentrequest.action";



    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,  PersonController.login);
            get(LOGOUT,         PersonController.logout);
            get(EXPENSES,           ExpensesController.view);
            get(NEW_EXPENSE, ExpensesController.showNewExpenseForm);
            get(sent, RequestSentController.view);
            get(received, RequestReceivedController.view );
            post(SAVE_EXPENSE, ExpensesController.saveExpense);
            get(REDIRECT_TO_REQUEST, RequestSentController.showdetails);
            post(SEND_REQUEST, RequestSentController.addRequest);
            post(pay, RequestReceivedController.pay);
        });
    }
}
