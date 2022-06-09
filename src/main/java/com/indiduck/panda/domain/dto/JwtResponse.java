package com.indiduck.panda.domain.dto;

//- 응답 객체입니다.
//
//- 토큰을 반환합니다. 마찬가지로 커스터마이징 가능합니다.
import java.io.Serializable;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;

    public JwtResponse(String jwttoken) {
        this.jwttoken = jwttoken;
    }

    public String getToken() {
        return this.jwttoken;
    }
}