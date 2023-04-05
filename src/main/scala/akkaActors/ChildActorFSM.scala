package akkaActors

import akka.actor.FSM
import akkaActors.LoggerActor.Error
import akkaActors.Util.loggerActor
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
 // case class ValidateStream(record : List[String])
  case object ValidateStream
 // case class ProcessStream(record : List[String])
  case object ProcessValidStream
}

class ChildActorFSM extends FSM[ChildState,ChildData] {
  import ChildActorFSM._
  startWith(Idle,UninitializedData)

  when(Idle){
    case Event(ExecuteProcess(purchase), UninitializedData) => {
     // loggerActor ! Debug("Inside child actor")
      goto(ValidateData) using(CsvFileData(purchase))
    }
    case _ => loggerActor ! Error("Child Actor not initialized")
      stay()
  }

  when(ValidateData) {
    case Event(ValidateStream, CsvFileData(purchase)) =>{
      val errors = ValidatePurchasesService.validateData(purchase)
      if(errors.length == 0) {
        goto(ProcessValidData) using(CsvFileData(purchase))
      } else {
        log.error("Error detected in the record")
        goto(ProcessErroneousData) using ErroneousData(errors)
      }
    }
//    case Event(ExecuteProcess(purchase),CsvFileData(purchase1)) => {
//      //loggerActor ! Info("Validate : In the exceptional case ")
//      goto(ValidateData) using(CsvFileData(purchase1))
//      //stay()
//    }
    case _ => stay()
  }

  when(ProcessValidData){
    case Event(ProcessValidStream, CsvFileData(purchase)) =>{
    //  log.info("Validation successful. Processing purchase to Record case class.")
      val purchaseRecord = DataToRecordConversionService.convert(purchase)
      purchaseProcessingRef ! purchaseRecord
      bulkReportRef ! purchaseRecord
     // log.info("Processing complete!")
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

//  onTransition
//  {
//    case Idle -> ValidateData => loggerActor ! Info("From idle to ValidateData")
//    case ValidateData -> ProcessValidData => loggerActor ! Info("From validateData to process valid data")
//  }
 // initialize()
}
