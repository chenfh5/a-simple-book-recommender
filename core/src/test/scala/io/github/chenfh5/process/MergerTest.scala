package io.github.chenfh5.process

import io.github.chenfh5.OwnUtils
import io.github.chenfh5.conf.{Book, BookList}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory

class MergerTest extends AnyFunSuite with MockFactory with BeforeAndAfter {
  private val LOG = LoggerFactory.getLogger(getClass)

  val input =
    List(Book("n1", "u1", 1, List(BookList(11, List("bl1", "n1")))), Book("n2", "u2", 2, List(BookList(22, List("bl2", "n2")))))

  val input2 = List(Book("n1", "u1", 1, List(BookList(11, List("bl1", "n1")))), Book("n2", "u2", 2, List(BookList(22, List("bl2", "n2", "n1")))))

  val input3 = List(
    Book("n1", "u1", 1, List(BookList(11, List("bl1", "n1")), BookList(12, List("bl2", "n1")))),
    Book("n2", "u2", 2, List(BookList(21, List("bl1", "n2", "n1")), BookList(22, List("bl2", "n2", "n3")))))

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
    val tmp = o1.assignWeight(input3)
    val res = o1._map2Weight(tmp)

    println(res)
    assert(res.size == 2)
    assert(res.filter(e => e.name == "n1").head.similar("bl1") == 1)
    assert(res.filter(e => e.name == "n1").head.similar("bl2") == 1)
    assert(res.filter(e => e.name == "n2").head.similar("bl1") == 9)
    assert(res.filter(e => e.name == "n2").head.similar("bl2") == 1)
  }

}
