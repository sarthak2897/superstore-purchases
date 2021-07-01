package akkaActors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akkaActors.LoggerActor.Info
import main.Main.loggerActor
import service.{DataToRecordConversionService, ValidatePurchasesService}
//import streams.ErrorHandlingStream.errorRef
import streams.PurchaseProcessingStream.{bulkReportRef, purchaseProcessingRef}

class ChildActor extends Actor with ActorLogging{
  import ChildActor._
  import SuperStoreActor._
  var count = 0;
  override def receive: Receive = {

    case Execute(purchase) =>
      self ! ValidateData(purchase)

    case ValidateData(purchase) =>
      val errors = ValidatePurchasesService.validateData(purchase)
      if (errors.length == 0 ) self ! ProcessStream(purchase) // Processing data as a class object
     // else errors.foreach(errorRef ! _)

    case ProcessStream(purchase) =>
          val purchaseRecord = DataToRecordConversionService.convert(purchase)
                //Add filter stream to the data
      purchaseProcessingRef ! purchaseRecord
        bulkReportRef ! purchaseRecord
  }
}
object ChildActor{
  //case class Execute(record : String,sender : ActorRef)
  case class Execute(record : List[String])
  case class ValidateData(record : List[String])
  case class ProcessStream(record : List[String])
}
