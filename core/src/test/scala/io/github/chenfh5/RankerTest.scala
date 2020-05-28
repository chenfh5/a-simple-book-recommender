package io.github.chenfh5

import io.github.chenfh5.conf.BookWeight
import io.github.chenfh5.process.Ranker
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory

class RankerTest extends AnyFunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)
  val input = List(BookWeight("c5", Map("n1" -> 1, "n2" -> 2, "n3" -> 3, "n4" -> 4, "n5" -> 5, "n6" -> 6)))

  before {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  after {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  test("success") {
    val o1 = new Ranker(4)
    val res = o1.topK(input)

    println(res)
    assert(res.size == 1)
    assert(res.head._2.size == 4)
    assert(res.head._2.head._2 == 6)
  }

  test("exceed success") {
    val o1 = new Ranker(40)
    val res = o1.topK(input)

    println(res)
    assert(res.size == 1)
    assert(res.head._2.size == 6)
    assert(res.head._2.head._2 == 6)
  }

}
