package com.harpserg.tasks.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountShortDTO {

    private Money balance;
    private String ownerName;

}
