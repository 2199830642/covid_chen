package chen.study.generator;

import chen.study.bean.MaterialBean;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description:使用程序模拟生成疫情数据
 * @date: 2021-02-02 17:47
 **/
@Component
public class Covid19DataGenerator {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    //物资名称
    private String[] material_name = new String[]
            {"N95口罩/个","医用外科一次性口罩/个","84消毒液/瓶","电子体温计/个","一次性手套/副","护目镜/副","医用防护服/套"};

    //物资来源
    private  String[] material_from = new String[]
            {"采购","下拨","捐赠","消耗","需求"};

    //@Scheduled(initialDelay = 1000,fixedDelay = 10000)
    public void generator(){
        Random ran = new Random();
        for (int i = 0 ; i < 10;i++){
            MaterialBean materialBean = new MaterialBean(material_name[ran.nextInt(material_name.length)], material_from[ran.nextInt(material_from.length)], ran.nextInt(1000));
            System.out.println(materialBean);
            //将生成的疫情物资数据转换为json字符串发送到kafka集群
            String jsonStr = JSON.toJSONString(materialBean);
            kafkaTemplate.send("covid19_wz",ran.nextInt(3),jsonStr);
        }
    }
}
