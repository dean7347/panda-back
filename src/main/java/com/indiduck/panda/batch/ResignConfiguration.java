package com.indiduck.panda.batch;

import com.indiduck.panda.Service.JwtUserDetailsService;
import com.indiduck.panda.Service.ShopService;
import com.indiduck.panda.Service.UserService;
import com.indiduck.panda.domain.PaymentStatus;
import com.indiduck.panda.domain.SettleShop;
import com.indiduck.panda.domain.Shop;
import com.indiduck.panda.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResignConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired public StepBuilderFactory stepBuilderFactory;
    @Autowired public EntityManagerFactory entityManagerFactory;
    @Autowired
    private final JwtUserDetailsService userService;


    @Bean
    public Job ResignJob() throws Exception {
        log.info("회원탈퇴 스케쥴링 시작");
        Job Resign = jobBuilderFactory.get("ResignJob")
                .start(ResignStep())

                .build();

        return Resign;
    }

    @Bean
    @JobScope
    public Step ResignStep() throws Exception {

        return stepBuilderFactory.get("ResignStep")
                .<User,User>chunk(100)
                .reader(Resignreader())
                .processor(Resignprocessor())
                .writer(Resignwriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<User> Resignreader() throws Exception {

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("beforeDay", LocalDateTime.now().minusDays(7));

        JpaPagingItemReader<User> reader = new JpaPagingItemReader<User>() {
            @Override
            public int getPage() {
                return 0;
            }
        };
        reader.setParameterValues(parameterValues);
        reader.setQueryString("SELECT u FROM User u where u.leaveAt <: beforeDay  ORDER BY id ASC");
        reader.setPageSize(100);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setName("JpaPagingItemReader");

        return reader;
    }
//        return new JpaPagingItemReaderBuilder<User>()
//                .pageSize(10)
//                .parameterValues(parameterValues)
//                //샵 userorder.
//                .queryString("SELECT u FROM User u where u.leaveAt <: beforeDay  ORDER BY id ASC")
////                .queryString("SELECT uo FROM UserOrder uo where uo.enrollSettle =: Estatus And uo.orderStatus =: od And uo.paymentStatus =: ps ORDER BY id ASC")
//                .entityManagerFactory(entityManagerFactory)
//                .name("JpaPagingItemReader")
//                .build();
//    }

    @Bean
    @StepScope
    public ItemProcessor<User, User> Resignprocessor() throws Exception {

        System.out.println(" =프로세서입장 " );

        return new ItemProcessor<User, User>() {
            @Override
            public User process(User user) throws Exception {
//                System.out.println("탈퇴할 유저는? " + user.getEmail());
                userService.deleteUser(user);

                return user;
            }

        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<User> Resignwriter(){
        System.out.println("라이터실행");
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
