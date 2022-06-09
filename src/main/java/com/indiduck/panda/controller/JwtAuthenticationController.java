package com.indiduck.panda.controller;


//- MVC 패턴을 알고 있다면 web 부분과 연결되는 것이 Controller라는 사실을 알고 있을 것입니다.
//
//이제 컨트롤러를 작성해보겠습니다.
//
//- JwtRequest를 Json 형식으로 받았다면, 인증을 통해 토큰을 발급해주는 기능을 작성하였습니다.

import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.RedisUtil;
import com.indiduck.panda.Service.JwtUserDetailsService;
import com.indiduck.panda.domain.Panda;
import com.indiduck.panda.domain.Shop;
import com.indiduck.panda.domain.User;
import com.indiduck.panda.domain.UserOrder;
import com.indiduck.panda.domain.dao.JwtRequest;
import com.indiduck.panda.domain.dao.TFMessageDto;
import com.indiduck.panda.domain.dto.*;
import com.indiduck.panda.jwt.JwtTokenProvider;
import com.indiduck.panda.lib.Helper;
import com.indiduck.panda.util.ApiResponseMessage;
import com.siot.IamportRestClient.exception.IamportResponseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.springframework.http.ResponseEntity.status;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationController {


    private final Response response;


    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
@Autowired
private final JwtTokenProvider jwtTokenProvider;

//    @Autowired
//    private CookieUtil cookieUtil;

    @Autowired
    private RedisUtil redisUtil;

    //    //로그인
//    @RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
//    public  ResponseEntity<ApiResponseMessage> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest, HttpServletRequest req,
//                                                                         HttpServletResponse res) throws Exception {
//
//        User user = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
//        if(user!=null){
//            System.out.println("비밀번호 검증");
//            User verfiyed = userDetailsService.loginUser(authenticationRequest.getUsername(), authenticationRequest.getPassword());
//            if(verfiyed!=null)
//            {
//                try {
//
//                    final String token = jwtTokenUtil.generateToken(user);
//                    final String refreshJwt = jwtTokenUtil.generateRefreshToken(user);
//                    Cookie accessToken = cookieUtil.createCookie(jwtTokenUtil.ACCESS_TOKEN_NAME, token);
//                    Cookie refreshToken = cookieUtil.createCookie(jwtTokenUtil.REFRESH_TOKEN_NAME, refreshJwt);
//                    redisUtil.setDataExpire(refreshJwt, user.getUsername(), jwtTokenUtil.REFRESH_TOKEN_VALIDATION_SECOND);
//                    res.addCookie(accessToken);
//                    res.addCookie(refreshToken);
////                    ApiResponseMessage message = new ApiResponseMessage("Success", "로그인성공", "", "");
//                    ApiResponseMessage message = new ApiResponseMessage("Success", "로그인성공", token, refreshJwt);
//
//                    return new ResponseEntity<ApiResponseMessage>(message,HttpStatus.OK);
//
//                } catch (Exception e) {
////            return new Response("authError", "로그인에 실패했습니다.", e.getMessage());
//                    ApiResponseMessage message = new ApiResponseMessage("Authentification Error", "로그인실패", "", "");
//                    System.out.println("message = " + message);
//                    return new ResponseEntity<ApiResponseMessage>(message,HttpStatus.UNAUTHORIZED);
//
//                }
//            }
//        }
//        ApiResponseMessage message = new ApiResponseMessage("Authentification Error", "로그인실패", "", "");
//
//        return new ResponseEntity<ApiResponseMessage>(message,HttpStatus.UNAUTHORIZED);
//
//
//    }
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Validated UserRequestDto.Login login, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineErrors(errors));
        }


        return userDetailsService.login(login);
    }

    @PostMapping("/api/loginv2")
    public ResponseEntity<?> loginV2(@Validated UserRequestDto.Login login,HttpServletRequest req, HttpServletResponse res, Errors errors) throws Exception {
        // validation check
        log.info(login.getEmail()+"의 로그인 시도");
        if (errors.hasErrors()) {
            return response.invalidFields(Helper.refineErrors(errors));
        }
        UserResponseDto.TokenInfo tokenInfo = userDetailsService.loginV2(login);
        if(tokenInfo==null)
        {
            return ResponseEntity.ok(new TFMessageDto(false,"로그인에 실패했습니다"));
        }
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = login.toAuthentication();
        String name = usernamePasswordAuthenticationToken.getName();
        Cookie refreshToken = new Cookie("refreshToken", null);
        refreshToken.setMaxAge(2147483647);
        refreshToken.setPath("/");
        Cookie newAccessToken = new Cookie("accessToken", null);
        newAccessToken.setMaxAge(2147483647);
        newAccessToken.setPath("/");

        Optional<User> byEmail = userRepository.findByEmail(name);
        String rToken = tokenInfo.getRefreshToken();
        String aToken = tokenInfo.getAccessToken();
        Cookie rfreshToken = new Cookie("refreshToken", rToken);
        rfreshToken.setHttpOnly(true);
        rfreshToken.setMaxAge(2147483647);

        rfreshToken.setPath("/");
        res.addCookie(rfreshToken);

        Cookie AccessToken = new Cookie("accessToken", aToken);
        AccessToken.setHttpOnly(true);
        AccessToken.setPath("/");
        AccessToken.setMaxAge(2147483647);

        res.addCookie(AccessToken);



        return ResponseEntity.ok(new TokenLoginVTAO(true, aToken, byEmail.get()));

    }


    //회원가입
    @PostMapping("/api/signup")
    public ResponseEntity<?> signup(@RequestBody UserDto infoDto) { // 회원 추가
//        System.out.println("infoDto = " + infoDto.getEmail());


        try {
            String save = userDetailsService.save(infoDto);
            if (!save.equals("회원가입성공")) {
                return ResponseEntity.ok(new signupDto(false, save));

            }

        } catch (Exception e) {
            return ResponseEntity.ok(new signupDto(false, "회원가입에 실패했습니다 같은 오류가 반복될경우 고객센터에 문의남겨주시기 바랍니다"));

        }
        return ResponseEntity.ok(new signupDto(true, "회원가입에성공했습니다"));
    }


    //아이디/비밀번호찾기
    @PostMapping("/api/findid")
    public ResponseEntity<?> findId(@RequestBody findId findId) throws IamportResponseException, IOException { // 회원 추가
        String code = findId.code;
        User id = userDetailsService.findId(code);

        return ResponseEntity.ok(new findIdDto(true, id.getId(), id.getEmail()));
    }

    //비밀번호 변경
    @PostMapping("/api/changepw")
    public ResponseEntity<?> changepw(@RequestBody ChangePw changePw) throws IamportResponseException, IOException { // 회원 추가
        log.info(changePw.code+"의 비밀번호 변경 요청");
        userDetailsService.changePw(changePw.code, changePw.pw);
        return ResponseEntity.ok(new TFMessageDto(true, "비밀번호 변경 성공"));
    }
    //체크
