package streams

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import main.Main.actorSystem
import model.ErrorMessage
import utility.Utility.{errorMessageFileSink, errorMessageLoggerSink}
//import utility.Utility.{errorMessageFileSink, errorMessageLoggerSink}

object ErrorHandlingStream {
  implicit val materializer = ActorMaterializer()(actorSystem)
  val sourceActor : Source[ErrorMessage,ActorRef] = Source.actorRef(10,OverflowStrategy.dropHead)

//val errorGraph = Sink.fromGraph(GraphDSL.create(){
//  implicit builder =>
//    import GraphDSL.Implicits._
//    val broadcast = builder.add(Broadcast[ErrorMessage](2))
//    broadcast.out(0) ~> errorMessageLoggerSink
//    broadcast.out(1) ~> errorMessageFileSink
//    SinkShape(broadcast.in)
//})

  val errorRef = sourceActor
    .alsoTo(errorMessageLoggerSink)
    .to(errorMessageFileSink).run()

  //Exposed the actor reference for usage in Child actor for stream processing
  //val errorRef: ActorRef = sourceActor.to(errorGraph).run()



}
