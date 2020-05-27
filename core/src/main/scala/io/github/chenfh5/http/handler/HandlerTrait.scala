package io.github.chenfh5.http.handler

import java.nio.charset.StandardCharsets

import io.github.chenfh5.conf.OwnConfigReader.OwnConfig
import org.glassfish.grizzly.http.Method
import org.glassfish.grizzly.http.server.{HttpHandler, Request, Response}

trait HandlerTrait extends HttpHandler {

  override def service(request: Request, response: Response): Unit = {
    // authentication
    if (OwnConfig.NEED_AUTH) {
      if (request.getAuthorization == null || OwnConfig._AUTH64 != request.getAuthorization.split(' ').last)
        illegalActionWarn(401, s"Unauthorized!", response)
    }
    request.setCharacterEncoding(StandardCharsets.UTF_8.toString)
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString)

    // real dispatch
    val method = request.getMethod
    method match {
      case Method.GET    => doGet(request, response)
      case Method.POST   => doPost(request, response)
      case Method.DELETE => doDelete(request, response)
      case _             => illegalActionWarn(505, s"Http Method $method not supported yet!", response)
    }
  }

  def doGet(request: Request, response: Response): Unit

  def doPost(request: Request, response: Response): Unit

  def doDelete(request: Request, response: Response): Unit

  def illegalActionWarn(statusCode: Int = 999, msg: String, response: Response): Unit = {
    response.setStatus(statusCode)
    response.getWriter.write(msg)
    response.finish()
  }

}
