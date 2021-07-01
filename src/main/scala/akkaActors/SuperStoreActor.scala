package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import java.io.File
import scala.io.Source

class SuperStoreActor extends Actor with ActorLogging{
  import SuperStoreActor._
  import MasterActor._
  val masterActor = context.actorOf(Props[MasterActor],"master")
  override def receive: Receive = {
    case InitializeMaster(noOfWorkers) =>
      log.info("Creating master actor")
      masterActor ! InitializeWorkers(noOfWorkers)

    case FetchSuperstorePurchases =>
      log.info("Sending csv data to master actor")
      val superStoreData = "C:\\Users\\sartnagpal\\Downloads\\Superstore_purchases.csv"
      Source.fromFile(new File(superStoreData)).getLines().drop(1).foreach(masterActor ! ReadSuperstorePurchases(_))

    case NoOfRecords(count,senderName) => log.info(s"${senderName} No of records : "+count)
  }
}

object SuperStoreActor{
  case class InitializeMaster(noOfWorkers : Int)
  //case class NoOfRecords(count : Int,senderName : String)
  case class NoOfRecords(record : String,senderName : String)
}
