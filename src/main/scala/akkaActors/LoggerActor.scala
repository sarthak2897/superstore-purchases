package akkaActors

import akka.actor.{Actor, ActorLogging}


class LoggerActor extends Actor with ActorLogging{
  import LoggerActor._
  override def receive: Receive = {

    case Info(message) => log.info(s"${message}")
    case Error(message) => log.error(s"${message}")
    case Debug(message) => log.debug(s"${message}")
  }
}

object LoggerActor{
  case class Info(message : String)
  case class Error(message : String)
  case class Debug(message : String)
}


