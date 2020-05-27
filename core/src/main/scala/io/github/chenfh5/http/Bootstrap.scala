package io.github.chenfh5.http

import io.github.chenfh5.http.server.G2Server

object Bootstrap {

  def main(args: Array[String]): Unit = {
    val g2Server = G2Server()
    g2Server.init()
    g2Server.start()
    Thread.sleep(3600 * 1000) // server persistent manually
  }

  def dryRun = {
    val url = """http://localhost:8086/?names=斗破苍穹,武动乾坤&size=2&factor=11&k=21"""
    val url2 = """http://localhost:8086/?names=斗破苍穹,武动乾坤"""
  }

}
