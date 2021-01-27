package chen.study.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
* 封装HttpClient工具，方便爬取网页内容
* */
public abstract class HttpUtils {

    private static PoolingHttpClientConnectionManager cm;//声明HttpClient管理器对象（连接池）
    private static List<String> userAgentList = null;
    private static RequestConfig config = null;

    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        config = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000).build();
        userAgentList = new ArrayList<>();
        //每次访问浏览器都不一样，避免封掉IP
        userAgentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        userAgentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:73.0) Gecko/20100101 Firefox/73.0");
        userAgentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15");
        userAgentList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299");
        userAgentList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        userAgentList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
    }

    public static String getHtml(String url){
        //从连接池中获取HttpClient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        //创建HttpGet对象
        HttpGet httpGet = new HttpGet(url);
        //设置请求头和请求配置对象
        httpGet.setConfig(config);
        httpGet.setHeader("User-Agent",userAgentList.get(new Random().nextInt(userAgentList.size())));
        //发起请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            //获取响应
            if (response.getStatusLine().getStatusCode()==200) {
                String html = null;
                if (response.getEntity() != null) {
                    html = EntityUtils.toString(response.getEntity(), "UTF-8");
                    return html;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }finally {
            try {
                response.close();
                //连接池 httpclient不需要关
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String html = HttpUtils.getHtml("https://tieba.baidu.com/index.html");
        System.out.println(html);
    }
}
