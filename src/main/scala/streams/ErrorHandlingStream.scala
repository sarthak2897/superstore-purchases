package streams

import akka.actor.ActorRef
import akka.stream.{OverflowStrategy, SinkShape}
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink, Source}
import akkaActors.LoggerActor.Error
import main.Main.loggerActor
import main.Main.actorSystem
import model.ErrorMessage
//import utility.Utility.{errorMessageFileSink, errorMessageLoggerSink}

object ErrorHandlingStream {
  implicit val materializer = actorSystem
  val sourceActor : Source[ErrorMessage,ActorRef] = Source.actorRef(10,OverflowStrategy.dropHead)

//val errorGraph = Sink.fromGraph(GraphDSL.create(){
//  implicit builder =>
//    import GraphDSL.Implicits._
//    val broadcast = builder.add(Broadcast[ErrorMessage](2))
//    broadcast.out(0) ~> errorMessageLoggerSink
//    broadcast.out(1) ~> errorMessageFileSink
//    SinkShape(broadcast.in)
//})

  //Exposed the actor reference for usage in Child actor for stream processing
 // val errorRef: ActorRef = sourceActor.to(errorGraph).run()



}
