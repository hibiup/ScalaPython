package com.hibiup.examples

import cats.MonadError
import cats.data.{IndexedStateT, StateT}
import cats.effect.IO
import com.typesafe.scalalogging.Logger
import jep.Jep

import scala.util.{Failure, Success, Try}

/**
  * 与 Py4J 不同，Jep 使用 JNI 来嵌入 Python. 需要以下条件:
  *
  * 1) 安装 Jep
  *       python -m pip install jep encodings
  *
  *    JEP 3.8.2 版本在 Windows 下安装需要 VS 2017.0 Build tools (MSVC 14.1) 支持，下载地址：
  *      https://my.visualstudio.com/Downloads?q=visual%20studio%202017&wt.mc_id=o~msft~vscom~older-downloads
  *
  *    MSVC 编译器版本和 VS 版本对照表：
  *      https://en.wikipedia.org/wiki/Microsoft_Visual_C%2B%2B
  *
  * 2) 需要命令行参数:
  *       PYTHONHOME=C:\path\to\python\folder
  *    或
  *       javaOptions += "-Djava.library.path=/path/to/python/jep"
  *
  * 参考: https://github.com/ninia/jep/wiki/Getting-Started
  * */

object Example_Jep_CallToPython extends App{
    val logger = Logger(this.getClass)

    /**
      * 1) 将 Jep 转成 State Monad，因为 Jep 是有状态的
      * */
    def eval(expr:String): StateT[IO, Jep, Boolean] = StateT { jep => IO{
        /**
          * 可以执行无返回值的命令，比如 import python module. 执行成功返回 boolean
          * */
        (jep, jep.eval(expr) )
    } }

    def getValue[T](expr:String): StateT[IO, Jep, T] = StateT {jep => IO{
        /**
          * 执行有返回值的命令
          * */
        (jep,  jep.getValue(expr).asInstanceOf[T])
    }}
    def runScript(script:String): StateT[IO, Jep, Unit] = StateT {jep => IO{
        /**
          * 或执行脚本
          * */
        (jep,  jep.runScript(script))
    }}

    def close:StateT[IO, Jep, Unit] = StateT{ jep => IO {
        /**
          * 关闭 Python 运行环境
          * */
        (jep, jep.close())
    }}

    /**
      * 2) 设计命令组合，并传入 Python 指令
      * */
    def pythonGreeting(name:String): StateT[IO, Jep, String] = for {
        // 执行 Python 已安装的模块
        _ <- eval("""import os""")
        current_path <- getValue[String](s"""os.getcwd()""")
        _ <- eval(s"""print("$current_path")""")

        // 从脚本获得 Python 执行模块
        // TODO：(不成功)
        //_ <- eval("""from Python.main.python import Example_Jep_CallToPython""")
        //v <- getValue[String](s"""Example_Jep_CallToPython.say_hello("$name")""")

        _ <- close
    } yield current_path


    /**
      * 3) 初始化 Jep 引擎，并执行组合
      * */
    Try {
        /**
          * 如果环境变量设置不正确，new Jep() 可能会抛出一个 Fatal 异常: UnsatisfiedLinkError 而 Try 只能处理 NonFatal，参考：
          *   https://www.scala-lang.org/api/2.9.3/scala/util/Try.html
          *
          *  Try 回避 Fatal 异常的处理机制：
          *    https://dzone.com/articles/catching-exceptions-in-scala
          *
          * 所以必须用 try...catch... 来将它转成 NonFatal 类异常然后抛出给 Try
          */
        import scala.util.control.NonFatal
        try new Jep() catch {
            case NonFatal(t) => throw t   // 如果是 NonFatal，则直接抛出，Try 会处理
            case e: Throwable => throw new RuntimeException(e)  // 否则转一下
        }
    }.map( jep =>  pythonGreeting("""John""").run(jep).attempt.unsafeRunSync()) match {
        /** 4) 处理结果 */
        case Failure(f) =>
            logger.error(f.getMessage, f)
        case Success(s) => s match {
            case Right((_, v)) => println(v)
            case Left(t) => logger.error(t.getMessage, t)
        }
    }
}
