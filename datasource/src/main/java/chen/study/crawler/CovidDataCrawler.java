package chen.study.crawler;

import chen.study.DatasourceApplication;
import chen.study.bean.CovidBean;
import chen.study.util.HttpUtils;
import chen.study.util.TimeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现疫情数据爬取
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= DatasourceApplication.class)
public class CovidDataCrawler {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Test
    public void testKafkaTemplate() throws Exception {
        kafkaTemplate.send("covid19",1,"ccc");
        Thread.sleep(100000);
    }






    @Test
    public void test() {
        String dateTime = TimeUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");

        //1.爬取指定页面
        String html = HttpUtils.getHtml("https://ncov.dxy.cn/ncovh5/view/pneumonia");
        //System.out.println(html);
        //2.解析页面指定内容,即id为getAreaStat的标签中的全国疫情数据
        Document doc = Jsoup.parse(html);
        String text = doc.select("script[id=getAreaStat]").toString();
        //System.out.println(text);

        //3.使用正则表达式获取json数据
        String pattern = "\\[(.*)\\]";//定义正则
        Pattern reg = Pattern.compile(pattern);//编译成正则对象
        Matcher matcher = reg.matcher(text);//去text中进行匹配
        String jsonStr = "";
        if (matcher.find()){//如果text中的内容和正则规则匹配上的就取出来赋值给jsonStr
            jsonStr = matcher.group(0);
            //System.out.println(jsonStr);
        }else {
            System.out.println("no match");
        }

        //对json数据进行更进一步的解析
        //4.将第一层的json(省份数据)解析为JavaBean
        List<CovidBean> pCovidBeans = JSON.parseArray(jsonStr, CovidBean.class);
        for (CovidBean pBean : pCovidBeans) {//省份
            // System.out.println(pBean);
            pBean.setDatetime(dateTime);
            //获取cities
            String citiesStr = pBean.getCities();
            //5.将第二层json(城市数据)解析为JavaBean
            List<CovidBean> covidBeans = JSON.parseArray(citiesStr, CovidBean.class);
            for (CovidBean bean : covidBeans) {//城市
                //System.out.println(bean);
                bean.setDatetime(dateTime);
                bean.setPid(pBean.getLocationId());//把省份的id设置为城市的pid
                bean.setProvinceShortName(pBean.getProvinceShortName());
                //System.out.println(bean);
                //后续需要将城市疫情数据发送给kafka
            }
            //6.获取第一层json（省份数据）中每一天的统计数据
            String statisticsDataUrl = pBean.getStatisticsData();
            String statisticsDataStr = HttpUtils.getHtml(statisticsDataUrl);
            //获取statisticsDataStr中的data字段对应的数据
            JSONObject jsonObject = JSON.parseObject(statisticsDataStr);
            String dataStr = jsonObject.getString("data");
            //将爬取解析出来的每一天的数据设置回pBean中的StatisticsData字段中（之前该字段只是一个URL）
            pBean.setStatisticsData(dataStr);
            pBean.setCities(null);
            //System.out.println(pBean);
            //后续需要将省份疫情数据发送给kafka
        }
    }
}
