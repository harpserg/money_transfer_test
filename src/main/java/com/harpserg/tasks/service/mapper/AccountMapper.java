package com.harpserg.tasks.service.mapper;

import com.harpserg.tasks.domain.Account;
import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface AccountMapper {

    @Mappings({@Mapping(target = "balance.cents", source = "balance")})
    AccountFullDTO fromEntity(Account account);

    @Mappings({@Mapping(target = "balance", source = "balance.cents")})
    Account toEntity(AccountFullDTO accountFullDTO);

    @Mappings({@Mapping(target = "balance", source = "balance.cents")})
    Account toEntity(AccountShortDTO accountDTO);

}
