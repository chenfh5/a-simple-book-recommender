package io.github.chenfh5.http

import io.github.chenfh5.OwnUtils
import io.github.chenfh5.conf.OwnConfigReader.OwnConfig
import io.github.chenfh5.http.server.G2Server
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory
import scalaj.http.Http

import scala.collection.parallel.CollectionConverters._

class GrizzlyHttpTest extends AnyFunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)

  before {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  after {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  test("http call process success but Exception") {
    val g2Server = G2Server()

    (0 to 1).toList.par.foreach {
      case row if row % 2 == 0 =>
        println(s"server persistent manually thread id=${Thread.currentThread().getId}")
        g2Server.init()
        g2Server.start()
        while (g2Server.server.isStarted) {
          Thread.sleep(1000)
        }
      case row if row % 2 == 1 =>
        while (!g2Server.server.isStarted) {
          Thread.sleep(1000)
        }
        println(s"wait for server bootstrap thread id=${Thread.currentThread().getId}")
        val resp = Http(url = "http://%s:%s/".format(OwnConfig.SERVER_HOST, OwnConfig.HTTP_SERVER_PORT))
          .header("Authorization", OwnConfig._AUTH64) // default Method is `GET`
          .param("favorBookList", "斗破苍穹,武动乾坤")
          .param("maxBookListSize", "12")
          .param("maxBookListContentSize", "80")
          .param("amplifyFactor", "2")
          .param("topK", "10")
          .timeout(3000, 60000)
          .asString

        LOG.info("resp=%s".format(resp.body))
        assert(resp.code == 200)
        g2Server.stop()
    }
  }

  test("google http call process success but Exception") {
    val url = "https://www.google.com/search?q=site%3Awww.qidiantu.com%2Finfo+斗破苍穹"
    val resp = Http(url = url).timeout(3000, 60000).asString

    println(resp)
    println(resp.code)
    println(resp.body)
  }

}
