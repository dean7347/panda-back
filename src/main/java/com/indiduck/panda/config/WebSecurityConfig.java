package com.indiduck.panda.config;



import com.indiduck.panda.jwt.JwtAuthenticationFilter;
import com.indiduck.panda.jwt.JwtTokenProvider;
import com.indiduck.panda.lib.CustomAuthenticationEntryPoint;
import com.indiduck.panda.lib.WebAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final WebAccessDeniedHandler webAccessDeniedHandler;
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic().disable()
//                .csrf().disable()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).
                and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers( "/favicon.ico",    "/css/**",  "/fonts/**", "/img/**",  "/js/**",
                        "/api/reissue","/api/reissueV2","/api/reissuev2","/api/preview**","/api/product/products_by**","/api/getpandas_by_id**","/api/ispanda**","api/proxy?url=**","/api/getqna?**",
                        "/api/getqna*","/api/login","/api/loginv2","/api/user/logoutv2","/api/signup","/api/userresign","/api/ispanda","/api/userauth","/api/getreview").permitAll()
//                .antMatchers("/api**").permitAll()
                .antMatchers("/api/*","/api/shop/**","/api/cart/**","/api/payment/**","api/readRefundRequest","/api/copyproduct","/api/pandamovieedit","/api/haveshop",
                        "/product/api/proxy?url=/http://localhost:3000/**","/api/userprivateedit","/api/editShop","/api/editPanda","/api/editLow","/api/createqna","/api/createqnareply"
                ,"/api/product/changeprostatus","/api/addpropanda","/api/partialCancel","/api/confirmtrade","/api/rejecttrade","/api/createreview").authenticated()


                .antMatchers("/api/**","/api/shop/**").hasRole("USER")
                .antMatchers("/api/**","/api/admin/**","/api/admin/confirmregpanda","/api/admin/confirmregshop","/api/admin/pandaSettleConfirm").hasRole("ADMIN")
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(webAccessDeniedHandler)
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);
        // JwtAuthenticationFilter를 UsernamePasswordAuthentictaionFilter 전에 적용시킨다.
    }

    // 암호화에 필요한 PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//
////- 마지막으로 스프링 시큐리티를 위한 설정을 하겠습니다.
////
////- WebSecurity와 HttpSecurity를 커스터마이징하는 단계입니다. 비밀번호를 인코딩하기 위해 필요한 빈 설정을 하고, jwt로 스프링 시큐리티를 이용하겠다는 설정을 진행합니다.
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    @Autowired
//    private UserDetailsService jwtUserDetailsService;
//
//    @Autowired
//    private JwtRequestFilter jwtRequestFilter;
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        // configure AuthenticationManager so that it knows from where to load
//        // user for matching credentials
//        // Use BCryptPasswordEncoder
//        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        // We don't need CSRF for this example
//        httpSecurity.csrf().disable()
//                // dont authenticate this particular request
//                .authorizeRequests().antMatchers("/**","/createShop","/haveshop","/api/shop/dashboard","/api/shop/dashboard/orderStatus","/api/shop/confirm","/createFile","/regnewproduct",
//                "/api/preview","/api/searchpreview","/api/product/products_by_id","/api/addpropanda","/api/getpandas_by_id","/api/ispanda","/api/regpanda"
//                ,"/api/addcart","/api/mycart","/api/payment","/api/payment/complete","/api/test2","/api/test","/authenticate","/signup","/user/logout","/auth/check"
//                ,"/api/addaddress","/api/myaddress","/api/deleteaddr","/product/api/proxy", //여기까지 걍추가함
//                "/shop/**","/authenticate","/signup","/auth/check","/logout","/test","/test2","/createFile", "/index.html",
//                "/favicon.ico",    "/css/**",  "/fonts/**", "/img/**",  "/js/**" ).permitAll().
//                //인증된 회원만
//                // all other requests need to be authenticated
//                        anyRequest().authenticated().and().
//                // make sure we use stateless session; session won't be used to
//                // store user's state.
//                        exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//        // Add a filter to validate the tokens with every request
//        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//    }
//}