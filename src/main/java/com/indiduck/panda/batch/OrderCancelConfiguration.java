package com.indiduck.panda.batch;

import com.indiduck.panda.Service.PandaService;
import com.indiduck.panda.Service.ShopService;
import com.indiduck.panda.Service.UserOrderService;
import com.indiduck.panda.domain.*;
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
public class OrderCancelConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired public StepBuilderFactory stepBuilderFactory;
    @Autowired public EntityManagerFactory entityManagerFactory;
    @Autowired public UserOrderService userOrderService;




    @Bean
    public Job OrderCancelJob() throws Exception {
        log.info("5일간 확인하지 않은 주문 취소 로직을 시작합니다");
        Job OrderCancel = jobBuilderFactory.get("OrderCancelJob")
                .start(OrderCancelStep())

                .build();

        return OrderCancel;
    }

    @Bean
    @JobScope
    public Step OrderCancelStep() throws Exception {

        return stepBuilderFactory.get("OrderCancelStep")
                .<UserOrder,UserOrder>chunk(100)
                .reader(OrderCancelreader())
                .processor(OrderCancelprocessor())
                .writer(OrderCancelwriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<UserOrder> OrderCancelreader() throws Exception {

        Map<String,Object> parameterValues = new HashMap<>();
//        parameterValues.put("Estatus", false);
        parameterValues.put("od", OrderStatus.결제완료);
        parameterValues.put("limitDay", LocalDateTime.now().minusDays(5));


        JpaPagingItemReader<UserOrder> reader = new JpaPagingItemReader<UserOrder>() {
            @Override
            public int getPage() {
                return 0;
            }
        };
        reader.setParameterValues(parameterValues);
        reader.setQueryString("SELECT uo FROM UserOrder uo where uo.createdAt <: limitDay And uo.orderStatus =: od ORDER BY id ASC");
        reader.setPageSize(100);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setName("JpaPagingItemReader");

        return reader;
//        return new JpaPagingItemReaderBuilder<UserOrder>()
//                .pageSize(10)
//                .parameterValues(parameterValues)
//                //샵 userorder.
////                .queryString("Select p FROM Panda p where exists (Select odp from p.orderDetailPandas odp where odp.enrollSettle =: Estatus And odp.paymentStatus =: ps And odp.orderStatus =: od) ORDER BY id ASC")
////                .queryString("SELECT uo FROM UserOrder uo where uo.enrollSettle =: Estatus And uo.orderStatus =: od And uo.paymentStatus =: ps ORDER BY id ASC")
//                .queryString("SELECT uo FROM UserOrder uo where uo.createdAt <: limitDay And uo.orderStatus =: od ORDER BY id ASC")
//                .entityManagerFactory(entityManagerFactory)
//                .name("JpaPagingItemReader")
//                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<UserOrder, UserOrder> OrderCancelprocessor() throws Exception {

        System.out.println(" =프로세서입장 " );

        return new ItemProcessor<UserOrder, UserOrder>() {
            @Override
            public UserOrder process(UserOrder userOrder) throws Exception {
                log.info("취소되는 주문은 = " + userOrder.getUserId());
                userOrder.cancelOrder();
                userOrder.cancelMoneyJob();

                userOrderService.cancelOrderSystem(userOrder.getId());

                return userOrder;
            }

        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<UserOrder> OrderCancelwriter(){
        System.out.println("라이터실행");
        return new JpaItemWriterBuilder<UserOrder>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
