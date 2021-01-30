package chen.study.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 用来封装各省市疫情数据的JavaBean
 */
@Data//自动生成get，set，toString...方法
@NoArgsConstructor//空参构造
@AllArgsConstructor//全参构造
public class CovidBean {
    private String provinceName;//省份名称
    private String provinceShortName;//省份短名
    private String cityName;
    private Integer currentConfirmedCount;//当前确诊人数
    private Integer confirmedCount;//累记确诊人数
    private Integer suspectedCount;//疑似病例人数
    private Integer curedCount;//治愈人数
    private Integer deadCount;//死亡人数
    private Integer locationId;//位置id
    private Integer pid;//位置id
    private String statisticsData;//每一天的统计数据
    private String cities;//下属城市
    private String datetime;//下属城市
}
