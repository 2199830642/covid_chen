package chen.study;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



/**
 * 演示使用JDK自带的API实现网络爬虫
 */
public class JDKAPITest {
    @Test
    public void testGet() throws Exception {
        //1.确定要访问/爬取的url
        URL url = new URL("https://tieba.baidu.com/f?kw=%E7%A6%8F%E5%B0%94%E6%91%A9%E6%96%AF&fr=index&fp=0&ie=utf-8?username=xx");
        //2.获取连接对象
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //3.设置连接的信息，请求方式/请求参数/请求头
        urlConnection.setRequestMethod("GET");//请求方式默认为get，注意要大写
        urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        urlConnection.setConnectTimeout(30000);//设置超时时间，单位毫秒
        //4.获取数据
        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        String html = "";
        while((line = reader.readLine())!=null){
            html += line + "\n";
        }
        System.out.println(html);
        //5.关闭连接
        in.close();
        reader.close();
    }

    @Test
    public void testPost() throws Exception{
        URL url = new URL("https://mooc1-1.chaoxing.com/mycourse/studentcourse?courseId=202703322&clazzid=5793914&enc=9d728673c57c13dbee4558cbcc6f1c5d&cpi=41193774&vc=1");
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setDoOutput(true);//允许向服务端输出内容
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        urlConnection.setConnectTimeout(30000);
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write("username=xx".getBytes());
        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        String html = "";
        while((line = reader.readLine())!=null){
            html += line + "\n";
        }
        System.out.println(html);
        //5.关闭连接
        in.close();//3537802178
        reader.close();
    }

}
