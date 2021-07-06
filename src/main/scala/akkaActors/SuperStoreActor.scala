package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akkaActors.LoggerActor.Info
import akkaActors.Util.loggerActor


import java.io.File
import scala.io.Source

class SuperStoreActor extends Actor with ActorLogging{
  import SuperStoreActor._
  import MasterActor._
  val masterActor = context.actorOf(Props[MasterActor],"master")
  override def receive: Receive = initializeMaster.orElse(fetchSuperstorePurchases)

    def initializeMaster : Receive ={
      case InitializeMaster(noOfWorkers) =>
        log.info("Creating master actor")
    //    loggerActor ! Info("Creating master actor")
        masterActor ! InitializeWorkers(noOfWorkers)
    }

  def fetchSuperstorePurchases : Receive ={
    case FetchSuperstorePurchases =>
      log.info("Sending csv data to master")
     // loggerActor ! Info("Sending csv data to master actor")
      val superStoreData = "C:\\Users\\sartnagpal\\Downloads\\Superstore_purchases.csv"
      Source.fromFile(new File(superStoreData)).getLines().drop(1).foreach(masterActor ! ReadSuperstorePurchases(_))

  }

}

object SuperStoreActor{
  case class InitializeMaster(noOfWorkers : Int)
}
