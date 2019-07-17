package com.hibiup.examples

/**
  * 案例参考：https://www.py4j.org/advanced_topics.html#using-the-traditional-javagateway-gatewayserver
  */

/** *
  * 1) 定义一个用于 Python 的 interface
  *
  * 因为 Scala 不是动态语言，因此它需要预先知道 Python 方法的签名。这个签名将由 Python 来实现。参考 Python 脚本
  */
trait PythonHello {
    def sayHello(msg: String):String
}

import scala.concurrent.Future
import cats.effect.IO
import com.typesafe.scalalogging.Logger

/**
  * ClientServer (推荐)
  * */
object Example_Call2Python_With_ClientServer extends App{
    import py4j.ClientServer

    val logger = Logger(this.getClass)

    /**
      * 3) ClientServer 尝试连接 Python 端启动的 ClientServer 以获得注册的接口
      *
      * 　也可以用 GatewayServer，但是 Python 端必须改用 JavaGateway. 服务端 ClientServer 不支持 GatewayServer 交互。
      * */
    val gateway:ClientServer = new ClientServer()
    Future.successful(gateway.startServer())

    val service = IO{
        gateway.getPythonServerEntryPoint(Array[Class[_]](classOf[PythonHello])).asInstanceOf[PythonHello]
    }

    (for {
        io <- service
        res <- IO(io.sayHello("Java"))
    } yield res).unsafeRunAsync{
        case Right(msg) =>
            println(s"Receive message: $msg")
        case Left(t) =>
            logger.error(t.getMessage, t)
    }

    gateway.shutdown()
}

/**
  * GatewayServer
  * */
object Example_Call2Python_With_GatewayServer extends App{
    import py4j.GatewayServer

    /**
      * 3) GatewayServer 可以连接 Python 段启动的 JavaGateway 或 ClientServer 以获得注册的接口. 但是多次双向调用可能产生异常。
      * */
    val gateway:GatewayServer = new GatewayServer()
    Future.successful(gateway.start())  // 参数 fork 表示 gateway 是否运行在一个新线程中。

    // Very unsafe!! doing it with IO
    val service = gateway.getPythonServerEntryPoint(Array[Class[_]](classOf[PythonHello])).asInstanceOf[PythonHello]
    val msg = service.sayHello("Java")
    println(s"Receive message: $msg")

    gateway.shutdown()
}