package chen.study.util

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange

import scala.collection.mutable

/**
 * @program: covid_chen
 * @author: XiaoChen
 * @description:用来手动维护偏移量到MySQL中的工具类
 * @date: 2021-02-10 14:00
 **/
object OffsetUtils {


  /**
   * 根据参数查询偏移量信息并封装成map返回
   * @param groupId 消费者组名称
   * @param topic 主题
   * @return 偏移量信息封装成的Map
   */
  def getOffsetsMap(groupId: String, topic: String): mutable.Map[TopicPartition, Long] = {
    //1.获取连接
    val conn: Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata?useSSL=false&characterEncoding=UTF-8", "root","1nianzhijian")
    //2.编写sql
    val sql:String = "select 'partition','offset' from t_offset where groupid = ? and topic = ?"
    //3.获取预编译语句对象
    val ps:PreparedStatement = conn.prepareStatement(sql)
    //4.设置参数并执行
    //4.设置参数并执行
    ps.setString(1,groupId)
    ps.setString(2,topic)
    val rs: ResultSet = ps.executeQuery()
    //5.获取返回值并封装成Map
    val offsetsMap:mutable.Map[TopicPartition, Long] = mutable.Map[TopicPartition, Long]()
    while(rs.next()){
      val partition:Int = rs.getInt("partition")
      val offset: Int = rs.getInt("offset")
      offsetsMap += new TopicPartition(topic,partition) -> offset
    }

    //6.关闭资源
    rs.close()
    ps.close()
    conn.close()
    //7.返回Map
    offsetsMap
  }

  /**
   * 将消费者组的偏移量信息存入MySQL
   *
   * @param groupId 消费者组名称
   * @param offsets 偏移量信息
   */
  def saveOffsets(groupId: String, offsets: Array[OffsetRange]):Unit = {
    //1.加载驱动并获取连接
    val conn: Connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bigdata?useSSL=false&characterEncoding=UTF-8", "root", "1nianzhijian")
    //2.编写sql语句
    val sql:String = "replace into t_offset(`topic`, `partition`, `groupid`, `offset`) values(?,?,?,?)"
    //3.创建预编译语句对象`topic`, `partition`, `groupId`, `offset`
    val ps:PreparedStatement = conn.prepareStatement(sql)
    //4.设置参数并执行
    for (o<-offsets){
      ps.setString(1,o.topic)
      ps.setInt(2,o.partition)
      ps.setString(3,groupId)
      ps.setLong(4,o.untilOffset)
      ps.executeUpdate()
    }
    //5.关闭资源
    ps.close()
    conn.close()
  }

}
