package io.github.chenfh5

import io.github.chenfh5.conf.{Book, BookList}
import io.github.chenfh5.process.Merger
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory

class MergerTest extends FunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)

  val input =
    List(
      Book("n1", "u1", 1, List(BookList(11, List("bl1", "n1")))),
      Book("n2", "u2", 2, List(BookList(22, List("bl2", "n2")))))

  val input2 = List(
    Book("n1", "u1", 1, List(BookList(11, List("bl1", "n1")))),
    Book("n2", "u2", 2, List(BookList(22, List("bl2", "n2", "n1")))))

  before {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  after {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  test("success initWeight") {
    val o1 = new Merger(4)
    val res = o1._initWeight(input)

    println(res)
    assert(res.size == 2)
    assert(res("n2").head._1 == 22)
    assert(res("n2").head._2.head._1 == "bl2")
    assert(res("n2").head._2.head._2 == 1)
  }

  test("success assignWeight without 10X") {
    val o1 = new Merger(4)
    val res = o1.assignWeight(input)

    println(res)
    assert(res.size == 2)
    assert(res("n2").head._1 == 22)
    assert(res("n2").head._2.head._1 == "bl2")
    assert(res("n2").head._2.head._2 == 1)
  }

  test("success assignWeight with 10X") {
    val o1 = new Merger(4)
    val res = o1.assignWeight(input2)

    println(res)
    assert(res.size == 2)
    assert(res("n2").head._1 == 22)
    assert(res("n2").head._2.head._1 == "bl2")
    assert(res("n2").head._2.head._2 == 9)
  }

  test("success map2Weight") {
    val o1 = new Merger(4)
    val input3 = o1.assignWeight(input2)
    val res = o1._map2Weight(input3)

    println(res)
    assert(res.size == 2)
    assert(res.last.name == "n2")
    assert(res.last.similar("n2") == 9)
  }

}
