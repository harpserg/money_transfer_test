package com.harpserg.tasks.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountFullDTO extends AccountShortDTO {

    private UUID id;

}
