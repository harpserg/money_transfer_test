package com.harpserg.tasks.service.impl;

import com.harpserg.tasks.domain.Account;
import com.harpserg.tasks.dto.MoneyTransferDTO;
import com.harpserg.tasks.exception.AccountNotFoundException;
import com.harpserg.tasks.exception.BadRequestException;
import com.harpserg.tasks.exception.InsufficientFundsException;
import com.harpserg.tasks.exception.MoneyTransferConflictException;
import com.harpserg.tasks.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import java.util.List;

@RequiredArgsConstructor
public final class MoneyServiceImpl implements MoneyService {

    private static final Logger log = LoggerFactory.getLogger(MoneyServiceImpl.class);
    private final SessionFactory factory;
    private final int MAX_ATTEMPTS = 50;//should never happened

    @Override
    public void transferMoney(MoneyTransferDTO moneyTransferDTO) {

        validateRequest(moneyTransferDTO);

        boolean success = false;
        Session session = null;
        int attempt = 0;

        do {
            log.info(String.format("%d:attempt:%d -> from:%s, to:%s, amount:%d", Thread.currentThread().getId(), ++attempt, moneyTransferDTO.getFrom(), moneyTransferDTO.getTo(), moneyTransferDTO.getAmount().getCents()));

            if (attempt >= MAX_ATTEMPTS) {
                throw new MoneyTransferConflictException("Your transaction couldn't be completed. Please try again later.");
            }

            try {

                session = factory.openSession();
                session.beginTransaction();
                Query query = session.createQuery("from Account where id in(:from, :to)");
                query.setParameter("from", moneyTransferDTO.getFrom());
                query.setParameter("to", moneyTransferDTO.getTo());
                query.setLockMode(LockModeType.OPTIMISTIC);
                List<Account> accounts = query.getResultList();

                if (accounts.size() != 2)
                    throw new AccountNotFoundException(String.format("Some of accounts (%s, %s) couldn't be found!", moneyTransferDTO.getFrom(), moneyTransferDTO.getTo()));

                Account from = accounts.get(0).getId().equals(moneyTransferDTO.getFrom()) ? accounts.get(0) : accounts.get(1);
                Account to = accounts.get(0).getId().equals(moneyTransferDTO.getTo()) ? accounts.get(0) : accounts.get(1);

                if (from.getBalance() < moneyTransferDTO.getAmount().getCents())
                    throw new InsufficientFundsException(String.format("Account %s has insufficient funds: expected %d, available %d", from.getId(), from.getBalance(), moneyTransferDTO.getAmount().getCents()));

                from.setBalance(from.getBalance() - moneyTransferDTO.getAmount().getCents());
                to.setBalance(to.getBalance() + moneyTransferDTO.getAmount().getCents());

                session.getTransaction().commit();
                success = true;

            } catch (OptimisticLockException e) {
                log.info("OptimisticLock occurs: " + e.getMessage());
            } finally {
                session.close();
            }

        } while (!success);
    }

    private void validateRequest(MoneyTransferDTO moneyTransferDTO) {
        if (moneyTransferDTO.getAmount().getCents() <= 0)
            throw new BadRequestException("Amount must be grater then 0");
        if (moneyTransferDTO.getFrom() == null)
            throw new BadRequestException("'From' must be not null UUID");
        if (moneyTransferDTO.getTo() == null)
            throw new BadRequestException("'To' must be not null UUID");

    }

}
