package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akkaActors.ChildActorFSM.{ExecuteProcess, ProcessValidStream, ValidateStream}
import akkaActors.LoggerActor.Info
import akkaActors.MasterActorFSM.InitializeChildWorkers
import akkaActors.Util.loggerActor
import main.Main.dataActor

import java.io.File
import scala.io.Source

class SuperStoreActor extends Actor with ActorLogging{
  import SuperStoreActor._
  import MasterActor._
  //val masterActor = context.actorOf(Props(new MasterActor(dataActor)),"master")
  val masterActor = context.actorOf(Props[MasterActorFSM],"masterFSMActor")
  override def receive: Receive = initializeMaster.orElse(fetchSuperstorePurchases)

    def initializeMaster : Receive ={
      case InitializeMaster(noOfWorkers) =>
        log.info("Creating master actor")
    //    loggerActor ! Info("Creating master actor")
       // masterActor ! InitializeWorkers(noOfWorkers)
        masterActor ! InitializeChildWorkers(noOfWorkers)
        self ! FetchSuperstorePurchases
    }

  def fetchSuperstorePurchases : Receive ={
    case FetchSuperstorePurchases =>
      log.info("Sending csv data to master")
      //loggerActor ! Info("Sending csv data to master actor")
      val superStoreData = "C:\\Users\\sartnagpal\\Downloads\\Superstore_purchases.csv"
      //Source.fromFile(new File(superStoreData)).getLines().drop(1).foreach(masterActor ! ReadSuperstorePurchases(_))
      //val stringList = record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList
      Source.fromFile(new File(superStoreData)).getLines().drop(1).foreach{
        record => {
          masterActor ! ExecuteProcess(record.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList)
          masterActor ! ValidateStream
          masterActor ! ProcessValidStream
        }
  }
  }

}

object SuperStoreActor{
  case class InitializeMaster(noOfWorkers : Int)
}
