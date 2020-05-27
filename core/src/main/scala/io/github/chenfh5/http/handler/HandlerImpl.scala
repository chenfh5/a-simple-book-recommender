package io.github.chenfh5.http.handler
import io.github.chenfh5.process.{Fetcher, Merger, Ranker}
import org.glassfish.grizzly.http.server.{Request, Response}
import org.slf4j.LoggerFactory

import scala.collection.mutable

class HandlerImpl extends HandlerTrait {
  private val LOG = LoggerFactory.getLogger(getClass)
  private val globalMap = mutable.Map[String, List[(String, Int)]]() // name -> TopK(name, score)

  override def doGet(request: Request, response: Response): Unit = {
    val t0 = System.nanoTime()

    // parameter validatpr
    val favorBookList = request.getParameter("names").split(',').toList
    var maxBookListContentSize = request.getParameter("size")
    var amplifyFactor = request.getParameter("factor")
    var topK = request.getParameter("k")

    if (maxBookListContentSize == null) maxBookListContentSize = "4"
    if (amplifyFactor == null) amplifyFactor = "3"
    if (topK == null) topK = "10"
    LOG.info("names=%s, size=%s, factor=%s, k=%s".format(favorBookList, maxBookListContentSize, amplifyFactor, topK))

    // exec
    if (favorBookList.nonEmpty) {
      val bc = new Fetcher(favorBookList, maxBookListContentSize.toInt).getBookListContent
      val obj = new Merger(amplifyFactor.toInt)
      val bw = obj._map2Weight(obj.assignWeight(bc))
      val res = new Ranker(topK.toInt).topK(bw)

      // append
      res.foreach { e =>
        response.getWriter.write("----> query=%s\n".format(e._1))
        e._2.foreach(e => response.getWriter.write("%s\n".format(e)))
      }
    }
    else {
      response.getWriter.write("favorBookList=%s is not valid\n".format(favorBookList))
    }

    response.finish()
    val durationInSec = (System.nanoTime - t0) / 1e9d
    LOG.info("finish at %s".format(durationInSec))
  }

  override def doPost(request: Request, response: Response): Unit = ???

  override def doDelete(request: Request, response: Response): Unit = ???
}

object HandlerImpl {
  def apply() = new HandlerImpl()

}
