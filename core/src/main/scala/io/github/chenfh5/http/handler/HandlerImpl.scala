package io.github.chenfh5.http.handler
import io.github.chenfh5.LRUCache
import io.github.chenfh5.conf.RequestParameter
import io.github.chenfh5.process.{Fetcher, Merger, Ranker}
import org.glassfish.grizzly.http.server.{Request, Response}
import org.slf4j.LoggerFactory

class HandlerImpl extends HandlerTrait {
  private val LOG = LoggerFactory.getLogger(getClass)
  private val lruCache = LRUCache[Int, Map[String, List[(String, Int)]]](1000) // hash->(bookName -> TopK(similarBookName, score))

  override def doGet(request: Request, response: Response): Unit = {
    val t0 = System.nanoTime()
    // build parameter
    val requestParameter = buildRequestParameter(request)

    // exec
    val hash = requestParameter.hashCode()
    if (lruCache.containsKey(hash)) write2Response(response, lruCache.get(hash))
    else {
      if (requestParameter.favorBookList.nonEmpty) {
        // processing
        val bc =
          new Fetcher(requestParameter.favorBookList, requestParameter.maxBookListSize, requestParameter.maxBookListContentSize).getBookListContent
        val obj = new Merger(requestParameter.amplifyFactor)
        val bw = obj._map2Weight(obj.assignWeight(bc))
        val res = new Ranker(requestParameter.topK).topK(bw)
        write2Response(response, res)

        // build cache
        lruCache.put(hash, res)
      }
      else response.getWriter.write("favorBookList=%s is not valid\n".format(requestParameter.favorBookList))
    }

    val durationInSec = (System.nanoTime - t0) / 1e9d
    LOG.info("finish at %s".format(durationInSec))
    response.finish()
  }

  override def doPost(request: Request, response: Response): Unit = ???

  override def doDelete(request: Request, response: Response): Unit = ???

  private def buildRequestParameter(request: Request): RequestParameter = {
    // extractor
    val param = RequestParameter()
    param.productElementNames.foreach { k =>
      val v = request.getParameter(k)
      k match {
        case "favorBookList"          => if (v == null) param.favorBookList = List() else param.favorBookList = v.split(',').toList
        case "maxBookListSize"        => if (v == null) param.maxBookListSize = 4 else param.maxBookListSize = v.toInt
        case "maxBookListContentSize" => if (v == null) param.maxBookListContentSize = 200 else param.maxBookListContentSize = v.toInt
        case "amplifyFactor"          => if (v == null) param.amplifyFactor = 3 else param.amplifyFactor = v.toInt
        case "topK"                   => if (v == null) param.topK = 10 else param.topK = v.toInt
        case _                        =>
      }
    }
    LOG.info("RequestParameter=%s".format(param))
    param
  }

  private def write2Response(response: Response, res: Map[String, List[(String, Int)]]): Unit = {
    res.foreach { e =>
      response.getWriter.write("----> query=%s\n".format(e._1))
      e._2.foreach(e => response.getWriter.write("%s\n".format(e)))
    }
  }

}

object HandlerImpl {
  def apply() = new HandlerImpl()
}
