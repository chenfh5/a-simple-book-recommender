package io.github.chenfh5.conf

import java.util.Properties

object OwnConfigReader {

  private val properties = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("/config/variable.properties"))
    properties
  }

  object OwnConfig {
    var SERVER_HOST = getAsStr("SERVER_HOST")

    var HTTP_SERVER_PORT = getAsInt("HTTP_SERVER_PORT")
    var NEED_AUTH = getAsBoolean("NEED_AUTH")
    var _AUTH64 = getAsStr("_AUTH64")

    override def toString: String = super.toString
  }

  private def getAsInt(str: String): Int = properties.getProperty(str).toInt

  private def getAsBoolean(str: String): Boolean = properties.getProperty(str).toBoolean

  private def getAsStr(str: String): String = properties.getProperty(str)

}
