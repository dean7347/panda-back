package com.indiduck.panda.domain.dto;

import lombok.Data;

@Data
public class ResultDto {
    boolean success;
    String message;
    public ResultDto(boolean b, String message) {
        success=b;
        this.message=message;
    }
}