package com.hibiup.examples

import cats.effect.ExitCase.Error
import cats.effect.{IO, LiftIO}
import com.typesafe.scalalogging.Logger
import jep.Jep

import scala.util.{Failure, Success, Try}

/**
  * 与 Py4J 不同，Jep 使用 JNI 来嵌入 Python. 执行以下代码需要:
  *
  * 1) 安装 Jep
  *       python -m pip install jep
  *
  * 2) 需要命令行参数:
  *       javaOptions += "-Djava.library.path=/path/to/python/jep"
  *
  * 参考: https://github.com/ninia/jep/wiki/Getting-Started
  * */

object Example_Jep_CallToPython extends App{
    val logger = Logger(this.getClass)

    def pythonGreeting[T](name:String)(implicit jep:Try[Jep]): Try[T] = jep.map(j => {
        j.getValue(s"""JepService.sayHello("$name")""").asInstanceOf[T]
    })

    /** new Jep() 会抛出一个不可 Try 异常: UnsatisfiedLinkError 所以不得不以传统方式捕获.*/
    pythonGreeting[String]("""John""")(Try {
        try new Jep() catch {
            case e: UnsatisfiedLinkError => throw new Throwable(e)
            case t:Throwable => throw t
        }
    }) match {
        case Failure(f) => logger.error(f.getMessage, f)
        case Success(s) => println(s)
    }
}
