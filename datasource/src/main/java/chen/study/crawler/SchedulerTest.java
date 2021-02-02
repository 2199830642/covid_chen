package chen.study.crawler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description:演示定时任务
 * @date: 2021-02-02 17:34
 **/
@Component//表示将该类交给spring管理，作为spring容器中的对象
public class SchedulerTest {
    public static void main(String[] args) {
        //演示JDK中自带的定时任务API
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("每隔1秒执行一次");
            }
        },1000,1000);
    }

    //演示SpringBoot中提供的定时任务工具
    //@Scheduled(initialDelay = 1000,fixedDelay = 1000)
    //@Scheduled(cron = "0/1 * * * * ?")//每隔一秒执行一次
    //@Scheduled(cron = "0 0 8 * * ?")//每天八点定时执行
    public void scheduler(){
        System.out.println("@Scheduled-cron每隔一秒执行一次");
    }
}
