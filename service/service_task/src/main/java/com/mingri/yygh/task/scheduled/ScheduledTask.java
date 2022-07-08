package com.mingri.yygh.task.scheduled;

import com.mingri.common.rabbitmq.constant.MqConst;
import com.mingri.common.rabbitmq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling //开启定时任务操作
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;

    //每天8点执行方法，就医提醒
    //cron表达式，设置执行间隔
    //0 0 8 * * ? 每天8点
    @Scheduled(cron = "0/30 * * * * ?")  //每隔30秒，便于测试
    public void task() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"");
    }
}
