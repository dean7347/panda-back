//package com.indiduck.panda.batch;
//
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.Job;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//@Slf4j
//public class ConfrimStateChangeJob implements Job {
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//
//        Date date = new Date();
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy년 MM월 dd일");
//        SimpleDateFormat sdf2 = new SimpleDateFormat("HH시 mm분 ss초");
//        String currentDate = sdf1.format(date);
//        String currentTime = sdf2.format(date);
//
//        /*
//         *         execute() method 에 로직 추가
//         */
//        log.info("========= acmaQuartzJob execute() method Start !!! =========");
//        log.info("Start Time >>> {}", currentDate + " " + currentTime);
//    }
//}
//
