package streams

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import main.Main.actorSystem
import utility.Utility._

object PurchaseProcessingStream {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
  //Creating the source actor
  val sourceActor: Source[Nothing, ActorRef] = Source.actorRef(15,OverflowStrategy.dropHead)




//  val purchaseProcessingRef: ActorRef = sourceActor
//    .via(categoryFilterFlow)
//    .alsoTo(financialYearFilterFlow
//      .alsoTo(financialAggregatorSink)
//      .to(Sink.ignore))
//    .alsoTo(Sink.ignore)
//    .to(categoryFilterSink.async).run()

  //Checkpoint 5 & 6
  val purchaseProcessingRef = sourceActor
    .via(categoryFilterFlow)
    .alsoTo(financialYearFilterFlow.to(financialAggregatorSink))
    .to(categoryFilterSink).run()

  //Checkpoint 7
  val bulkReportRef: ActorRef =   sourceActor
    .via(quantityFilterFlow)
    .via(financialYearFilterFlow)
    //.alsoTo(Sink.ignore)
    .to(bulkFinancialReportSink).run()
    //.toMat(bulkFinancialReportSink)(Keep.right).run()


  //Using Graph DSL
  //val bulkReportGraph = Sink.fromGraph(GraphDSL.create(){
  //  implicit builder =>
  //    import GraphDSL.Implicits._
  //    val quantityFilterShape = builder.add(quantityFilterFlow)
  //    val financialYearFilterShape = builder.add(financialYearFilterFlow)
  //    val broadcast = builder.add(Broadcast[Record](2))
  //
  //    quantityFilterShape.out  ~> financialYearFilterShape ~> broadcast
  //    broadcast.out(0) ~> bulkFinancialReportSink // Write to CSV file
  //    broadcast.out(1) ~> cassandraFinancialReportSink // Write to Cassandra DB
  //    SinkShape(quantityFilterShape.in)
  //})
  //Exposing Source ActorRef for usage in child actor
  //val bulkReportRef: ActorRef = sourceActor.to(bulkReportGraph).run()


  //  val aggregatorGraph = Sink.fromGraph(GraphDSL.create(){
  //    implicit builder =>
  //      import GraphDSL.Implicits._
  //      val broadcast = builder.add(Broadcast[Record](2))
  //      val aggregatorBroadcast = builder.add(Broadcast[Record](2))
  //      val categoryBroadcast = builder.add(Broadcast[Record](2))
  //      val financialAggregatorFlowShape = builder.add(financialYearFilterFlow)
  //      val categoryFilterFlowShape = builder.add(categoryFilterFlow)
  //
  //      //For category wise filtered records
  //      categoryFilterFlowShape.out ~> broadcast
  //      broadcast.out(0) ~> categoryBroadcast
  //      categoryBroadcast.out(0) ~> categoryFilterSink  // Write to CSV file
  //      categoryBroadcast.out(1) ~> cassandraCategorySink // Write to Cassandra DB
  //
  //      //For category wise aggregate report
  //      broadcast.out(1) ~> financialAggregatorFlowShape~> aggregatorBroadcast
  //      aggregatorBroadcast.out(0) ~> financialAggregatorSink // Write to CSV file
  //      aggregatorBroadcast.out(1) ~> cassandraAggregatorSink // Write to Cassandra DB
  //      SinkShape(categoryFilterFlowShape.in)
  //  })
  //Exposing Source ActorRef for usage in child actor
  // val purchaseProcessingRef = sourceActor.to(aggregatorGraph).run()


}
