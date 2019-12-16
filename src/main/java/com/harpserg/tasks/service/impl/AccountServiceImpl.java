package com.harpserg.tasks.service.impl;

import com.harpserg.tasks.domain.Account;
import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;
import com.harpserg.tasks.service.AccountService;
import com.harpserg.tasks.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final SessionFactory factory;

    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Override
    public AccountFullDTO addAccount(AccountShortDTO accountShortDTO) {
        Account account = accountMapper.toEntity(accountShortDTO);
        Session session = factory.openSession();
        session.beginTransaction();
        session.save(account);
        session.getTransaction().commit();
        return accountMapper.fromEntity(account);
    }

    @Override
    public List<AccountFullDTO> getAllAccounts() {
        Session session = factory.openSession();
        List<Account> accounts = session.createQuery("FROM Account").list();

        return accounts.stream().map(accountMapper::fromEntity).collect(Collectors.toList());
    }

    @Override
    public int deleteAll() {
        Session session = factory.openSession();
        Query deleteQuery = session.createQuery("delete FROM Account");
        session.beginTransaction();
        deleteQuery.executeUpdate();
        session.getTransaction().commit();
        return 1;
    }

    @Override
    public AccountFullDTO getAccount(UUID id) {
        Session session = factory.openSession();
        Account account = session.get(Account.class, id);

        return accountMapper.fromEntity(account);
    }
}
