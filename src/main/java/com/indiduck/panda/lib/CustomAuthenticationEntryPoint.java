package com.indiduck.panda.lib;

import com.indiduck.panda.Service.JwtUserDetailsService;
import com.indiduck.panda.domain.ErrorType;
import com.indiduck.panda.domain.dto.Response;
import com.indiduck.panda.domain.dto.UserResponseDto;
import com.indiduck.panda.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JwtUserDetailsService jwtUserDetailsService;
    private final Response res;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String exception = (String)request.getAttribute("exception");
        ErrorType errorType;
//        System.out.println(" =access재생성로직 시작 ");

//        /**
//         * 토큰이 없는 경우 예외처리
//         */
        if(exception == null) {
            errorType = ErrorType.UNAUTHORIZEDException;
            setResponse(response, errorType);
//            System.out.println("토큰이 없습니다");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);


            return;
        }

        /**
         * 토큰이 만료된 경우 예외처리
         */
        //malformed
        if(exception.equals("MalformedJwtException"))
        {

            log.info("위조토큰");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;

        }
        //expried
        if(exception.equals("Expired")) {
            log.info("만료토큰");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }
        //unsuppoet
        if(exception.equals("Unsupported"))
        {
            log.info("지원하지 않는토큰");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }
        //ecveption
        if(exception.equals("empty"))
        {
            log.info("빈토큰");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }


    }

    private void setResponse(HttpServletResponse response, ErrorType errorType) throws IOException {
        JSONObject json = new JSONObject();
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");

//        json.put("code", errorType.getCode());
//        json.put("ermessage", errorType.getDescription());
//        response.getWriter().print(json);
    }
}