package akkaActors

import akka.actor.{Actor, ActorLogging, Props}
import akkaActors.ChildActorFSM.{ExecuteProcess, ProcessValidStream, ValidateStream}
import akkaActors.LoggerActor.Info
import akkaActors.MasterActorFSM.{FetchSuperstorePurchases, InitializeChildWorkers}
import com.typesafe.config.ConfigFactory
import main.Main.loggerActor

import java.io.File
import scala.io.Source

class SuperStoreActor extends Actor with ActorLogging{
  import SuperStoreActor._
  val masterActor = context.actorOf(Props[MasterActorFSM],"masterFSMActor")
  override def receive: Receive = initializeMaster.orElse(fetchSuperstorePurchases)

  def initializeMaster : Receive ={
    case InitializeMaster(noOfWorkers) =>
      loggerActor ! Info("Creating master actor")
      masterActor ! InitializeChildWorkers(noOfWorkers)
      self ! FetchSuperstorePurchases
  }

  def fetchSuperstorePurchases : Receive = {
    case FetchSuperstorePurchases =>
      loggerActor ! Info("Sending csv data to master")
      val propConfig = ConfigFactory.load("superstore.properties")
      val superStoreData = propConfig.getString("fileName")
      val source = Source.fromFile(new File(superStoreData)).getLines()
      source.drop(1).foreach {
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
