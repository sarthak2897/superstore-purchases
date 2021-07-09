package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akkaActors.ChildActorFSM.{ExecuteProcess, ProcessValidStream, ValidateStream}
import akkaActors.LoggerActor.{Debug, Info}
import akkaActors.Util.loggerActor

class MasterActor(parent : ActorRef) extends Actor with ActorLogging{
  import MasterActor._
  import ChildActor._
  var childrenRef : Seq[ActorRef]= Seq()
  var originalSender : ActorRef = null

  // Logic without Routers

//  override def receive: Receive = {
//    case InitializeWorkers(noOfWorkers) =>
//      log.info(s"Creating $noOfWorkers child actors")
//      val children = for(i <- 1 to noOfWorkers) yield
//        context.actorOf(Props[ChildActor],s"child_actor_$i")
//        //child ! Execute
//      childrenRef = children
//      sender() ! FetchSuperstorePurchases
//    case ReadSuperstorePurchases(record) =>
//      originalSender = sender()
//      println("forwarding data to child workers")
//      childrenRef(0) ! Execute(record,originalSender)
//  }

  var routerRef : Router = null
  override def receive: Receive = initializeWorkers.orElse(childTerminated).orElse(readSuperstorePurchases).orElse(validateData).orElse(processValidData)

        def initializeWorkers : Receive = {
          case InitializeWorkers(noOfWorkers) => {
           log.info(s"Creating $noOfWorkers child actors")
            //loggerActor ! Info(s"Creating $noOfWorkers child actors")
            val children = for (i <- 1 to noOfWorkers) yield {
              val child = context.actorOf(Props[ChildActor], s"child-actor_$i")
              context.watch(child)
              ActorRefRoutee(child)
            }
            val router = Router(RoundRobinRoutingLogic(), children)
            routerRef = router
            parent ! FetchSuperstorePurchases
          }
        }
  def childTerminated : Receive = {
    case Terminated(ref) =>
      routerRef.removeRoutee(ref)
      val newChild = context.actorOf(Props[ChildActor], "child_new")
      context.watch(newChild)
      routerRef.addRoutee(newChild)
  }
  def readSuperstorePurchases : Receive = {
    case ReadSuperstorePurchases(record) =>
      log.debug("forwarding data to child workers")
      originalSender = sender()
      val stringList = record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList
     // loggerActor ! Debug("forwarding data to child workers")
      routerRef.route(Execute(stringList), self)
  }

  def validateData : Receive = {
    case ValidatePurchase => routerRef.route(ValidateStream,self)
  }

  def processValidData : Receive = {
  case ProcessValidPurchase => routerRef.route(ProcessValidStream,self)
  }
}

object MasterActor{
  case class InitializeWorkers(workers : Int)
  case object FetchSuperstorePurchases
  case class ReadSuperstorePurchases(record : String)
  case object ValidatePurchase
  case object ProcessValidPurchase
  //def props(parent: ActorRef): Props = Props(new MasterActor(parent))
}
