package com.indiduck.panda.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String email;
    private String password;
    private String phone;
    private String name;
    private boolean adult;
    private boolean apprterm;
    private boolean priagree;
    private String auth;
}