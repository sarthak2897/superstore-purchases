package akkaActors

import akka.actor.{Actor, ActorLogging, Props}
import akkaActors.ChildActorFSM.{ExecuteProcess, ProcessValidStream, ValidateStream}
import akkaActors.MasterActorFSM.InitializeChildWorkers
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.io.Source

class SuperStoreActor extends Actor with ActorLogging{
  import MasterActor._
  import SuperStoreActor._
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
      val propConfig = ConfigFactory.load("superstore.properties")
      //loggerActor ! Info("Sending csv data to master actor")
      val superStoreData = propConfig.getString("fileName")
      log.info(superStoreData)
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
