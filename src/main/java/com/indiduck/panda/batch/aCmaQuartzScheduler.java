//package com.indiduck.panda.batch;
//
//import java.util.Date;
//
//import javax.annotation.PostConstruct;
//
//import org.quartz.CronScheduleBuilder;
//import org.quartz.JobBuilder;
//import org.quartz.JobDetail;
//import org.quartz.Scheduler;
//import org.quartz.SchedulerException;
//import org.quartz.SchedulerFactory;
//import org.quartz.Trigger;
//import org.quartz.TriggerBuilder;
//import org.quartz.impl.StdSchedulerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class aCmaQuartzScheduler {
//
//    private SchedulerFactory schedulerFactory;
//    private Scheduler scheduler;
//
//
//    @PostConstruct
//    public void start() throws SchedulerException {
//
//        schedulerFactory = new StdSchedulerFactory();
//        scheduler = schedulerFactory.getScheduler();
//        scheduler.start();
//
//        JobDetail job = JobBuilder.newJob(acmaQuartzJob.class).withIdentity("aCmaJob").build();
//        Trigger trigger = TriggerBuilder.newTrigger()
//                .withSchedule(CronScheduleBuilder.cronSchedule("30 * * * * ?"))
//                .build();
//
//        //Trigger trigger = TriggerBuilder.newTrigger().startAt(startDateTime).endAt(endDateTime)
//        //        .withSchedule(CronScheduleBuilder.cronSchedule("*/1 * * * *")).build();
//
//        scheduler.scheduleJob(job, trigger);
//    }
//}
