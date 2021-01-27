package chen.study;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/*
* 使用Jsoup实现页面解析
* */
public class JsoupTest {
    @Test
    public void testGetDocument() throws IOException {
        Document doc = Jsoup.connect("https://tieba.baidu.com/index.html").get();
        //Document DOC = Jsoup.parse(new URL("https://tieba.baidu.com/index.html"), 10000);
        System.out.println(doc);
        Element title = doc.getElementsByTag("title").first();
        String text = title.text();
        System.out.println(text);
    }

    @Test
    public void testJsoupHtml() throws Exception {
        Document doc = Jsoup.parse(new File("jsoup.html"), "UTF-8");

        //**使用dom方式遍历文档
        //1. 根据id查询元素getElementById
        Element element = doc.getElementById("city_bj");
        System.out.println(element.text());
        //2. 根据标签获取元素getElementsByTag
        element = doc.getElementsByTag("title").first();
        System.out.println(element.text());
        //3. 根据class获取元素getElementsByClass
        element = doc.getElementsByClass("s_name").last();
        System.out.println(element.text());
        //4. 根据属性获取元素getElementsByAttribute
        element = doc.getElementsByAttribute("abc").first();
        System.out.println(element.text());
        element = doc.getElementsByAttributeValue("class", "city_con").first();
        System.out.println(element.text());


        //**元素中数据获取
        //1. 从元素中获取id
        String str = element.id();
        System.out.println(str);
        //2. 从元素中获取className
        str = element.className();
        System.out.println(str);
        //3. 从元素中获取属性的值attr
        str = element.attr("id");
        System.out.println(str);
        //4. 从元素中获取所有属性attributes
        str = element.attributes().toString();
        System.out.println(str);
        //5. 从元素中获取文本内容text
        str = element.text();
        System.out.println(str);

        //**使用选择器语法查找元素
        //jsoup elements对象支持类似于CSS (或jquery)的选择器语法，来实现非常强大和灵活的查找功能。
        //select方法在Document/Element/Elements对象中都可以使用。可实现指定元素的过滤，或者链式选择访问。
        //1. tagname: 通过标签查找元素，比如：span
        Elements span = doc.select("span");
        for (Element e : span) {
            System.out.println(e.text());
        }
        //2. #id: 通过ID查找元素，比如：#city_bjj
        str = doc.select("#city_bj").text();
        System.out.println(str);
        //3. .class: 通过class名称查找元素，比如：.class_a
        str = doc.select(".class_a").text();
        System.out.println(str);
        //4. [attribute]: 利用属性查找元素，比如：[abc]
        str = doc.select("[abc]").text();
        System.out.println(str);
        //5. [attr=value]: 利用属性值来查找元素，比如：[class=s_name]
        str = doc.select("[class=s_name]").text();
        System.out.println(str);


        //**Selector选择器组合使用
        //1. el#id: 元素+ID，比如： h3#city_bj
        str = doc.select("h3#city_bj").text();
        System.out.println(str);
        //2. el.class: 元素+class，比如： li.class_a
        str = doc.select("li.class_a").text();
        System.out.println(str);
        //3. el[attr]: 元素+属性名，比如： span[abc]
        str = doc.select("span[abc]").text();
        System.out.println(str);
        //4. 任意组合，比如：span[abc].s_name
        str = doc.select("span[abc].s_name").text();
        System.out.println(str);
        //5. ancestor child: 查找某个元素下子元素，比如：.city_con li 查找"city_con"下的所有li
        str = doc.select(".city_con li").text();
        System.out.println(str);
        //6. parent > child: 查找某个父元素下的直接子元素，
        //比如：.city_con > ul > li 查找city_con第一级（直接子元素）的ul，再找所有ul下的第一级li
        str = doc.select(".city_con > ul > li").text();
        System.out.println(str);
        //7. parent > * 查找某个父元素下所有直接子元素.city_con > *
        str = doc.select(".city_con > *").text();
        System.out.println(str);
    }
}
