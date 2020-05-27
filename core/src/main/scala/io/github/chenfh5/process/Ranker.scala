package io.github.chenfh5.process

import io.github.chenfh5.conf.BookWeight
import org.slf4j.LoggerFactory

class Ranker(k: Int = 11) {
  private val LOG = LoggerFactory.getLogger(getClass)

  def topK(bookWeight: List[BookWeight]): Map[String, List[(String, Int)]] = {
    val tmp = bookWeight.map { e =>
      (e.name, e.similar.toList.sortWith(_._2 > _._2).take(k))
    }

    LOG.info("finish rank")
    tmp.toMap
  }

}