//    @RequestMapping(path = "/api/user/logout", method = RequestMethod.GET)
//    private void removeCookies(HttpServletRequest request, HttpServletResponse response) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        SecurityContextHolder 클리어시켜볼까?
//        System.out.println(" 로그아웃 ");
//        Cookie[] cookies = request.getCookies();
//
//        for(Cookie c : cookies) {
//            System.out.println("쿠키네임"+(c.getName()));  // 쿠키 이름 가져오기
//            System.out.println("쿠키밸류"+(c.getValue()));  // 쿠키 값 가져오기
//        }
//
//        if(authentication !=null)
//        {
//            new SecurityContextLogoutHandler().logout(request,response,authentication);
//        }
//
//
//    }

    @RequestMapping(path = "/api/user/logoutv2", method = RequestMethod.POST)
    public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse res) {
//        Cookie[] cookies = req.getCookies();
//        String atToken="";
//        for(Cookie c : cookies) {
//            if(c.getName().equals("accessToken"))
//            {
//                atToken=c.getValue();
//            }
//        }
        String atCookie="";
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("accessToken"))
                atCookie = cookie.getValue();
        }
        String atToken = atCookie;
//        if (errors.hasErrors()) {
//            return response.invalidFields(Helper.refineErrors(errors));
//        }
        boolean b = userDetailsService.logoutV2(atToken);
        if (b) {
            log.info("로그아웃성공");
            return ResponseEntity.ok(new TFMessageDto(b, "로그아웃 성공"));

        }
        log.error("로그아웃 실패");
        return ResponseEntity.ok(new TFMessageDto(b, "로그아웃 실패"));


    }

