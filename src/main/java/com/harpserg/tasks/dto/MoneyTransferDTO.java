package com.harpserg.tasks.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class MoneyTransferDTO {

    private Money amount;
    private UUID from;
    private UUID to;

}
