package com.indiduck.panda.batch;

import com.indiduck.panda.batch.JobConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobConfiguration jobConfiguration;
    @Autowired
    private DepositConfiguration depositConfiguration;
    @Autowired
    private DepositPandaConfiguration pandaConfiguration;
    @Autowired
    private OrderCancelConfiguration orderCancelConfiguration;

    @Autowired
    private ResignConfiguration resignConfiguration;


    //구매확정은 매일 새벽 03시에 실행한다
    @Scheduled(cron = "0 0 3 ? * *")
    public void runConfirmJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {

            jobLauncher.run(jobConfiguration.ConfirmJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //상점 정산서는 매주 일요일 04시에 실행한다
    @Scheduled(cron = "0 0 4 ? * SUN ")
    public void runDepositJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        log.info("샵청구스케쥴링시작");
        try {

            jobLauncher.run(depositConfiguration.DepositJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //판다 청구서는 매주 일요일 05시 30에 실행한다
    @Scheduled(cron = "0 30 5 ? * SUN ")
    public void runDepositPandaJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        log.info("판다청구스케쥴링시작");
        try {

            jobLauncher.run(pandaConfiguration.DepositPandaJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //회원탈퇴는 매일 새벽 04시 30에 실행한다
    @Scheduled(cron = "0 30 4 ? * *")
    public void runResignJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        System.out.println("회원탈퇴스케쥴링시작");
        try {

            jobLauncher.run(resignConfiguration.ResignJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //회원탈퇴는 매일 새벽 04시 40에 실행한다
    @Scheduled(cron = "0 40 4 ? * *")
    public void cancelOrderJob() {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        System.out.println("주문취소");
        try {

            jobLauncher.run(orderCancelConfiguration.OrderCancelJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//package com.indiduck.panda.batch;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.*;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@Slf4j
//public class JobScheduler {
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    private Job ExampleJob;
//
//    @Scheduled(cron = "30 * * * * ?")
//    public void jobSchduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
//            JobRestartException, JobInstanceAlreadyCompleteException {
//
//        Map<String, JobParameter> jobParametersMap = new HashMap<>();
//
//        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//        Date time = new Date();
//
//        String time1 = format1.format(time);
//
//        jobParametersMap.put("date",new JobParameter(time1));
//
//        JobParameters parameters = new JobParameters(jobParametersMap);
//
//        JobExecution jobExecution = jobLauncher.run(ExampleJob, parameters);
//
//        while (jobExecution.isRunning()) {
//            log.info("...");
//        }
//
//        log.info("Job Execution: " + jobExecution.getStatus());
//        log.info("Job getJobConfigurationName: " + jobExecution.getJobConfigurationName());
//        log.info("Job getJobId: " + jobExecution.getJobId());
//        log.info("Job getExitStatus: " + jobExecution.getExitStatus());
//        log.info("Job getJobInstance: " + jobExecution.getJobInstance());
//        log.info("Job getStepExecutions: " + jobExecution.getStepExecutions());
//        log.info("Job getLastUpdated: " + jobExecution.getLastUpdated());
//        log.info("Job getFailureExceptions: " + jobExecution.getFailureExceptions());
//
//    }
//}