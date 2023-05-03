package akkaActors

import akka.actor.FSM
import akkaActors.LoggerActor.{Debug, Error}
import main.Main.loggerActor
import model.ErrorMessage
import service.{DataToRecordConversionService, ValidatePurchasesService}
import streams.ErrorHandlingStream.errorRef
import streams.PurchaseProcessingStream.{bulkReportRef, purchaseProcessingRef}

trait ChildState
case object Idle extends ChildState
case object ValidateData extends ChildState
case object ProcessValidData extends ChildState
case object ProcessErroneousData extends ChildState

trait ChildData
case object UninitializedData extends ChildData
case class CsvFileData(purchase : List[String]) extends ChildData
case class ErroneousData(errors : List[ErrorMessage]) extends ChildData

object ChildActorFSM{
  case class ExecuteProcess(record : List[String])
  case object ValidateStream
  case object ProcessValidStream
}

class ChildActorFSM extends FSM[ChildState,ChildData] {
  import ChildActorFSM._
  startWith(Idle,UninitializedData)

  when(Idle){
    case Event(ExecuteProcess(purchase), UninitializedData) => {
      goto(ValidateData) using(CsvFileData(purchase))
    }
    case _ => loggerActor ! Debug("Child Actor not initialized")
      stay()
  }

  when(ValidateData) {
    case Event(ValidateStream, CsvFileData(purchase)) =>{
      val errors = ValidatePurchasesService.validateData(purchase)
      if(errors.isEmpty) {
        goto(ProcessValidData) using(CsvFileData(purchase))
      } else {
        loggerActor ! Error("Error detected in the record")
        goto(ProcessErroneousData) using ErroneousData(errors)
      }
    }
    case _ => stay()
  }

  when(ProcessValidData){
    case Event(ProcessValidStream, CsvFileData(purchase)) =>{
      val purchaseRecord = DataToRecordConversionService.convert(purchase)
      //Sending records to streams
      purchaseProcessingRef ! purchaseRecord
      bulkReportRef ! purchaseRecord

      goto(Idle) using (UninitializedData)
    }
  }
  when(ProcessErroneousData) {
    case Event(ProcessValidStream,ErroneousData(errors)) =>{
      log.error("Processing erroneous record")
      errors.foreach(errorRef ! _)
      goto(Idle) using(UninitializedData)
    }
  }

}
