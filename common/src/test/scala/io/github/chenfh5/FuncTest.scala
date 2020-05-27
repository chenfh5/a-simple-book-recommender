package io.github.chenfh5

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory

class FuncTest extends FunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)

  before {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  after {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  test("success") {
    val now = OwnUtils.getTimeNow()
    assert(now(4) == '-')
  }

}
