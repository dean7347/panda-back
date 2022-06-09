package com.indiduck.panda.domain.dao;

import lombok.Data;

@Data
public  class TFMessageDto {
    boolean success;
    String message;

    public TFMessageDto(boolean tf, String me)
    {
        this.success=tf;
        this.message =me;
    }

}
