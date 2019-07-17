import com.typesafe.scalalogging.Logger
import org.scalatest.FlatSpec

import scala.util.{Failure, Success, Try}

class TryErrorTest extends FlatSpec{
    val logger = Logger(this.getClass)
    "Try Error" should "" in {
        /** 不会抛出异常 */
        Try(throw new Error("Boom!")) match {
            case Failure(f) => logger.error(f.getMessage)
            case _ => ???
        }

        /** !! 会抛出异常 !! */
        Try(throw new UnsatisfiedLinkError("Boom!")) match {
            case Failure(f) => logger.error(f.getMessage)
            case _ => ???
        }
    }
}
