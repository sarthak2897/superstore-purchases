package service

import akkaActors.LoggerActor.Debug
import main.Main.loggerActor
import model.ErrorMessage

import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object ValidatePurchasesService {


  def validateData(record: List[String]): List[ErrorMessage] = {

    loggerActor ! Debug("Starting validation of the record")

    var errorList: ListBuffer[ErrorMessage] = ListBuffer()
    if (record.head.isEmpty || !isDateValid(record.head))
      errorList += ErrorMessage(record.mkString("-"), "Order Date not found " +
        "or has invalid format", "Order Date")
    if (record(1).isEmpty || !isDateValid(record(1)))
      errorList += ErrorMessage(record.mkString("-"), "Ship Date not found or" +
        " has invalid format", "Ship Date")
    if (record(2).isEmpty)
      errorList += ErrorMessage(record.mkString("-"), "Ship mode not found",
        "Ship Mode")
    if (record(3).isEmpty)
      errorList += ErrorMessage(record.mkString("-"), "Customer name not " +
        "found", "Customer Name")
    errorList.toList
  }

  def isDateValid(date: String): Boolean = {
    val sdf = new SimpleDateFormat("MM/dd/yyyy")
    //val dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val triedDate = Try {
      sdf.parse(date)
    }
    triedDate match {
      case Success(value) => if (value == null) false else true
      case Failure(exception) =>
        //loggerActor ! Error("Error occurred in validating date: "+exception)
        false
    }
  }
}

