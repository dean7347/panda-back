package com.indiduck.panda.Service;


//- UserDetailsService를 implements 해주었다는 것이 중요합니다.
//
//- DB에서 UserDetail를 얻어와 AuthenticationManager에게 제공하는 역할을 수행합니다.
//
//- 이번에는 DB 없이 하드코딩된 User List에서 get userDetail합니다.
//
//- password 부분이 해싱되어있는데, Spring Security 5.0에서는 Password를 BryptEncoder를 통해 Brypt화하여 저장하고 있습니다. 따라서 하드코딩해서 넣어줍니다.
//
//- https://www.javainuse.com/onlineBcrypt 에서 user_pw를 Bcrypt화할 수 있습니다.
//
//- id : user_id, pw: user_pw로 고정해 사용자 확인하고, 사용자 확인 실패시 throw Exception을 제공합니다.

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.indiduck.panda.Repository.DeliverAddressRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.config.ApiKey;
import com.indiduck.panda.domain.User;
import com.indiduck.panda.domain.dto.Response;
import com.indiduck.panda.domain.dto.UserDto;
import com.indiduck.panda.domain.UserType;
import com.indiduck.panda.domain.dto.UserRequestDto;
import com.indiduck.panda.domain.dto.UserResponseDto;
import com.indiduck.panda.jwt.JwtTokenProvider;
import com.indiduck.panda.util.SecurityUtil;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JwtUserDetailsService implements UserDetailsService {

    //	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		if ("user_id".equals(username)) {
//			return new User("user_id", "$2a$10$jCvWm3NXDRFs/EfuI4h4.u0ZxNocv.ZkgEy6qbjVXrfQ5.KzLfhAe",
//					new ArrayList<>());
//		} else {
//			throw new UsernameNotFoundException("User not found with username: " + username);
//		}
//	}
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final DeliverAddressService deliverAddressService;
    @Autowired
    private final DeliverAddressRepository deliverAddressRepository;
    //    @Autowired
//    private CookieUtil cookieUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ApiKey apiKey;

    //새로운버전
    private final Response response;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;


    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    /**
     * Spring Security 필수 메소드 구현
     *
     * @param email 이메일
     * @return UserDetails
     * @throws UsernameNotFoundException 유저가 없을 때 예외 발생
     */
    @Override // 기본적인 반환 타입은 UserDetails, UserDetails를 상속받은 UserInfo로 반환 타입 지정 (자동으로 다운 캐스팅됨)
    public User loadUserByUsername(String email) throws UsernameNotFoundException { // 시큐리티에서 지정한 서비스이기 때문에 이 메소드를 필수로 구현
//        System.out.println("email = " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException((email)));
    }

    /**
     * 비밀번호 검증
     */
    public User loginUser(String id, String password) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(id);
        User user = byEmail.get();
        if (user == null) throw new Exception("멤버가 없습니다");
        String passwordEncode = user.getPassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, passwordEncode);
        if(user.getLeaveAt()!=null)
        {
            return null;
        }
        if (matches) {
            return user;
        }
        return null;
    }


    /**
     * 회원정보 저장
     *
     * @param infoDto 회원정보가 들어있는 DTO
     * @return 저장되는 회원의 PK
     */
    @Transactional
    public String save(UserDto infoDto) {

        IamportClient client;
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = infoDto.getPhone();
        client = new IamportClient(test_api_key, test_api_secret);

        try {
            IamportResponse<Certification> certification_response = client.certificationByImpUid(test_imp_uid);
//            System.out.println("certification_response = " + certification_response.getResponse().getName());
//            System.out.println("certification_response = " + certification_response.getResponse().getPhone());
//            System.out.println("certification_response = " + certification_response.getResponse().getBirth());
//            System.out.println("certification_response = " + certification_response.getResponse().getBirth().getYear());
//            System.out.println("certification_response = " + certification_response.getResponse().isCertified());
//            System.out.println("certification_response = " + certification_response.getResponse().getUniqueKey());
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            LocalDate now = LocalDate.now();
            Date birth = certification_response.getResponse().getBirth();

            String format1 = format.format(birth);
            LocalDate parsedBirthDate = LocalDate.parse(format1, DateTimeFormatter.ofPattern("yyyyMMdd"));

            int americanAge = now.minusYears(parsedBirthDate.getYear()).getYear(); // (1)

            //중복 ci
            Optional<User> byCi = userRepository.findByCi(certification_response.getResponse().getUniqueKey());

            if(byCi.isPresent())
            {
                return "이미 가입된 회원 ID가 있습니다";
            }
            if (parsedBirthDate.plusYears(americanAge).isAfter(now)) {
                americanAge = americanAge - 1;
            }
            //19세이하 가입불가
            if (americanAge < 19) {
                return "만 19세 이하는 가입할 수 없습니다";
            }
            //인증실패
            if (!certification_response.getResponse().isCertified()) {
                return "인증실패";
            }

            if (userRepository.findByEmail(infoDto.getEmail()).isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                infoDto.setPassword(encoder.encode(infoDto.getPassword()));
                //만나이 계산

                userRepository.save(User.builder()
                        .email(infoDto.getEmail())
                        .auth(infoDto.getAuth())
                        .adult(infoDto.isAdult())
                        .apprterm(infoDto.isApprterm())
                        .userRName(certification_response.getResponse().getName())
                        .priagree(infoDto.isPriagree())
                        .userPhoneNumber(certification_response.getResponse().getPhone())
                        .ci(certification_response.getResponse().getUniqueKey())
                        .regAt(LocalDateTime.now())
                        .password(infoDto.getPassword())
                        .roles(Collections.singletonList(UserType.ROLE_USER.toString())).build()).getId();
                return "회원가입성공";

            }
            return "중복된 아이디가 존재합니다";

        } catch (IamportResponseException e) {
            System.out.println(e.getMessage());

            switch (e.getHttpStatusCode()) {
                case 401:
                    //TODO
                    return "ERR 401이 발생했습니다 해당 사항이 계속된다면 쇼핑호스트 판다로 문의부탁드립니다";


                case 500:
                    return "ERR 500이 발생했습니다 해당 사항이 계속된다면 쇼핑호스트 판다로 문의부탁드립니다";

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return "ERR E이 발생했습니다 해당 사항이 계속된다면 쇼핑호스트 판다로 문의부탁드립니다";

        }

        return null;

    }


    // TODO 테스트버전에서의 로그인

    @Transactional
    public String saveTEST(UserDto infoDto) {


        try {

            if (userRepository.findByEmail(infoDto.getEmail()).isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                infoDto.setPassword(encoder.encode(infoDto.getPassword()));
                //만나이 계산

                userRepository.save(User.builder()
                        .email(infoDto.getEmail())
                        .auth(infoDto.getAuth())
                        .adult(infoDto.isAdult())
                        .apprterm(infoDto.isApprterm())
                        .userRName("테스트버전용네임")
                        .priagree(infoDto.isPriagree())
                        .userPhoneNumber("01012345678")
                        .ci("testCICICI")
                        .regAt(LocalDateTime.now())
                        .password(infoDto.getPassword())
                        .roles(Collections.singletonList(UserType.ROLE_USER.toString())).build()).getId();
                return "회원가입성공";

            }
            return "중복된 아이디가 존재합니다";

        } catch (Exception e) {
            System.out.println("e = " + e);
        }

        return null;

    }

    public User findId(String code) throws IamportResponseException, IOException {
        IamportClient client;
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = code;
        client = new IamportClient(test_api_key, test_api_secret);
        IamportResponse<Certification> certification_response = client.certificationByImpUid(test_imp_uid);
        String uniqueKey = certification_response.getResponse().getUniqueKey();
        Optional<User> byCi = userRepository.findByCi(uniqueKey);
        return byCi.get();

    }

    @Transactional
    public User changePw(String code, String pw) throws IamportResponseException, IOException {
        IamportClient client;
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = code;
        client = new IamportClient(test_api_key, test_api_secret);
        IamportResponse<Certification> certification_response = client.certificationByImpUid(test_imp_uid);
        String uniqueKey = certification_response.getResponse().getUniqueKey();
        Optional<User> byCi = userRepository.findByCi(uniqueKey);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        byCi.get().setPassword(encoder.encode(pw));
        return byCi.get();

    }

    //새로운버전의 로그인
    public ResponseEntity<?> login(UserRequestDto.Login login) {

        if (userRepository.findByEmail(login.getEmail()).orElse(null) == null) {
            return response.fail("해당하는 유저가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        Optional<User> byEmail = userRepository.findByEmail((login.getEmail()));
        if(byEmail.get().getRegAt()!=null)
        {
            return response.fail("탈퇴된 유저입니다", HttpStatus.BAD_REQUEST);

        }

        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = login.toAuthentication();

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        UserResponseDto.TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return response.success(tokenInfo, "로그인에 성공했습니다.", HttpStatus.OK);
    }

    public UserResponseDto.TokenInfo loginV2(UserRequestDto.Login login) throws Exception {

        User user = userDetailsService.loginUser(login.getEmail(), login.getPassword());
        if(user ==null)
        {
            return null;

        }
        if (userRepository.findByEmail(login.getEmail()).orElse(null) == null) {
            return null;
        }

        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = login.toAuthentication();

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        UserResponseDto.TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return tokenInfo;
    }


    public ResponseEntity<?> logout(UserRequestDto.Logout logout) {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(logout.getAccessToken())) {
            return response.fail("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(logout.getAccessToken());

        // 3. Redis 에서 해당 User email 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(logout.getAccessToken());
        redisTemplate.opsForValue()
                .set(logout.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);

        return response.success("로그아웃 되었습니다.");
    }

    public boolean logoutV2(String atToken) {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(atToken)) {
            System.out.println("잘못된 로그아웃 요청 atToken오류 ");
            return false;
        }

        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(atToken);

        // 3. Redis 에서 해당 User email 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(atToken);
        redisTemplate.opsForValue()
                .set(atToken, "logout", expiration, TimeUnit.MILLISECONDS);

        return true;
    }

//    public UserResponseDto.TokenInfo reissueV2(String at, String rt) {
//        // 1. Refresh Token 검증
//        if (!jwtTokenProvider.validateToken(rt)) {
//            log.info("refreshToken 정보 유효하지 않음");
//            return null;
////            return response.fail("Refresh Token 정보가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
//        }
//
//        // 2. Access Token 에서 User email 을 가져옵니다.
//        Authentication authentication = jwtTokenProvider.getAuthentication(at);
//
//        // 3. Redis 에서 User email 을 기반으로 저장된 Refresh Token 값을 가져옵니다.
//        String refreshToken = (String)redisTemplate.opsForValue().get("RT:" + authentication.getName());
//        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
//        if(ObjectUtils.isEmpty(refreshToken)) {
//            log.info("잘못된요청");
//            return null;
//
////            return response.fail("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
//        }
//        System.out.println("refreshToken = " + refreshToken);
//        System.out.println("rt = " + rt);
//        if(!refreshToken.equals(rt)) {
//            log.info("refreshToken 정보 유효하지 않음(저장된토큰과 다름)");
//            return null;
//
////            return response.fail("Refresh Token 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
//        }
//
//        // 4. 새로운 토큰 생성
//        UserResponseDto.TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
//
//        // 5. RefreshToken Redis 업데이트
//        redisTemplate.opsForValue()
//                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);
//
////        return response.success(tokenInfo, "Token 정보가 갱신되었습니다.", HttpStatus.OK);
//        return tokenInfo;
//    }

    public ResponseEntity<?> reissue(UserRequestDto.Reissue reissue) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(reissue.getRefreshToken())) {
            return response.fail("Refresh Token 정보가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(reissue.getAccessToken());

        // 3. Redis 에서 User email 을 기반으로 저장된 Refresh Token 값을 가져옵니다.
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authentication.getName());
        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if (ObjectUtils.isEmpty(refreshToken)) {
            return response.fail("잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
        }
        if (!refreshToken.equals(reissue.getRefreshToken())) {
            return response.fail("Refresh Token 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 4. 새로운 토큰 생성
        UserResponseDto.TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return response.success(tokenInfo, "Token 정보가 갱신되었습니다.", HttpStatus.OK);
    }


    public void deleteTempSet(User user)
    {
        user.setLeaveAt();

    }

    public void deleteUser(User user) {


        User user1 = user.deleteAll();
        deliverAddressRepository.deleteByUserName(user);
        userRepository.save(user1);

    }

    public UserResponseDto.TokenInfo reissueV2(String token, String rt) {
//         1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(rt)) {
            return null;
        }
        System.out.println("token = " + token);
        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // 3. Redis 에서 User email 을 기반으로 저장된 Refresh Token 값을 가져옵니다.
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authentication.getName());
        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if (ObjectUtils.isEmpty(refreshToken)) {
            return null;
        }
//        if(!refreshToken.equals(reissue.getRefreshToken())) {
//            return response.fail("Refresh Token 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
//        }

        // 4. 새로운 토큰 생성
        UserResponseDto.TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return tokenInfo;
    }


//    public ResponseEntity<?> authority() {
//        // SecurityContext에 담겨 있는 authentication userEamil 정보
//        String userEmail = SecurityUtil.getCurrentUserEmail();
//
//        User user = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new UsernameNotFoundException("No authentication information."));
//
//        // add ROLE_ADMIN
//        user.getRoles().add(Authority.ROLE_ADMIN.name());
//        userRepository.save(user);
//
//        return response.success();
//    }


}