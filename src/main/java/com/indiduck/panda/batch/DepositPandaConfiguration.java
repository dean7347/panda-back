package com.indiduck.panda.batch;

import com.indiduck.panda.Service.PandaService;
import com.indiduck.panda.Service.ShopService;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DepositPandaConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired public StepBuilderFactory stepBuilderFactory;
    @Autowired public EntityManagerFactory entityManagerFactory;
    @Autowired
    private final ShopService shopService;
    @Autowired
    private final PandaService pandaService;



    @Bean
    public Job DepositPandaJob() throws Exception {
        log.info("정산 스케쥴링 시작");
        Job DepositPanda = jobBuilderFactory.get("DepositPandaJob")
                .start(DepositPandaStep())

                .build();

        return DepositPanda;
    }

    @Bean
    @JobScope
    public Step DepositPandaStep() throws Exception {

        return stepBuilderFactory.get("DepositPandaStep")
                .<Panda,Panda>chunk(100)
                .reader(DepositPandareader())
                .processor(DepositPandaprocessor())
                .writer(DepositPandawriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Panda> DepositPandareader() throws Exception {

        Map<String,Object> parameterValues = new HashMap<>();
        parameterValues.put("Estatus", false);
        parameterValues.put("od", OrderStatus.구매확정);
        parameterValues.put("ps", PaymentStatus.지급예정);


        JpaPagingItemReader<Panda> reader = new JpaPagingItemReader<Panda>() {
            @Override
            public int getPage() {
                return 0;
            }
        };
        reader.setParameterValues(parameterValues);
        reader.setQueryString("Select p FROM Panda p where exists (Select odp from p.orderDetailPandas odp where odp.enrollSettle =: Estatus And odp.paymentStatus =: ps And odp.orderStatus =: od) ORDER BY id ASC");
        reader.setPageSize(100);
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setName("JpaPagingItemReader");

        return reader;
//        return new JpaPagingItemReaderBuilder<Panda>()
//                .pageSize(10)
//                .parameterValues(parameterValues)
//                //샵 userorder.
//                .queryString("Select p FROM Panda p where exists (Select odp from p.orderDetailPandas odp where odp.enrollSettle =: Estatus And odp.paymentStatus =: ps And odp.orderStatus =: od) ORDER BY id ASC")
////                .queryString("SELECT uo FROM UserOrder uo where uo.enrollSettle =: Estatus And uo.orderStatus =: od And uo.paymentStatus =: ps ORDER BY id ASC")
//                .entityManagerFactory(entityManagerFactory)
//                .name("JpaPagingItemReader")
//                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Panda, Panda> DepositPandaprocessor() throws Exception {

        System.out.println(" =프로세서입장 " );

        return new ItemProcessor<Panda, Panda>() {
            @Override
            public Panda process(Panda Panda) throws Exception {
                System.out.println("청구서결제의판다는us = " + Panda.getPandaName());
                SettlePanda settlePanda = pandaService.SettleLogic(Panda);


                return Panda;
            }

        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Panda> DepositPandawriter(){
        System.out.println("라이터실행");
        return new JpaItemWriterBuilder<Panda>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
