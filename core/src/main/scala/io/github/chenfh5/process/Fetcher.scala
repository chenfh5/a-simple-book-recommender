package io.github.chenfh5.process

import io.github.chenfh5.conf
import io.github.chenfh5.conf.{Book, BookList}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import scala.collection.parallel.CollectionConverters._
import scala.jdk.CollectionConverters._
import scala.util.Random

class Fetcher(queryBooks: List[String], maxBookListSize: Int, maxBookListContentSize: Int) {

  private val LOG = LoggerFactory.getLogger(getClass)
  private val prefix = "https://www.google.com/search?q=site%3Awww.qidiantu.com%2Finfo+"
  private val bookUrlPattern = """(http|https)://www.qidiantu.com/info/\d+/(c|c/\d+)""".r
  private val pageNumPattern = """/info/\d+/c/(\d+)""".r
  private val bookListUrlPattern = """/booklist/(\d+)""".r
  private val bookListContentSizePattern = """.+\((\d+)本书.+\)""".r
  private val bookListUrl = "https://www.qidiantu.com/booklist"

  def _getBookIdFromGoogle: List[Book] = {
    val t0 = System.nanoTime()
    val res = queryBooks
      .map(_.trim)
      .par
      .map { n =>
        val url = "%s%s".format(prefix, n)
        LOG.info("url=%s".format(url))
        val doc = Jsoup.connect(url).get
        val headlines = doc.select("div.r a")

        val tmp = headlines.asScala.map { h =>
          val url = h.attr("href")
          url match {
            case bookUrlPattern(_*) =>
              url
            case _ =>
              if (LOG.isDebugEnabled) LOG.debug("url=%s is not match".format(url))
              ""
          }
        }
        val tmp2 = tmp.filter(_.nonEmpty)
        if (tmp2.nonEmpty) Book(n, tmp2.head) else Book(n, "") // only get the first one (need confirm hit)
      }
      .filter(_.url.nonEmpty)
      .toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit book url=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    res
  }

  private def getPageNum: List[Book] = {
    val t0 = System.nanoTime()
    val books = _getBookIdFromGoogle
    val res = books.par
      .map {
        case Book(n, u, _, _) =>
          val doc = Jsoup.connect(u).get
          val headlines = doc.select("ul.pagination a")
          val tmp = headlines.asScala.map { h =>
            val url = h.attr("href")
            url match {
              case pageNumPattern(url) =>
                url.toInt
              case _ =>
                LOG.warn("url=%s is not match".format(url))
                -1
            }
          }
          if (tmp.nonEmpty) Book(n, u, tmp.max) // only get max page number
          else Book("", "")
      }
      .filter(_.name.nonEmpty)
      .filter(_.url.nonEmpty)
      .toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    LOG.info("hit pageNum=%s, finish at %s".format(res, durationInSec))
    res
  }

  private def getBookListUrl: List[Book] = {
    val t0 = System.nanoTime()
    val books = getPageNum
    val res = books.par.map {
      case Book(n, u, p, _) =>
        val res = (0 to p).par.map { d => // 403 Forbidden
          val url = "%s/%d".format(u, d)
          if (LOG.isDebugEnabled) LOG.debug("searching url=%s".format(url))
          val doc = Jsoup.connect(url).get
          val headlines = doc.select("div.panel-footer")
          val tmp = headlines.asScala.map { h =>
            // bookListContentSize
            val txt = h.text
            val bookListContentSize = txt match {
              case bookListContentSizePattern(txt) =>
                txt.toInt
              case _ =>
                if (LOG.isDebugEnabled) LOG.debug("txt=%s is not match bookListContentSizePattern".format(txt))
                -1
            }

            // valid url
            if (bookListContentSize != -1 && bookListContentSize <= maxBookListContentSize) {
              val url = h.select("a").attr("href")
              url match {
                case bookListUrlPattern(url) =>
                  url.toLong
                case _ =>
                  if (LOG.isDebugEnabled) LOG.debug("url=%s is not match bookListUrlPattern".format(url))
                  -1
              }
            }
            else {
              if (LOG.isDebugEnabled) LOG.debug("size =%s exceed bookListContentSizePattern".format(bookListContentSize))
              -1
            }
          }
          tmp.filter(_ != -1)
        }.toList
        conf.Book(n, u, p, res.flatten.map(e => BookList(e)))
    }.toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit bookList url=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    res
  }

  def getBookListContent: List[Book] = {
    val t0 = System.nanoTime()
    val books = getBookListUrl
    val res = books.par.map {
      case Book(n, u, p, bl) =>
        val input =
          if (bl.size <= maxBookListSize) bl
          else {
            if (LOG.isDebugEnabled) LOG.debug("need random pick bookLists")
            Random.shuffle(bl).take(maxBookListSize)
          }
        val tmp = input.par
          .map {
            case BookList(u, _) =>
              val url = "%s/%d".format(bookListUrl, u)
              if (LOG.isDebugEnabled) LOG.debug("search booklist url=%s".format(url))
              val name = try {
                val doc = Jsoup.connect(url).get
                val headlines = doc.select("div.media-body a h4")
                headlines.asScala.map(h => h.text().trim).toList.filter(_.nonEmpty)
              } catch {
                case e: Throwable =>
                  LOG.warn("get content failed %s".format(e.getMessage))
                  List()
              }
              BookList(u, name)
          }
          .filter(_.names.nonEmpty)
          .toList
        conf.Book(n, u, p, tmp)
    }.toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit bookList content=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    res
  }

}
