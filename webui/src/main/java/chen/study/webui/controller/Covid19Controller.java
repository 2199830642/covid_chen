package chen.study.webui.controller;

import chen.study.webui.bean.Result;
import chen.study.webui.mapper.CovidMapper;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description: 用来接收前端数据请求的Controller
 * @date: 2021-02-20 14:58
 **/
@RestController//表示该类是SpringBoot的controller，且返回的数据为Json格式
@RequestMapping("covid")
public class Covid19Controller {

    @Autowired
    private CovidMapper covidMapper;

    /**
     * 接收前端请求返回全国疫情汇总数据
     */
    @RequestMapping("getNationalData")
    public Result getNationalData(){
        System.out.println("接收到前端发起的获取json数据的请求，后续需要查询mysql，将数据返回");
        String datetime = FastDateFormat.getInstance("yyyy-MM-dd").format(System.currentTimeMillis());
        Map<String, Object> data = covidMapper.getNationalData(datetime).get(0);
        Result result = Result.success(data);
        return result;
    }


    //getNationalMapData

    //getCovidTimeData

    //getCovidImportData

    //getCovidWz


}
