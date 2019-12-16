package com.harpserg.tasks.service;

import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountFullDTO addAccount(AccountShortDTO accountShortDTO);

    List<AccountFullDTO> getAllAccounts();

    AccountFullDTO getAccount(UUID id);

    int deleteAll();
}
