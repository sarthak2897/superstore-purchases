package main

import akka.actor.{ActorSystem, Props}
import akkaActors.{LoggerActor, SuperStoreActor}

import java.io.File
import scala.io.Source

object Main extends App {


  val datasource = Source.fromFile(new File("C:\\Users\\sartnagpal\\Downloads\\Superstore_purchases.csv")).getLines().drop(1).toArray

  val actorSystem = ActorSystem("FinancialActorSystem")
  val loggerActor = actorSystem.actorOf(Props[LoggerActor], "loggerActor")
  val dataActor = actorSystem.actorOf(Props[SuperStoreActor], "dataActor")

  import SuperStoreActor._

  dataActor ! InitializeMaster(10)


}
