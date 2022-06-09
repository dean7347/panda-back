package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name="member_id")
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "auth")
    private String auth;

    private String userRName;
    private String userPhoneNumber;
    private LocalDateTime regAt;
    private LocalDateTime leaveAt;

    private boolean isEmail;
    private Long recentAddress;
    //동의사항
    private boolean adult;
    private boolean apprterm;
    private boolean priagree;

    //CI정보
    private String ci;
    //인증성공여부




    @OneToMany(mappedBy = "user")
    private List<OrderDetail> orders =new ArrayList<>();

    @OneToMany(mappedBy = "userName", orphanRemoval = true)
    private List<DeliverAddress> userAddress =new ArrayList<>();
//
//    @OneToMany(mappedBy = "writerName")
//    private List<ProductReply> replies =new ArrayList<>();

    @OneToOne(optional = true)
    private Shop shop;

    @OneToMany(mappedBy = "userId")
    private List<UserOrder> userOrders;
    @OneToOne(mappedBy = "user")
    private Panda panda;
    @OneToMany(mappedBy = "user")
    private List<RefundRequest> refundRequests;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();


    public void setUsername(String username)
    {
        this.email=username;
    }
    // 사용자의 권한을 콜렉션 형태로 반환
    // 단, 클래스 자료형은 GrantedAuthority를 구현해야함

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // 사용자의 id를 반환 (unique한 값)
    @Override
    public String getUsername() {
        return email;
    }

    // 사용자의 password를 반환
    @Override
    public String getPassword() {
        return password;
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }

    public void deleteShop()
    {
        this.shop=null;
    }
    public void deletePanda()
    {
        this.panda=null;
    }
    public void setLeaveAt(){
        this.leaveAt=LocalDateTime.now();
    }
    public User deleteAll()
    {

//        System.out.println(" =회원 탈퇴로직 시작 ");
        for (UserOrder userOrder : userOrders) {
            userOrder.resignUser();
        }
        this.userPhoneNumber="조회 불가능한 회원입니다";
        this.userRName="조회 불가능한 회원입니다";
        this.ci="조회 불가능한 회원입니다";
        return this;
    }
}
