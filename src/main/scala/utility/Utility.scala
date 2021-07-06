package utility

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akkaActors.LoggerActor.{Debug, Error, Info}
import akkaActors.Util.loggerActor
import com.typesafe.config.{Config, ConfigFactory}
import main.Main._
import model.FiguresAggregator.{count, discount, profit, quantity, sales}
import model.{ErrorMessage, FiguresAggregator, Record}
import service.RecordToCsvService

import java.text.SimpleDateFormat

object Utility {

  //Creating the source actor
  val sourceActor = Source.actorRef(15,OverflowStrategy.dropHead)

  //Error message sink to print the loggers on console

  val errorMessageLoggerSink = Sink.foreach[ErrorMessage](errorMsg =>loggerActor ! Error(s"Error detected -> Errorenous record : ${errorMsg.erroneousRow} with error : ${errorMsg.errorMsg} at column named ${errorMsg.columnName}"))

  //Error message sink to move the error messages to a csv file
  val errorMessageFileSink = Sink.foreach[ErrorMessage](errorMsg => {
    val errors = List(errorMsg.errorMsg,errorMsg.columnName,errorMsg.erroneousRow)
    RecordToCsvService.writeErrorsToCSVFile(errors)
  })
  //Accessing app.conf file
  //val config: Config = ConfigFactory.load("application.conf")
  val config: Config = ConfigFactory.load()

  //Accessing the categoryFilter
  val categoryFilter: String = config.getString("categoryFilter")

  //Creating the category filter flow
  val categoryFilterFlow = Flow[Record].filter(_.category.equals(categoryFilter))

  //Creating the category filter sink
  val categoryFilterSink = Sink.foreach[Record](record => RecordToCsvService.writeToCSVFile(record,config.getString("categorySinkFile")))

  //Creating sink for writing data to Cassandra DB
  val cassandraCategorySink = Sink.foreach[Record](record =>{
    loggerActor ! Debug("Inside cassandraCategorySink")
    cassandraActor ! record.toString.split(";").toList
  })

  // Financial Year filter flow
  val financialYearFilterFlow = Flow[Record].filter(record => new SimpleDateFormat("yyyy").format(record.orderDate).toInt == config.getInt("FinancialYear"))

  //Financial Aggregator Sink
  val financialAggregatorSink = Sink.foreach[Record](record =>{
    loggerActor ! Debug("Inside financialAggregatorSink")
    val fileContents = List(new SimpleDateFormat("yyyy").format(record.orderDate),record.category,formatValue(sales),quantity.toString,formatValue(discount),formatValue(profit),count.toString)
    FiguresAggregator.aggregate(record.sales,record.quantity,record.discount,record.profit)
    //Writing to CSV file
    RecordToCsvService.writeToCSVFile(fileContents,config.getString("CategoryWiseFinancialYearSinkFile"),false)
  })

  //Creating sink for writing data to Cassandra DB
  val cassandraAggregatorSink = Sink.foreach[Record](record => {
    loggerActor ! Debug("Inside cassandraAggregatorSink")
    cassandraActor ! List(new SimpleDateFormat("yyyy").format(record.orderDate),record.category,formatValue(sales),quantity.toString,formatValue(discount),formatValue(profit),count.toString)
  })
    //Quantity filter flow
  val quantityFilterFlow = Flow[Record].filter(_.quantity > config.getInt("BulkQuantityValue"))

  // Creating the Yearly Bulk Financial Report
  val bulkFinancialReportSink = Sink.foreach[Record](record =>{
    loggerActor ! Debug("Creating bulk financial report")

    val fileContents = List(new SimpleDateFormat("yyyy").format(record.orderDate),formatValue(sales),formatValue(sales/count),quantity.toString,formatValue(quantity/count),formatValue(discount),formatValue(discount/count),formatValue(profit),formatValue(profit/count),count.toString)

    //Computing the total and average of the metrics
    FiguresAggregator.aggregate(record.sales,record.quantity,record.discount,record.profit)
    //Writing to csv files
    RecordToCsvService.writeToCSVFile(fileContents,config.getString("BulkProductInsightSinkFile"),true)
  })

  //Creating sink for writing data to Cassandra DB
  val cassandraFinancialReportSink = Sink.foreach[Record](record => {
    loggerActor ! Debug("Inside cassandraFinancialReportSink")
    cassandraActor ! List(new SimpleDateFormat("yyyy").format(record.orderDate),formatValue(sales),formatValue(sales/count),quantity.toString,formatValue(quantity/count),formatValue(discount),formatValue(discount/count),formatValue(profit),formatValue(profit/count),count.toString)
  })



  def formatValue(value : Double)= {
    String.format("%.2f",value)
  }

}


