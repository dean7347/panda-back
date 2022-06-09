package com.indiduck.panda.batch;

import com.indiduck.panda.domain.OrderStatus;
import com.indiduck.panda.domain.UserOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
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
public class JobConfiguration {

    @Autowired public JobBuilderFactory jobBuilderFactory;
    @Autowired public StepBuilderFactory stepBuilderFactory;
    @Autowired public EntityManagerFactory entityManagerFactory;

    @Bean
    public Job ConfirmJob() throws Exception {
        Job exampleJob = jobBuilderFactory.get("ConfirmJob")
                .start(Step())
                .build();

        return exampleJob;
    }

    @Bean
    @JobScope
    public Step Step() throws Exception {
        return stepBuilderFactory.get("Step")
                .<UserOrder,UserOrder>chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<UserOrder> reader() throws Exception {

        Map<String,Object> parameterValues = new HashMap<>();
        parameterValues.put("beforeDay", LocalDateTime.now().minusDays(14));
        parameterValues.put("od", OrderStatus.발송중);



        JpaPagingItemReader<UserOrder> reader = new JpaPagingItemReader<UserOrder>() {
            @Override
            public int getPage() {
                return 0;
            }
        };
        reader.setParameterValues(parameterValues);
        reader.setQueryString("SELECT uo FROM UserOrder uo where uo.standardfinishAt <: beforeDay And uo.orderStatus =: od ORDER BY id ASC");
        reader.setPageSize(100);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setName("JpaPagingItemReader");

        return reader;

//        return new JpaPagingItemReaderBuilder<UserOrder>()
//                .pageSize(15)
//                .parameterValues(parameterValues)
//                .queryString("SELECT uo FROM UserOrder uo where uo.standardfinishAt <: beforeDay And uo.orderStatus =: od ORDER BY id ASC")
//                .entityManagerFactory(entityManagerFactory)
//                .name("JpaPagingItemReader")
//                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<UserOrder, UserOrder> processor(){

        return new ItemProcessor<UserOrder, UserOrder>() {

            @Override
            public UserOrder process(UserOrder us) throws Exception {
                log.info("us 컨펌실행" + us.getId());
             us.batchConfirm(LocalDateTime.now());

                return us;
            }
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<UserOrder> writer(){
        return new JpaItemWriterBuilder<UserOrder>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}