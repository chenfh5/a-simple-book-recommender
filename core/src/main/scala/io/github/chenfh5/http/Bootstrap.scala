package io.github.chenfh5.http

import io.github.chenfh5.http.server.G2Server
import io.github.chenfh5.process.{Fetcher, Merger, Ranker}
import org.slf4j.LoggerFactory

import scala.io.StdIn

object Bootstrap {
  private val LOG = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    dryRunOnce
//    dryRunServer
  }

  def dryRunOnce: Unit = {
    val t0 = System.nanoTime()

    // exec
    val favorBookList = List("斗破苍穹", "武动乾坤", "大主宰")
    val bc = new Fetcher(favorBookList, 100).getBookListContent
    val obj = new Merger(4)
    val bw = obj._map2Weight(obj.assignWeight(bc))
    val res = new Ranker(10).topK(bw)

    // showing
    val durationInSec = (System.nanoTime - t0) / 1e9d
    LOG.info("finish at %s".format(durationInSec))
    res.foreach { e =>
      LOG.info("----> query=%s".format(e._1))
      e._2.foreach(e => LOG.info("%s".format(e)))
    }
  }

  // @see http://localhost:8086/?names=斗破苍穹,武动乾坤&size=2&factor=11&k=21
  // @see http://localhost:8086/?names=斗破苍穹,武动乾坤
  def dryRunServer: String = {
    val g2Server = G2Server()
    g2Server.init()
    g2Server.start()
    LOG.info("Press entry key to stop the server...")
    StdIn.readLine()
  }

}
