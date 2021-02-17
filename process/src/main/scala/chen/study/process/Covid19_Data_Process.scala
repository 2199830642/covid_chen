package chen.study.process

import chen.study.bean.{CovidBean, StatisticsDataBean}
import com.alibaba.fastjson.JSON
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger

import scala.collection.mutable

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description: 全国各省市疫情数据实时处理统计分析
 * @date: 2021-02-15 14:34
 **/
object Covid19_Data_Process {
  def main(args: Array[String]): Unit = {
    //1.创建StructuredStreaming执行环境
    //StructuredStreaming支持使用SQL来处理实时流数据，数据抽象和SparkSQL一样，也是DataFrame和DataSet
    //所以这里创建StructuredStreaming执行环境就直接创建SparkSession即可
    val spark = SparkSession.builder().master("local[*]").appName("Covid19_Data_Process").getOrCreate()
    val sc = spark.sparkContext
    sc.setLogLevel("WARN")
    //导入隐式转换，方便后续使用
    import spark.implicits._
    import org.apache.spark.sql.functions._
    import scala.collection.JavaConversions._

    //2.连接kafka
    //从kafka接收消息
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "192.168.120.128:9092")
      .option("subscribe", "covid19")
      .load()
    //取出消息中的value
    val jsonStrDS = kafkaDF.selectExpr("CAST(value AS STRING)").as[String]
    /*jsonStrDS.writeStream
      .format("console")//输出目的地
      .outputMode("append")//输出模式，默认append，表示显示新增的记录
      .trigger(Trigger.ProcessingTime(0))//触发间隔，0表示尽可能快的执行
      .option("truncate",false)//表示如果列名过长，不进行截断
      .start()
      .awaitTermination()*/

    //3.处理数据
    //将jsonStr转为样例类
    val covidBeanDS = jsonStrDS.map(jsonStr => {
      //Scala中获取class对象使用classOf[类名]
      JSON.parseObject(jsonStr, classOf[CovidBean])
    })
    //分离出省份数据
    val provinceDS = covidBeanDS.filter(_.statisticsData != null)
    //分离出城市数据
    val cityDS = covidBeanDS.filter(_.statisticsData == null)
    //分离出各省份每一天的统计数据
    val statisticsDataDS = provinceDS.flatMap(p => {
      val jsonStr = p.statisticsData //获取到的是该省份每一天的统计数据组成的jsonStr数组
      val list: mutable.Buffer[StatisticsDataBean] = JSON.parseArray(jsonStr, classOf[StatisticsDataBean])
      list.map(s => {
        s.provinceShortName = p.provinceShortName
        s.locationId = p.locationId
        s
      })
    })

    /*statisticsDataDS.writeStream
      .format("console")//输出目的地
      .outputMode("append")//输出模式，默认append，表示显示新增的记录
      .trigger(Trigger.ProcessingTime(0))//触发间隔，0表示尽可能快的执行
      .option("truncate",false)//表示如果列名过长，不进行截断
      .start()
      .awaitTermination()*/
    //4.统计分析

    //5.结果输出

  }

}
