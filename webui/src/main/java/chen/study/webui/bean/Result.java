package chen.study.webui.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description: 封装响应给前端数据的JavaBean(Controller会将该JavaBean转为Json,前端要求该json中有data字段)
 * @date: 2021-02-24 16:38
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Object data;
    private Integer code;
    private String message;

    public static Result success(Object data){
        Result result = new Result();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static Result fail(){
        Result result = new Result();
        result.setCode(500);
        result.setMessage("fail");
        result.setData(null);
        return result;
    }
}
