package io.github.chenfh5.http.server

import org.glassfish.grizzly.http.server.HttpServer
import org.slf4j.LoggerFactory

trait Server {
  private val LOG = LoggerFactory.getLogger(getClass)

  var server: HttpServer = new HttpServer()

  def init()

  // block here until server.stop() call
  def start(): Unit = {
    LOG.info("this is the HttpServer starting...")
    server.start()
  }

  def stop(): Unit = {
    LOG.info("this is the HttpServer stopping...")
    if (server != null) server.shutdownNow()
    LOG.info("this is the HttpServer shutdown!!!")
  }

}
