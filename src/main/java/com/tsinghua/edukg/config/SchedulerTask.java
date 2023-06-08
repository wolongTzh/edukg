package com.tsinghua.edukg.config;

import com.tsinghua.edukg.manager.RedisManager;
import com.tsinghua.edukg.service.GraphService;
import com.tsinghua.edukg.utils.RuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Configuration
@Async("scheduleThreadPool")
public class SchedulerTask {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerTask.class);

    @Autowired
    RedisManager redisManager;

    @Autowired
    GraphService graphService;

    public final String cron = "0 0 0/1 * * ?";

    Map<String, String> subjects = RuleHandler.grepSubjectMap();

    /**
     * @Scheduled(fixedRate = 6000) ：上一次开始执行时间点之后6秒再执行
     * @Scheduled(fixedDelay = 6000) ：上一次执行完毕时间点之后6秒再执行
     * @Scheduled(initialDelay=1000, fixedRate=6000) ：第一次延迟1秒后执行，之后按fixedRate的规则每6秒执行一次
     * @Scheduled(cron=""):详见cron表达式http://www.pppet.net/
     */

    @Scheduled(cron=cron)
    public void totalStat(){
        logger.info("执行全图谱状态更新定时任务");
        redisManager.deleteUpdateTotalStatus();
        redisManager.updateTotalStatus();
    }
    @Scheduled(cron=cron)
    public void updateSource(){
        logger.info("执行全图谱来源更新定时任务");
        redisManager.deleteUpdateSource();
        redisManager.updateSource();
    }
    @Scheduled(cron=cron)
    public void subjectStatus(){
        logger.info("执行学科信息统计更新定时任务");
        for(Map.Entry entry : subjects.entrySet()) {
            String subject = (String) entry.getKey();
            redisManager.deleteUpdateSubjectStatus(subject);
            redisManager.updateSubjectStatus(subject);
        }
    }
    @Scheduled(cron=cron)
    public void topTen(){
        logger.info("执行学科实体关系数量top ten更新定时任务");
        for(Map.Entry entry : subjects.entrySet()) {
            String subject = (String) entry.getKey();
            redisManager.deleteHotEntities(subject);
            redisManager.getHotEntities(subject);
        }
    }

    @Scheduled(cron=cron)
    public void updateSubjectGraph() throws IOException {
        graphService.updateSubjectGraph();
    }
}
