package chen.study;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用HttpClient实现网络爬虫
 */
public class HttpClientTest {
    @Test
    public void testGet() throws Exception {
        //1.创建HttpClient对象
        //DefaultHttpClient defaultHttpClient = new DefaultHttpClient();  过时了
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //2.创建HttpGet请求并进行相关设置
        HttpGet httpGet = new HttpGet("https://mooc1-1.chaoxing.com/mycourse/studentcourse?courseId=202703322&clazzid=5793914&enc=9d728673c57c13dbee4558cbcc6f1c5d&cpi=41193774&vc=1");
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");

        //3.发起请求
        CloseableHttpResponse response = httpClient.execute(httpGet);

        //4.判断响应状态并获取响应数据
        if (response.getStatusLine().getStatusCode() == 200){//200表示响应成功
            String html = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(html);
        }

        //5.关闭资源
        httpClient.close();
        response.close();
    }

    @Test
    public void testPost() throws Exception {
        //1.创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //2.创建HttpPost对象并进行相关设置
        HttpPost httpPost = new HttpPost("https://mooc1-1.chaoxing.com/mycourse/studentcourse?courseId=202703322&clazzid=5793914&enc=9d728673c57c13dbee4558cbcc6f1c5d&cpi=41193774&vc=1");
        //准备集合用来存放请求参数
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username","java"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");

        //3.发起请求
        CloseableHttpResponse response = httpClient.execute(httpPost);

        //4.判断响应状态并且获取响应数据
        if (response.getStatusLine().getStatusCode()==200){
            String html = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(html);
        }

        //5.关闭资源
        response.close();
        httpClient.close();
    }

    @Test//使用HttpClient连接池
    public void testPool() throws Exception {
        //1.创建HttpClient连接管理器
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //2.设置参数
        cm.setMaxTotal(200);//设置最大连接数
        cm.setDefaultMaxPerRoute(20);//设置每个主机的最大并发
        doGet(cm);
        doGet(cm);
    }

    private void doGet(PoolingHttpClientConnectionManager cm) throws Exception {
        //3.从连接池中获取HttpClient对象
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        //在这里加上断点观察到每次从池中获取到一个HttpClient对象
        //4.创建httpGet对象
        HttpGet httpGet = new HttpGet("https://mooc1-1.chaoxing.com/mycourse/studentcourse?courseId=202703322&clazzid=5793914&enc=9d728673c57c13dbee4558cbcc6f1c5d&cpi=41193774&vc=1");
        //5.发送请求
        CloseableHttpResponse response = httpClient.execute(httpGet);
        //6.获取数据
        if (response.getStatusLine().getStatusCode()==200){
            String html = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(html);
        }
        //7.关闭资源
        response.close();
        //httpClient.close();  这里不用关闭httpClient，因为使用连接池，HttpClient对象使用完要还回到池中
    }

    @Test//HttpClient超时设置,使用代理服务器
    public void testConfig() throws Exception {
        //0.创建请求配置对象
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)//设置连接超时时间
                .setConnectTimeout(10000)//设置创建连接超时时间
                .setConnectionRequestTimeout(10000)//设置请求超时时间
                .setProxy(new HttpHost("47.98.112.7",80))//添加代理服务器
                .build();
        //1.创建HttpClient对象
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        //2.创建HttpGet对象
        HttpGet httpGet = new HttpGet("https://www.baidu.com/");
        //3.发起请求
        CloseableHttpResponse response = httpClient.execute(httpGet);
        //4.获取响应数据
        if (response.getStatusLine().getStatusCode()==200){
            String html = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(html);
        }
        //5.关闭资源
        response.close();
        httpClient.close();
    }

}
