package io.github.chenfh5

import java.io.{FileOutputStream, ObjectOutputStream}

import org.slf4j.LoggerFactory

object Handler {
  private val LOG = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val favorBookList = List("时空之头号玩家", "诸天尽头")
    val books = new Fetcher(favorBookList).getBookListContent

    val out = new ObjectOutputStream(new FileOutputStream("test.dat"))
    out.writeObject(books)
    out.close()

    LOG.info("hit books=%s".format(books))
  }

}
