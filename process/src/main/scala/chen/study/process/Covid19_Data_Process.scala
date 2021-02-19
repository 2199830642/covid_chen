package chen.study.process

import chen.study.bean.{CovidBean, StatisticsDataBean}
import chen.study.util.BaseJdbcSink
import com.alibaba.fastjson.JSON
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SparkSession}
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
    //4.1.全国疫情汇总信息:现有确诊,累计确诊,现有疑似,累计治愈,累计死亡--注意:按照日期分组统计
    val result1: DataFrame = provinceDS.groupBy('datetime)
      .agg(sum('currentConfirmedCount) as "currentConfirmedCount", //现有确诊
        sum('confirmedCount) as "confirmedCount", //累计确诊
        sum('suspectedCount) as "suspectedCount", //现有疑似
        sum('curedCount) as "curedCount", //累计治愈
        sum('deadCount) as "deadCount" //累计死亡
      )

    //4.2.全国各省份累计确诊数地图--注意:按照日期-省份分组
    /*cityDS.groupBy('datetime,'provinceShortName)
      .agg(sum('confirmedCount) as "confirmedCount")*/
    val result2: DataFrame = provinceDS.select('datetime,'locationId,'provinceShortName,'currentConfirmedCount,'confirmedCount,'suspectedCount,'curedCount,'deadCount)

    //4.3.全国疫情趋势--注意:按照日期分组聚合
    val result3: DataFrame = statisticsDataDS.groupBy('dateId)
      .agg(
        sum('confirmedIncr) as "confirmedIncr", //新增确诊
        sum('confirmedCount) as "confirmedCount", //累计确诊
        sum('suspectedCount) as "suspectedCount", //累计疑似
        sum('curedCount) as "curedCount", //累计治愈
        sum('deadCount) as "deadCount" //累计死亡
      )

    //4.4.境外输入排行--注意:按照日期-城市分组聚合
    val result4: Dataset[Row] = cityDS.filter(_.cityName.contains("境外输入"))
      .groupBy('datetime, 'provinceShortName, 'pid)
      .agg(sum('confirmedCount) as "confirmedCount")
      .sort('confirmedCount.desc)

    //4.5.统计北京市的累计确诊地图
    val result5: DataFrame = cityDS.filter(_.provinceShortName.equals("北京")).select('datetime,'locationId,'provinceShortName,'cityName,'currentConfirmedCount,'confirmedCount,'suspectedCount,'curedCount,'deadCount)
    //.....


    //5.结果输出--先输出到控制台观察,最终输出到MySQL
    /*result1.writeStream
      .format("console")
      //输出模式:
      //1.append:默认的,表示只输出新增的数据,只支持简单的查询,不支持聚合
      //2.complete:表示完整模式,所有数据都会输出,必须包含聚合操作
      //3.update:表示更新模式,只输出有变化的数据,不支持排序
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      .awaitTermination()*/
    result1.writeStream
      .foreach(new BaseJdbcSink("replace into covid19_1 (datetime,currentConfirmedCount,confirmedCount,suspectedCount,curedCount,deadCount) values(?,?,?,?,?,?)") {
        override def realProcess(sql: String, row: Row): Unit = {
          //取出row中的数据
          val datetime: String = row.getAs[String]("datetime")
          val currentConfirmedCount: Long = row.getAs[Long]("currentConfirmedCount")
          val confirmedCount: Long = row.getAs[Long]("confirmedCount")
          val suspectedCount: Long = row.getAs[Long]("suspectedCount")
          val curedCount: Long = row.getAs[Long]("curedCount")
          val deadCount: Long = row.getAs[Long]("deadCount")
          //获取预编译语句对象
          ps = conn.prepareStatement(sql)
          //给sql设置参数值
          ps.setString(1,datetime)
          ps.setLong(2,currentConfirmedCount)
          ps.setLong(3,confirmedCount)
          ps.setLong(4,suspectedCount)
          ps.setLong(5,curedCount)
          ps.setLong(6,deadCount)
          ps.executeUpdate()
        }
      })
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      //.awaitTermination()

    /*result2.writeStream
      .format("console")
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      //.awaitTermination()

    result3.writeStream
      .format("console")
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      .awaitTermination()

    result4.writeStream
      .format("console")
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      .awaitTermination()

    result5.writeStream
      .format("console")
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      .awaitTermination()*/

    result2.writeStream
      .foreach(new BaseJdbcSink("replace into covid19_2 (datetime,locationId,provinceShortName,currentConfirmedCount,confirmedCount,suspectedCount,curedCount,deadCount) values(?,?,?,?,?,?,?,?)") {
        override def realProcess(sql: String, row: Row): Unit = {
          val datetime: String = row.getAs[String]("datetime")
          val locationId: Int = row.getAs[Int]("locationId")
          val provinceShortName: String = row.getAs[String]("provinceShortName")
          val currentConfirmedCount: Int = row.getAs[Int]("currentConfirmedCount")
          val confirmedCount: Int = row.getAs[Int]("confirmedCount")
          val suspectedCount: Int = row.getAs[Int]("suspectedCount")
          val curedCount: Int = row.getAs[Int]("curedCount")
          val deadCount: Int = row.getAs[Int]("deadCount")
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)
          ps.setInt(2,locationId)
          ps.setString(3,provinceShortName)
          ps.setInt(4,currentConfirmedCount)
          ps.setInt(5,confirmedCount)
          ps.setInt(6,suspectedCount)
          ps.setInt(7,curedCount)
          ps.setInt(8,deadCount)
          ps.executeUpdate()
        }
      })
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()

    result3.writeStream
      .foreach(new BaseJdbcSink("replace into covid19_3 (dateId,confirmedIncr,confirmedCount,suspectedCount,curedCount,deadCount) values(?,?,?,?,?,?)") {
        override def realProcess(sql: String, row: Row): Unit = {
          //取出row中的数据
          val dateId: String = row.getAs[String]("dateId")
          val confirmedIncr: Long = row.getAs[Long]("confirmedIncr")
          val confirmedCount: Long = row.getAs[Long]("confirmedCount")
          val suspectedCount: Long = row.getAs[Long]("suspectedCount")
          val curedCount: Long = row.getAs[Long]("curedCount")
          val deadCount: Long = row.getAs[Long]("deadCount")
          //获取预编译语句对象
          ps = conn.prepareStatement(sql)
          //给sql设置参数值
          ps.setString(1,dateId)
          ps.setLong(2,confirmedIncr)
          ps.setLong(3,confirmedCount)
          ps.setLong(4,suspectedCount)
          ps.setLong(5,curedCount)
          ps.setLong(6,deadCount)
          ps.executeUpdate()
        }
      })
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()


    result4.writeStream
      .foreach(new BaseJdbcSink("replace into covid19_4 (datetime,provinceShortName,pid,confirmedCount) values(?,?,?,?)") {
        override def realProcess(sql: String, row: Row): Unit = {
          //取出row中的数据
          val datetime: String = row.getAs[String]("datetime")
          val provinceShortName: String = row.getAs[String]("provinceShortName")
          val pid: Int = row.getAs[Int]("pid")
          val confirmedCount: Long = row.getAs[Long]("confirmedCount")
          //获取预编译语句对象
          ps = conn.prepareStatement(sql)
          //给sql设置参数值
          ps.setString(1,datetime)
          ps.setString(2,provinceShortName)
          ps.setInt(3,pid)
          ps.setLong(4,confirmedCount)
          ps.executeUpdate()
        }
      })
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()

    result5.writeStream
      .foreach(new BaseJdbcSink("replace into covid19_5 (datetime,locationId,provinceShortName,cityName,currentConfirmedCount,confirmedCount,suspectedCount,curedCount,deadCount) values(?,?,?,?,?,?,?,?,?)") {
        override def realProcess(sql: String, row: Row): Unit = {
          val datetime: String = row.getAs[String]("datetime")
          val locationId: Int = row.getAs[Int]("locationId")
          val provinceShortName: String = row.getAs[String]("provinceShortName")
          val cityName: String = row.getAs[String]("cityName")
          val currentConfirmedCount: Int = row.getAs[Int]("currentConfirmedCount")
          val confirmedCount: Int = row.getAs[Int]("confirmedCount")
          val suspectedCount: Int = row.getAs[Int]("suspectedCount")
          val curedCount: Int = row.getAs[Int]("curedCount")
          val deadCount: Int = row.getAs[Int]("deadCount")
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)
          ps.setInt(2,locationId)
          ps.setString(3,provinceShortName)
          ps.setString(4,cityName)
          ps.setInt(5,currentConfirmedCount)
          ps.setInt(6,confirmedCount)
          ps.setInt(7,suspectedCount)
          ps.setInt(8,curedCount)
          ps.setInt(9,deadCount)
          ps.executeUpdate()
        }
      })
      .outputMode("append")
      .trigger(Trigger.ProcessingTime(0))
      .option("truncate",false)
      .start()
      .awaitTermination()

  }

}
