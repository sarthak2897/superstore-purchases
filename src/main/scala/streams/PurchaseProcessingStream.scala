package streams

import akka.Done
import akka.actor.ActorRef
import akka.stream.{OverflowStrategy, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import akkaActors.LoggerActor.Info
import com.typesafe.config.{Config, ConfigFactory}
import main.Main.{actorSystem, loggerActor}
import model.{FiguresAggregator, Record}
import service.RecordToCsvService
import FiguresAggregator._
import utility.Utility._

import java.text.SimpleDateFormat
import scala.concurrent.Future

object PurchaseProcessingStream {

  implicit val materializer = actorSystem
  //Creating the source actor
  val sourceActor = Source.actorRef(15,OverflowStrategy.dropHead)

val bulkReportGraph = Sink.fromGraph(GraphDSL.create(){
  implicit builder =>
    import GraphDSL.Implicits._
    val quantityFilterShape = builder.add(quantityFilterFlow)
    val financialYearFilterShape = builder.add(financialYearFilterFlow)
    quantityFilterShape  ~> financialYearFilterShape ~> bulkFinancialReportSink
    SinkShape(quantityFilterShape.in)
})
   val bulkReportRef: ActorRef = sourceActor.to(bulkReportGraph).run()


  val aggregatorGraph = Sink.fromGraph(GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Record](2))
      val financialAggregatorFlowShape = builder.add(financialYearFilterFlow)
      val categoryFilterFlowShape = builder.add(categoryFilterFlow)
      categoryFilterFlowShape.out ~> broadcast
      broadcast.out(0) ~> categoryFilterSink
      broadcast.out(1) ~> financialAggregatorFlowShape~> financialAggregatorSink
      SinkShape(categoryFilterFlowShape.in)
  })
  //Exposing Source ActorRef for usage in child actor
  val purchaseProcessingRef = sourceActor.to(aggregatorGraph).run()


}
