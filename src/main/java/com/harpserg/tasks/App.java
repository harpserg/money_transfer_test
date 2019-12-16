package com.harpserg.tasks;

import com.google.gson.JsonSyntaxException;
import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;
import com.harpserg.tasks.dto.MoneyTransferDTO;
import com.harpserg.tasks.exception.AccountNotFoundException;
import com.harpserg.tasks.exception.BadRequestException;
import com.harpserg.tasks.exception.InsufficientFundsException;
import com.harpserg.tasks.exception.MoneyTransferConflictException;
import com.harpserg.tasks.service.AccountService;
import com.harpserg.tasks.service.MoneyService;
import com.harpserg.tasks.service.impl.AccountServiceImpl;
import com.harpserg.tasks.service.impl.MoneyServiceImpl;
import org.eclipse.jetty.http.HttpStatus;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.harpserg.tasks.converter.GsonConverter.gson;
import static com.harpserg.tasks.util.ControllerUtils.generateUri;
import static spark.Spark.get;
import static spark.Spark.post;

public class App {

    private static final String H_LOCATION = "Location";

    //TODO move exceptions handling to common exceptions handler, add validation and exception handlers to all REST methods
    public static void main(String[] args) {

        SessionFactory factory = new Configuration().configure().buildSessionFactory();
        AccountService accountService = new AccountServiceImpl(factory);
        MoneyService moneyService = new MoneyServiceImpl(factory);

        post("/account", (request, response) -> {
            response.type("application/json");
            AccountShortDTO accountShortDTO = gson.fromJson(request.body(), AccountShortDTO.class);
            AccountFullDTO accountFullDTO = accountService.addAccount(accountShortDTO);
            if (accountFullDTO != null) {
                response.status(HttpStatus.CREATED_201);
                response.header(H_LOCATION, generateUri(request, accountFullDTO.getId()));
            } else {
                response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return gson.toJson(accountFullDTO);
        });

        get("/account/all", (request, response) -> {
            response.type("application/json");
            response.status(HttpStatus.OK_200);
            return gson.toJson(accountService.getAllAccounts());
        });

        get("/account/:id", (request, response) -> {
            response.type("application/json");
            response.status(HttpStatus.OK_200);
            return gson.toJson(accountService.getAccount(UUID.fromString(request.params(":id"))));
        });

        post("/money/transfer", (request, response) -> {
            response.type("application/json");
            try {
                MoneyTransferDTO moneyTransferDTO = gson.fromJson(request.body(), MoneyTransferDTO.class);
                moneyService.transferMoney(moneyTransferDTO);
            } catch (AccountNotFoundException e) {
                response.status(HttpStatus.NOT_FOUND_404);
                return e.getMessage();
            } catch (InsufficientFundsException e) {
                response.status(HttpStatus.PAYMENT_REQUIRED_402);
                return e.getMessage();
            } catch (MoneyTransferConflictException e) {
                response.status(HttpStatus.SERVICE_UNAVAILABLE_503);
                return e.getMessage();
            } catch (JsonSyntaxException | BadRequestException | IllegalArgumentException e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return e.getMessage();
            } catch (RuntimeException e) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return "Internal server error!";
            }

            response.status(HttpStatus.NO_CONTENT_204);
            return "";
        });

        get("/status", (request, response) -> {
            response.status(HttpStatus.OK_200);
            return "ok";
        });

    }

}
