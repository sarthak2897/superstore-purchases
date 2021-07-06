package streams

import akka.Done
import akka.actor.ActorRef
import akka.stream.{ActorMaterializer, OverflowStrategy, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import akkaActors.LoggerActor.Info
import com.typesafe.config.{Config, ConfigFactory}
import main.Main.{actorSystem}
import model.{FiguresAggregator, Record}
import service.RecordToCsvService
import FiguresAggregator._
import utility.Utility._

import java.text.SimpleDateFormat
import scala.concurrent.Future

object PurchaseProcessingStream {

  implicit val materializer = ActorMaterializer()(actorSystem)
  //Creating the source actor
  val sourceActor = Source.actorRef(15,OverflowStrategy.dropHead)

val bulkReportGraph = Sink.fromGraph(GraphDSL.create(){
  implicit builder =>
    import GraphDSL.Implicits._
    val quantityFilterShape = builder.add(quantityFilterFlow)
    val financialYearFilterShape = builder.add(financialYearFilterFlow)
    val broadcast = builder.add(Broadcast[Record](2))

    quantityFilterShape.out  ~> financialYearFilterShape ~> broadcast
    broadcast.out(0) ~> bulkFinancialReportSink // Write to CSV file
    broadcast.out(1) ~> cassandraFinancialReportSink // Write to Cassandra DB
    SinkShape(quantityFilterShape.in)
})
  //Exposing Source ActorRef for usage in child actor
   val bulkReportRef: ActorRef = sourceActor.to(bulkReportGraph).run()


  val aggregatorGraph = Sink.fromGraph(GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Record](2))
      val aggregatorBroadcast = builder.add(Broadcast[Record](2))
      val categoryBroadcast = builder.add(Broadcast[Record](2))
      val financialAggregatorFlowShape = builder.add(financialYearFilterFlow)
      val categoryFilterFlowShape = builder.add(categoryFilterFlow)

      //For category wise filtered records
      categoryFilterFlowShape.out ~> broadcast
      broadcast.out(0) ~> categoryBroadcast
      categoryBroadcast.out(0) ~> categoryFilterSink  // Write to CSV file
      categoryBroadcast.out(1) ~> cassandraCategorySink // Write to Cassandra DB

      //For category wise aggregate report
      broadcast.out(1) ~> financialAggregatorFlowShape~> aggregatorBroadcast
      aggregatorBroadcast.out(0) ~> financialAggregatorSink // Write to CSV file
      aggregatorBroadcast.out(1) ~> cassandraAggregatorSink // Write to Cassandra DB
      SinkShape(categoryFilterFlowShape.in)
  })
  //Exposing Source ActorRef for usage in child actor
  val purchaseProcessingRef = sourceActor.to(aggregatorGraph).run()


}
