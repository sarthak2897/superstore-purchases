package main

import akka.actor.{ActorSystem, Props}
import akkaActors.{LoggerActor, SuperStoreActor}

object Main extends App {

  //To run the application with different config file, write the command : -Dconfig.file = -Dconfig.file=C:/app.conf in the VM arguments

  //val datasource = Source.fromFile(new File ("C:\\Users\\sartnagpal\\Downloads\\Superstore_purchases.csv")).getLines().drop(1).toArray
  val actorSystem = ActorSystem("FinancialActorSystem")
  val loggerActor = actorSystem.actorOf(Props[LoggerActor], "loggerActor")
  val dataActor = actorSystem.actorOf(Props[SuperStoreActor], "dataActor")
  //val cassandraActorSystem = ActorSystem("FinancialPersistantActorSystem", ConfigFactory.load().getConfig("cassandraSpecs"))
  //val cassandraActor = cassandraActorSystem.actorOf(Props[CassandraActor], "cassandraActor")

  import SuperStoreActor._

  dataActor ! InitializeMaster(10)


}
