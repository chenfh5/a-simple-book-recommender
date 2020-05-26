package io.github.chenfh5

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

class PlaceHolder(queryBooks: List[String]) {
  private val LOG = LoggerFactory.getLogger(getClass)
  private val prefix = "https://www.google.com/search?q=site%3Awww.qidiantu.com%2Finfo+"
  private val bookUrlPattern = """(http|https)://www.qidiantu.com/info/\d+/c""".r
  private val pageNumPattern = """/info/\d+/c/(\d+)""".r
  private val bookListUrlPattern = """/booklist/(\d+)""".r
  private val booklistUrl = "https://www.qidiantu.com/booklist"

  def getBookIdFromGoogle: Map[String, Item] = {
    val books = mutable.Map[String, Item]()

    queryBooks.par.foreach { e =>
      val url = "%s%s".format(prefix, e)
      LOG.info("url=%s".format(url))
      val doc = Jsoup.connect(url).get
      val headlines = doc.select("div.r a")

      breakable {
        headlines.asScala.foreach { h =>
          val url = h.attr("href")
          url match {
            case bookUrlPattern(_*) =>
              books.update(e, Item(url))
              break() // TODO: only get the first one (need confirm hit)
            case _ =>
              LOG.debug("url=%s is not match".format(url))
          }
        }
      }
    }

    LOG.info("hit url=%s".format(books))
    books.toMap
  }

  def getPageNum: Map[String, Item] = {
    val books = getBookIdFromGoogle

    books.par.foreach {
      case (_, v) =>
        val doc = Jsoup.connect(v.url).get
        val headlines = doc.select("ul.pagination a")
        headlines.asScala.foreach { h =>
          val url = h.attr("href")
          url match {
            case pageNumPattern(url) =>
              val lastNum = v.pageNum
              if (url.toInt > lastNum) v.pageNum = url.toInt
            case _ =>
              LOG.debug("url=%s is not match".format(url))
          }
        }

    }

    LOG.info("hit pageNum=%s".format(books))
    books
  }

  def getBookListFromQidiantu: Map[String, Item] = {
    val books = getPageNum

    books.foreach {
      case (_, v) =>
        (0 to v.pageNum).par.foreach { d => // 403 Forbidden
          val url = "%s/%d".format(v.url, d)
          LOG.info("searching url=%s".format(url))
          val doc = Jsoup.connect(url).get
          val headlines = doc.select("div.panel-footer a")
          headlines.asScala.foreach { h =>
            val url = h.attr("href")
            url match {
              case bookListUrlPattern(url) =>
                v.bookLists.add(url.toLong)
              case _ =>
                LOG.debug("url=%s is not match".format(url))
            }
          }

        }
    }
    LOG.info("hit pageNum=%s".format(books))
    books
  }

  def getSimilarBook: Map[String, Item] = {
//        val books = getBookListFromQidiantu
    val books = Map("我真没想重生啊" -> Item("https://www.qidiantu.com/info/1015648531/c", 1, mutable.Set(630675, 629202)))
    books.foreach {
      case (_, v) =>
        v.bookLists.par.foreach { d =>
          val url = "%s/%d".format(booklistUrl, d)
          LOG.info("search booklist url=%s".format(url))
          val doc = Jsoup.connect(url).get
          val headlines = doc.select("div.media-body a h4")
          headlines.asScala.foreach { h =>
            val bookName = h.text()
            v.similarBooks.append(bookName) // concurrency put issue
          }
        }
    }
    LOG.info("hit pageNum=%s".format(books))
    books
  }

}

object PlaceHolder {
  val favorBookList = List("时空之头号玩家", "我真没想重生啊")
  def apply(): PlaceHolder = new PlaceHolder(favorBookList)

  def main(args: Array[String]): Unit = {
    val placeHolder = apply()
    placeHolder.getSimilarBook
  }

}

case class Item(
    url: String,
    var pageNum: Int = 0,
    bookLists: mutable.Set[Long] = mutable.Set.empty,
    similarBooks: mutable.ListBuffer[String] = ListBuffer())
