package io.github.chenfh5

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import org.slf4j.LoggerFactory

object Handler {
  private val LOG = LoggerFactory.getLogger(getClass)

  def fetch(): Unit = {
    val favorBookList = List("时空之头号玩家", "诸天尽头")
    val books = new Fetcher(favorBookList).getBookListContent

    val out = new ObjectOutputStream(new FileOutputStream("test.dat"))
    out.writeObject(books)
    out.close()

    LOG.info("hit books=%s".format(books))
  }

  def merge(): Unit = {
    val input = {
      val in = new ObjectInputStream(new FileInputStream("test.dat"))
      val obj = in.readObject()
      in.close()
      obj.asInstanceOf[List[Book]]
    }

    //    println(input)
    val o = new Merger(3)
    val res = o.map2Weight(o.assignWeight(input))
    println(res)
  }

  def rank(): Unit = {
    val input = {
      val in = new ObjectInputStream(new FileInputStream("test.dat"))
      val obj = in.readObject()
      in.close()
      obj.asInstanceOf[List[Book]]
    }

    val o = new Merger(3)
    val input2 = o.map2Weight(o.assignWeight(input))
    val o2 = new Ranker(1110)
    val res = o2.topK(input2)

    res.foreach { e =>
      println("-------->")
      println(e._1)
      e._2.foreach(println(_))
    }
  }

  def main(args: Array[String]): Unit = {
    rank()
  }

}
