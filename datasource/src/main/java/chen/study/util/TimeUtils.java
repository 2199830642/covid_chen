package chen.study.util;

import org.apache.commons.lang3.time.FastDateFormat;

/*
* 时间工具类
* */
public abstract class TimeUtils {
    public static String format(long timestamp,String pattern){
        return FastDateFormat.getInstance(pattern).format(timestamp);
    }
}
