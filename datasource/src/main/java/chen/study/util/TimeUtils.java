package chen.study.util;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description:时间工具类
 * @date: 2021-01-15 17:50
 **/
public abstract class TimeUtils {
    public static String format(long timestamp,String pattern){
        return FastDateFormat.getInstance(pattern).format(timestamp);
    }
}
