//package com.indiduck.panda.config;
//
////- 중요한 부분입니다. 프론트엔드에서 매순간 request를 요청 할 때마다 이 필터를 먼저 거치도록 해야합니다. 안그러면  인증되지 않은 사용자가 정보를 얻을 수도 있으니까요!
////
////- 헤더에 담겨온 토큰정보가 유효한지 확인하고, 맞다면 요청사항을 수행해줍니다.
//import java.io.IOException;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.indiduck.panda.Service.CookieUtil;
//import com.indiduck.panda.Service.JwtUserDetailsService;
//import com.indiduck.panda.Service.RedisUtil;
//import com.indiduck.panda.domain.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import io.jsonwebtoken.ExpiredJwtException;
//
//@Component
//public class JwtRequestFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtUserDetailsService userDetailsService;
//
//    @Autowired
//    private JwtTokenUtil jwtUtil;
//
//    @Autowired
//    private CookieUtil cookieUtil;
//
//    @Autowired
//    private RedisUtil redisUtil;
//    @Override
//    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
//
//        final Cookie jwtToken = cookieUtil.getCookie(httpServletRequest,JwtTokenUtil.ACCESS_TOKEN_NAME);
//
//        String username = null;
//        String jwt = null;
//        String refreshJwt = null;
//        String refreshUname = null;
//
//        try{
//            if(jwtToken != null){
//                jwt = jwtToken.getValue();
//                username = jwtUtil.getUsername(jwt);
//            }
//            if(username!=null){
//                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//                if(jwtUtil.validateToken(jwt,userDetails)){
//                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
//                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                }
//            }
//        }catch (ExpiredJwtException e){
//            Cookie refreshToken = cookieUtil.getCookie(httpServletRequest,JwtTokenUtil.REFRESH_TOKEN_NAME);
//            if(refreshToken!=null){
//                refreshJwt = refreshToken.getValue();
//            }
//        }catch(Exception e){
//
//        }
//
//        try{
//            if(refreshJwt != null){
//                refreshUname = redisUtil.getData(refreshJwt);
//
//                if(refreshUname.equals(jwtUtil.getUsername(refreshJwt))){
//                    UserDetails userDetails = userDetailsService.loadUserByUsername(refreshUname);
//                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
//                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//
//                    User member = new User();
//                    member.setUsername(refreshUname);
//                    String newToken =jwtUtil.generateToken(member);
//
//                    Cookie newAccessToken = cookieUtil.createCookie(JwtTokenUtil.ACCESS_TOKEN_NAME,newToken);
//                    httpServletResponse.addCookie(newAccessToken);
//                }
//            }
//        }catch(ExpiredJwtException e){
//
//        }
//
//        filterChain.doFilter(httpServletRequest,httpServletResponse);
//    }
//}