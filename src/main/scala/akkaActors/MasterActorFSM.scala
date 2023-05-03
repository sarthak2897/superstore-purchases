package akkaActors

import akka.actor.{ActorRef, FSM, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akkaActors.LoggerActor.{Debug, Info}
import main.Main.loggerActor


trait MasterState
case object IdleMaster extends MasterState
case object Active extends MasterState

trait MasterData
case object Uninitialized extends MasterData
case class RouterData(router : Router) extends MasterData

//Events
object MasterActorFSM{
  case class InitializeChildWorkers(noOfWorkers : Int)
  case class Terminated(ref : ActorRef)
  case object FetchSuperstorePurchases
  case object ProcessComplete
}

class MasterActorFSM extends FSM[MasterState,MasterData] {
  import MasterActorFSM._
  startWith(IdleMaster,Uninitialized)
  when(IdleMaster){
    case Event(InitializeChildWorkers(noOfWorkers),Uninitialized) => {
      loggerActor ! Info(s"Creating ${noOfWorkers} child actors from MasterFSM Actor")
      val children = for (i <- 1 to noOfWorkers) yield {
        val child = context.actorOf(Props[ChildActorFSM], s"child-actor_$i")
        context.watch(child)
        ActorRefRoutee(child)
      }
      val router = Router(RoundRobinRoutingLogic(), children)
      goto(Active) using RouterData(router)
    }
  }

  when(Active) {
    case Event(Terminated(ref), RouterData(router)) => {
      loggerActor ! Debug(s"Terminating actor ${ref}")
      router.removeRoutee(ref)
      val newChild = context.actorOf(Props[ChildActorFSM], "child_new")
      context.watch(newChild)
      router.addRoutee(newChild)

      goto(Active) using RouterData(router)
    }

    case Event(childEvent,RouterData(router)) => {
      loggerActor ! Debug("Routing data to child actor");
      router.route(childEvent,sender())
      stay()
    }
  }
}
