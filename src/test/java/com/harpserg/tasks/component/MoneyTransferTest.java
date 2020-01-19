package com.harpserg.tasks.component;


import com.github.javafaker.Faker;
import com.harpserg.tasks.component.helper.TestHelper;
import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;
import com.harpserg.tasks.dto.Money;
import com.harpserg.tasks.service.AccountService;
import com.harpserg.tasks.service.impl.AccountServiceImpl;
import com.harpserg.tasks.service.impl.MoneyServiceImpl;
import okhttp3.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.harpserg.tasks.component.helper.TestHelper.createMoneyTransferRequest;
import static com.harpserg.tasks.component.helper.TestHelper.getRandomNumberInRange;
import static org.junit.Assert.assertEquals;

public class MoneyTransferTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(MoneyServiceImpl.class);
    private SessionFactory factory = new Configuration().configure().buildSessionFactory();
    private AccountService accountService = new AccountServiceImpl(factory);
    Faker faker = new Faker();

    @Test
    public void transferUnknownAccount_ShouldReturnNotFoundHeader() throws IOException {
        Request request = createMoneyTransferRequest(UUID.randomUUID(), UUID.randomUUID(), 100);

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(404, response.code());
    }

    @Test
    public void unknownEndpointCall_ShouldReturnNotFoundHeader() throws IOException {
        String transferRequest = "{\"someData\":\"some data value\"}";

        RequestBody body = RequestBody.create(transferRequest, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(TestHelper.REST_API_BASE + "/money/unknown_endpoint")
                .post(body)
                .build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(404, response.code());
    }

    @Test
    public void transferRequestWithInvalidJsonPayload_ShouldReturnBadRequestHeader() throws IOException {
        String transferRequest = "{invalidData}";

        RequestBody body = RequestBody.create(transferRequest, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(TestHelper.REST_API_BASE + "/money/transfer")
                .post(body)
                .build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(400, response.code());
    }

    @Test
    public void transferWithNegativeAmountValue_ShouldReturnBadRequestHeader() throws IOException {

        AccountShortDTO from = new AccountShortDTO();
        from.setBalance(new Money(100));
        from.setOwnerName(faker.name().fullName());
        UUID fromUUID = accountService.addAccount(from).getId();

        AccountShortDTO to = new AccountShortDTO();
        to.setBalance(new Money(100));
        to.setOwnerName(faker.name().fullName());
        UUID toUUID = accountService.addAccount(to).getId();

        Request request = createMoneyTransferRequest(fromUUID, toUUID, -20);

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(400, response.code());
    }

    @Test
    public void transferWithoutEnoughFunds_ShouldReturnPaymentRequiredHeader() throws IOException {
        AccountShortDTO from = new AccountShortDTO();
        from.setBalance(new Money(100));
        from.setOwnerName(faker.name().fullName());
        UUID fromUUID = accountService.addAccount(from).getId();

        AccountShortDTO to = new AccountShortDTO();
        to.setBalance(new Money(100));
        to.setOwnerName(faker.name().fullName());
        UUID toUUID = accountService.addAccount(to).getId();

        Request request = createMoneyTransferRequest(fromUUID, toUUID, 200);

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(402, response.code());
    }

    @Test
    public void transferAllAvailableMoney_ShouldBePositive() throws IOException {
        final long availableAmount = 100;

        AccountShortDTO from = new AccountShortDTO();
        from.setBalance(new Money(availableAmount));
        from.setOwnerName(faker.name().fullName());
        UUID fromUUID = accountService.addAccount(from).getId();

        AccountShortDTO to = new AccountShortDTO();
        to.setBalance(new Money(0));
        to.setOwnerName(faker.name().fullName());
        UUID toUUID = accountService.addAccount(to).getId();

        Request request = createMoneyTransferRequest(fromUUID, toUUID, availableAmount);

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(204, response.code());
        assertEquals(0, accountService.getAccount(fromUUID).getBalance().getCents());
        assertEquals(availableAmount, accountService.getAccount(toUUID).getBalance().getCents());
    }

    @Test
    public void transfersTwoAccountsOneDirection_ShouldNotBlock_ShouldKeepConstantTotalAmount_ShouldHaveConsistentState() throws InterruptedException {
        int transfersCount = 999;
        int transferAmount = 10;
        long initialBalance = 10000;
        long accountsCount = 2;
        List<UUID> ids = new ArrayList<>();

        for (int i = 0; i < accountsCount; i++) {
            AccountShortDTO accountShortDTO = new AccountShortDTO();
            accountShortDTO.setBalance(new Money(initialBalance));
            accountShortDTO.setOwnerName(faker.name().fullName());
            ids.add(accountService.addAccount(accountShortDTO).getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Callable<Response>> requests = new ArrayList<>(transfersCount);

        UUID fromId = ids.get(0);
        UUID toId = ids.get(1);

        long fromStartBalance = accountService.getAccount(fromId).getBalance().getCents();
        long toStartBalance = accountService.getAccount(toId).getBalance().getCents();

        for (int i = 0; i < transfersCount; i++) {
            Call call = httpClient.newCall(createMoneyTransferRequest(fromId, toId, transferAmount));
            requests.add(() -> call.execute());
        }

        executor.invokeAll(requests);

        long totalBalance = accountService.getAllAccounts().stream().map(a -> a.getBalance().getCents()).reduce(0L, Long::sum);
        assertEquals(accountsCount * initialBalance, totalBalance);
        assertEquals(fromStartBalance - transfersCount * transferAmount, accountService.getAccount(fromId).getBalance().getCents());
        assertEquals(toStartBalance + transfersCount * transferAmount, accountService.getAccount(toId).getBalance().getCents());

        accountService.getAllAccounts().forEach(account -> log.info(account.getOwnerName() + " - " + account.getBalance().getCents()));
        accountService.deleteAll();
    }

    @Test
    public void randomTransfers_ShouldNotBlock__ShouldKeepConstantTotalAmount_ShouldHaveConsistentState() throws InterruptedException {
        int transfersCount = 1000;
        int maxTransferAmount = 10;
        long initialBalance = 10000;
        long accountsCount = 20;
        List<UUID> ids = new ArrayList<>();

        if (maxTransferAmount > initialBalance / transfersCount) {
            throw new IllegalArgumentException("maxTransferAmount must not be greater than initialBalance / transfersCount. "
                    + "We have to avoid situation, when account has insufficient funds in this test. "
                    + "Otherwise we can't predict expectedBalance state at the end of transfers, because we can't predict transfers processing order.");
        }

        for (int i = 0; i < accountsCount; i++) {
            AccountShortDTO accountShortDTO = new AccountShortDTO();
            accountShortDTO.setBalance(new Money(initialBalance));
            accountShortDTO.setOwnerName(faker.name().fullName());
            ids.add(accountService.addAccount(accountShortDTO).getId());
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Callable<Response>> requests = new ArrayList<>(transfersCount);

        Map<UUID, Long> startBalance = accountService.getAllAccounts().stream().collect(Collectors.toMap(AccountFullDTO::getId, a -> a.getBalance().getCents()));

        Map<UUID, Long> expectedBalance = new HashMap<>(startBalance);

        for (int i = 0; i < transfersCount; i++) {

            UUID from = ids.get(getRandomNumberInRange(0, ids.size() - 1));
            UUID to = ids.get(getRandomNumberInRange(0, ids.size() - 1));
            long amount = getRandomNumberInRange(1, maxTransferAmount);

            if (expectedBalance.get(from) >= amount) {
                expectedBalance.put(from, expectedBalance.get(from) - amount);
                expectedBalance.put(to, expectedBalance.get(to) + amount);
            }

            Call call = httpClient.newCall(createMoneyTransferRequest(from, to, amount));

            requests.add(() -> call.execute());
        }

        executor.invokeAll(requests);

        long totalBalance = accountService.getAllAccounts().stream().map(a -> a.getBalance().getCents()).reduce(0L, Long::sum);
        assertEquals(accountsCount * initialBalance, totalBalance);

        Map<UUID, Long> finishBalance = accountService.getAllAccounts().stream().collect(Collectors.toMap(AccountFullDTO::getId, a -> a.getBalance().getCents()));

        for (Map.Entry<UUID, Long> entry : expectedBalance.entrySet()) {
            assertEquals(entry.getValue(), finishBalance.get(entry.getKey()));
        }

        accountService.getAllAccounts().forEach(account -> log.info(account.getOwnerName() + " - " + account.getBalance().getCents()));
        accountService.deleteAll();
    }


}
