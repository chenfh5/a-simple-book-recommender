package io.github.chenfh5

import org.slf4j.LoggerFactory

object Handler {
  private val LOG = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val favorBookList = List("时空之头号玩家", "我真没想重生啊")
    val books = new Fetcher(favorBookList).getBookListContent
    LOG.info("hit books=%s".format(books))
  }

}
