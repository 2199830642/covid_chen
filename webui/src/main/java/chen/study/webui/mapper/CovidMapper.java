package chen.study.webui.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description:
 * @date: 2021-02-24 16:51
 **/
@Mapper
@Component
public interface CovidMapper {
    /**
     * 查询全国疫情汇总数据
     * @param datetime
     * @return
     */
    @Select("select `datetime`, `currentConfirmedCount`, `confirmedCount`, `suspectedCount`, `curedCount`, `deadCount` from covid19_1 where datetime = #{datetime}")
    List<Map<String,Object>> getNationalData(String datetime);
}
