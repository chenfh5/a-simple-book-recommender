package io.github.chenfh5.http.server

import io.github.chenfh5.conf.OwnConfigReader.OwnConfig
import io.github.chenfh5.http.handler.HandlerImpl
import org.glassfish.grizzly.http.server.NetworkListener
import org.slf4j.LoggerFactory

class G2Server extends Server {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def init(): Unit = {
    LOG.info(s"G2Server host=${OwnConfig.SERVER_HOST}, port=${OwnConfig.HTTP_SERVER_PORT}")
    server.addListener(new NetworkListener("chenfh5 grizzly http server", OwnConfig.SERVER_HOST, OwnConfig.HTTP_SERVER_PORT))
    addHandler()
  }

  def addHandler(): Unit = {
    server.getServerConfiguration.addHttpHandler(HandlerImpl(), "/")
  }

}

object G2Server {
  def apply(): G2Server = new G2Server()
}
