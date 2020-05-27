package io.github.chenfh5

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Random

class Fetcher(queryBooks: List[String]) {
  private val LOG = LoggerFactory.getLogger(getClass)
  private val prefix = "https://www.google.com/search?q=site%3Awww.qidiantu.com%2Finfo+"
  private val bookUrlPattern = """(http|https)://www.qidiantu.com/info/\d+/c""".r
  private val pageNumPattern = """/info/\d+/c/(\d+)""".r
  private val bookListUrlPattern = """/booklist/(\d+)""".r
  private val bookListUrl = "https://www.qidiantu.com/booklist"

  private val maxBookListContentSize = 10

  def getBookIdFromGoogle: Books = {
    val t0 = System.nanoTime()
    val res = queryBooks.par
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
              LOG.debug("url=%s is not match".format(url))
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
    Books(res)
  }

  def getPageNum: Books = {
    val t0 = System.nanoTime()
    val books = getBookIdFromGoogle
    val res = books.books.par.map {
      case Book(n, u, _, _) =>
        val doc = Jsoup.connect(u).get
        val headlines = doc.select("ul.pagination a")
        val tmp = headlines.asScala.map { h =>
          val url = h.attr("href")
          url match {
            case pageNumPattern(url) =>
              url.toInt
            case _ =>
              LOG.debug("url=%s is not match".format(url))
              -1
          }
        }
        Book(n, u, tmp.max) // only get max page number
    }.toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit pageNum=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    Books(res)
  }

  def getBookListUrl: Books = {
    val t0 = System.nanoTime()
    val books = getPageNum
    val res = books.books.par.map {
      case Book(n, u, p, _) =>
        val res = (0 to p).par.map { d => // 403 Forbidden
          val url = "%s/%d".format(u, d)
          LOG.debug("searching url=%s".format(url))
          val doc = Jsoup.connect(url).get
          val headlines = doc.select("div.panel-footer a")
          val tmp = headlines.asScala.map { h =>
            val url = h.attr("href")
            url match {
              case bookListUrlPattern(url) =>
                url.toLong
              case _ =>
                LOG.debug("url=%s is not match".format(url))
                -1
            }
          }
          tmp.filter(_ != -1)
        }.toList
        Book(n, u, p, res.flatten.map(e => BookList(e)))
    }.toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit bookList url=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    Books(res)
  }

  def getBookListContent: Books = {
    val t0 = System.nanoTime()
    val books = getBookListUrl
    val res = books.books.par.map {
      case Book(n, u, p, bl) =>
        val input =
          if (bl.size <= maxBookListContentSize) bl
          else {
            LOG.info("need random pick")
            Random.shuffle(bl).take(maxBookListContentSize)
          }
        val tmp = input.par
          .map {
            case BookList(u, _) =>
              val url = "%s/%d".format(bookListUrl, u)
              LOG.debug("search booklist url=%s".format(url))
              val name = try {
                val doc = Jsoup.connect(url).get
                val headlines = doc.select("div.media-body a h4")
                headlines.asScala.map(h => h.text()).toList
              } catch {
                case e: Throwable =>
                  LOG.warn("get content failed %s".format(e.getMessage))
                  List()
              }
              BookList(u, name)
          }
          .filter(_.names.nonEmpty)
          .toList
        Book(n, u, p, tmp)
    }.toList

    val durationInSec = (System.nanoTime - t0) / 1e9d
    if (LOG.isDebugEnabled) LOG.debug("hit bookList content=%s".format(res))
    else LOG.info("finish at %s".format(durationInSec))
    Books(res)
  }

}
