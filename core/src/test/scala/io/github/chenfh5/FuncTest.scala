package io.github.chenfh5

import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.{AfterClass, BeforeClass, Test}

class FuncTest {
  private val LOG = LoggerFactory.getLogger(getClass)

  @BeforeClass
  def setUp(): Unit = {
    LOG.info("this is the test begin={}", OwnUtils.getTimeNow())
  }

  @AfterClass
  def shut(): Unit = {
    LOG.info("this is the test   end={}", OwnUtils.getTimeNow())
  }

  @Test(enabled = true)
  def testProc(): Unit = {
    Assert.assertEquals(println("???"), ())
  }

}
