package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akkaActors.LoggerActor.{Debug, Info}
import akkaActors.Util.loggerActor
import service.{DataToRecordConversionService, ValidatePurchasesService}
import streams.ErrorHandlingStream.errorRef
//import streams.ErrorHandlingStream.errorRef
import streams.PurchaseProcessingStream.{bulkReportRef, purchaseProcessingRef}

class ChildActor extends Actor with ActorLogging{
  import ChildActor._
  import SuperStoreActor._
  override def receive: Receive = execute.orElse(validateData).orElse(processData)

    def execute : Receive = {
      case Execute(purchase) =>
        loggerActor ! Debug("Inside child actor")
        self ! ValidateData(purchase)
    }

    def validateData : Receive ={
      case ValidateData(purchase) =>
        val errors = ValidatePurchasesService.validateData(purchase)
        if (errors.length == 0 ) self ! ProcessStream(purchase) // Processing data as a class object
        else errors.foreach(errorRef ! _)
    }

    def processData : Receive ={
      case ProcessStream(purchase) =>
        val purchaseRecord = DataToRecordConversionService.convert(purchase)
        //Add filter stream to the data
        purchaseProcessingRef ! purchaseRecord
        bulkReportRef ! purchaseRecord
    }
}
object ChildActor{
  case class Execute(record : List[String])
  case class ValidateData(record : List[String])
  case class ProcessStream(record : List[String])
}