//    @GetMapping("/api/auth/check")
//    @ResponseBody
//    public ResponseEntity<?> check(HttpServletRequest request,
//                                   @CookieValue(name = "accessToken") String usernameCookie){
//
//        String usernameFromToken = jwtTokenUtil.getUsername(usernameCookie);
//        if(usernameFromToken !=null)
//        {
//            System.out.println("usernameFromToken = " + usernameFromToken);
//            return ResponseEntity.status(HttpStatus.OK).body(new SimpleCheckDto(usernameFromToken));
//        }
////
////
////
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("신뢰할수 없는 정보입니다 ");
//    }

    @RequestMapping(value = "/api/userauth", method = RequestMethod.POST)
    public ResponseEntity<?> shopDashBoardForOrderNumber(@CurrentSecurityContext(expression = "authentication")
                                                                 Authentication authentication) throws Exception {

        log.info(authentication + "의 유저 어스 요청");
        try {
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Shop shop = byEmail.get().getShop();
            Panda panda = byEmail.get().getPanda();
            boolean isShop = false;
            boolean isPanda = false;
            if (shop != null) isShop = true;
            if (panda != null) isPanda = true;
            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok(new RoleCheckDto(authentication.getName(), isShop, isPanda, false));

            }
            return ResponseEntity.ok(new RoleCheckDto(null, false, false, false));


        } catch (Exception e) {
            System.out.println("E = " + e);

            return ResponseEntity.ok(new RoleCheckDto("사용자정보없음", false, false, false));


        }
    }


//    @PostMapping("/api/reissue")
//    public ResponseEntity<?> reissue(@Validated UserRequestDto.Reissue reissue, Errors errors) {
//        // validation check
//        if (errors.hasErrors()) {
//            return response.invalidFields(Helper.refineErrors(errors));
//        }
//        return userDetailsService.reissue(reissue);
//    }

    @PostMapping("/api/reissuev2")
    public ResponseEntity<?> reissueV2(@CurrentSecurityContext(expression = "authentication")Authentication authentication,
                                                   HttpServletRequest req, HttpServletResponse res) {

//        log.info(authentication.getName() + "의 리이슈 신청");
//        if(authentication.getName().equals("anonymousUser"))
//        {
//            System.out.println(" 리이슈 실패 " );
//            return ResponseEntity.ok().body(new TFMessageDto(false, "재발급 실패1"));
//
//        }
        String rtCookie = "";
        String atCookie="";
        Cookie[] cookies = req.getCookies();
        System.out.println("cookies = " + cookies);
        if (cookies.length==0) {
            System.out.println("쿠키가없습니다");

            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken"))
                rtCookie = cookie.getValue();
            if (cookie.getName().equals("accessToken"))
                atCookie = cookie.getValue();
        }
//        System.out.println("기존쿡히"+atCookie);
        UserResponseDto.TokenInfo tokenInfo = userDetailsService.reissueV2(atCookie, rtCookie);
        if (tokenInfo == null) {
            return ResponseEntity.status(400).body(new TFMessageDto(false, "재발급 실패2"));

        }

        //
        System.out.println(" =재발급로직을 실행합니ㄷ=== ");

        //////로그인꺼 참고용
        //기존 쿡히 삭제
        String rToken = tokenInfo.getRefreshToken();
        String aToken = tokenInfo.getAccessToken();
        Cookie refreshToken = new Cookie("refreshToken", null);
        refreshToken.setPath("/");
        Cookie newAccessToken = new Cookie("accessToken", null);
        newAccessToken.setPath("/");

        //새로운쿡히
        refreshToken = new Cookie("refreshToken", rToken);
        refreshToken.setHttpOnly(true);
        refreshToken.setMaxAge(2147483647);

        refreshToken.setPath("/");

        newAccessToken = new Cookie("accessToken", aToken);
        newAccessToken.setHttpOnly(true);
        newAccessToken.setMaxAge(2147483647);

        newAccessToken.setPath("/");
        res.addCookie(refreshToken);
        res.addCookie(newAccessToken);
        System.out.println("가야대는쿡히 = " + tokenInfo.getAccessToken());
        System.out.println("변경쿡히"+newAccessToken.getValue());

        System.out.println(" =발송완료 ");
        /////
        String name = jwtTokenProvider.getAuthentication(aToken).getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Authentication auth = jwtTokenProvider.getAuthentication(aToken);
        System.out.println("auth = " + auth);


        return ResponseEntity.ok(new TokenLoginVTAO(true, aToken, byEmail.get()));


    }

