package chen.study.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description: 用来封装防疫物资
 * @date: 2021-02-02 17:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialBean {
    private String name;//物资名称
    private String from;//物资来源
    private Integer count;//物资数量
}
