package com.indiduck.panda.domain.dao;

//- 일종의 Dto입니다. 한번 감싸줌으로서 rare한 정보대신 가공된 정보를 받을 수 있도록 합니다.
//
//- 커스터마이징 가능하지만, 여기서는 로그인 기능이므로 아이디와 비밀번호만 필요합니다.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor //need default constructor for JSON Parsing
@AllArgsConstructor
public class JwtRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String username;
    private String password;
}