//    @PostMapping("/api/logout")
//    public ResponseEntity<?> logoutv3(@Validated UserRequestDto.Logout logout, Errors errors) {
//        System.out.println("logout = " + logout.getAccessToken());
//        // validation check
//        if (errors.hasErrors()) {
//            return response.invalidFields(Helper.refineErrors(errors));
//        }
//        return userDetailsService.logout(logout);
//    }

//    @GetMapping("/authority")
//    public ResponseEntity<?> authority() {
//        log.info("ADD ROLE_ADMIN");
//        return userDetailsService.authority();
//    }

    @GetMapping("/api/userTest")
    public ResponseEntity<?> userTest() {
        log.info("ROLE_USER TEST");
        return response.success();
    }

    @GetMapping("/api/adminTest")
    public ResponseEntity<?> adminTest() {
        log.info("ROLE_ADMIN TEST");
        return response.success();
    }

    /*
    펑션
     */


    //    private void authenticate(String username, String password) throws Exception {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//        } catch (DisabledException e) {
//            System.out.println("user_disabled");
//            throw new Exception("USER_DISABLED", e);
//
//
//        } catch (BadCredentialsException e) {
//            System.out.println("invalid_credentials");
//
//            throw new Exception("INVALID_CREDENTIALS", e);
//        }
//    }
//
//    private ResponseEntity<String> authenticateV2(JwtRequest authenticationRequest) {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername()
//                    , authenticationRequest.getPassword()));
//        } catch (DisabledException e) {
//             System.out.println("user_disabled");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("회원가입 실패");
////            return ResponseEntity("no user",HttpStatus.CONFLICT)
//
//        } catch (BadCredentialsException e) {
//            System.out.println("invalid_credentials");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("신뢰할수 없는 정보입니다 ");
//        }
//        return null;
//    }
    //////////////dto///////////
    @Data
    private static class TokenLoginVTAO {
        boolean success;
        String accessToken;
        String userName="";
        boolean panda=false;
        boolean seller=false;
        boolean user=false;

        public TokenLoginVTAO(boolean success, String accessToken, User user) {
            this.success = success;
            this.accessToken = accessToken;
            this.userName=user.getUsername();
            if(user ==null)
            {
                this.user=false;
                this.panda=false;
                this.seller=false;
            }else
            {
                this.user=true;
            }
            if(user.getShop()!=null)
            {
                seller=true;
            }
            if(user.getPanda()!=null)
            {
                panda=true;
            }
        }
    }

    @Data
    private static class TokenLoginTAO {
        boolean success;
        String accessToken;


        public TokenLoginTAO(boolean success, String accessToken) {
            this.success = success;
            this.accessToken = accessToken;
        }
    }

    @Data
    static class findId {

        String code;


    }

    @Data
    static class ChangePw {

        String code;
        String pw;


    }

    @Data
    static class findIdDto {
        boolean success;
        long id;
        String email;

        public findIdDto(boolean success, long id, String email) {
            this.success = success;
            this.id = id;
            this.email = email;
        }
    }

    @Data
    static class RoleCheckDto {

        boolean isShop;
        boolean isPanda;
        boolean isAdmin;
        String userName;

        public RoleCheckDto(String un, boolean isShop, boolean isPanda, boolean isAdmin) {
            this.userName = un;
            this.isShop = isShop;
            this.isPanda = isPanda;
            this.isAdmin = isAdmin;
        }
    }

    @Data
    static class SimpleCheckDto {

        private String username;

        public SimpleCheckDto(String user) {

            this.username = user;

        }
    }

    @Data
    static class signupDto {
        private boolean success;
        private String message;

        public signupDto(boolean su, String me) {
            success = su;
            message = me;
        }
    }

    @Data
    static class loginResultDto {

        private boolean auth;
        private boolean authError;

        public loginResultDto(boolean auth, boolean authError) {
            this.auth = auth;
            this.authError = authError;

        }
    }
}
