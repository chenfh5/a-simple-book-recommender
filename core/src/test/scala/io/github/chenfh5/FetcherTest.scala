package io.github.chenfh5

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory

class FetcherTest extends FunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)
  private val obj = stub[Fetcher]

  before {
    (obj.getBookIdFromGoogle _).when().returns(List(Book("n1", "u1"), Book("n2", "u2")))
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  after {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  test("getBookIdFromGoogle success") {
    val res = obj.getBookIdFromGoogle
    println(res)
    assert(res.size == 2)
  }

}
