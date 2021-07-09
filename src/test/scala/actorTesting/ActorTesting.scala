package actorTesting

import akka.actor.{ActorContext, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, EventFilter, ImplicitSender, TestActorRef, TestFSMRef, TestKit, TestProbe}
import akkaActors.ChildActorFSM.{ExecuteProcess, ValidateStream}
import akkaActors.LoggerActor.Info
import akkaActors.MasterActor.{FetchSuperstorePurchases, InitializeWorkers, ReadSuperstorePurchases}
import akkaActors.MasterActorFSM.InitializeChildWorkers
import akkaActors.{ChildActorFSM, CsvFileData, LoggerActor, MasterActor, MasterActorFSM, ProcessErroneousData, ProcessValidData, SuperStoreActor, UninitializedData, ValidateData}
import akkaActors.SuperStoreActor.InitializeMaster
import com.typesafe.config.ConfigFactory
import model.{ErrorMessage, Record}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.Date
import scala.concurrent.duration._

class ActorTesting extends TestKit(ActorSystem("FinancialActorSystem",ConfigFactory.load().getConfig("interceptLogs"))) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "Superstore actor" should {
    "should create Master Actor" in {
      EventFilter.info(message = "Creating master actor", occurrences = 1) intercept {
        val testProbe = TestProbe()
        val dataActor = TestActorRef(Props(new SuperStoreActor {
          override val masterActor = testProbe.ref
        }), "SuperstoreActor")
        dataActor ! InitializeMaster(10)
      }
    }
  }
    "Master Actor" should {
    "delegate creating of workers to child Actor" in {
      EventFilter.info(pattern = "Creating [0-9]+ child actors from MasterFSM Actor", occurrences = 1) intercept {

        //val dataActor = system.actorOf(Props[SuperStoreActor], "testSuperStoreActor")
        val masterActor = TestActorRef[MasterActorFSM]
        masterActor ! InitializeChildWorkers(10)
      }
    }
      "child actor should fetchPurchases" in {
      //  EventFilter.info(message = "Sending csv data to master",occurrences = 1) intercept{
       // EventFilter.debug(message = "forwarding data to child workers",occurrences = 1) intercept{
        val testProbe = TestProbe()
        val masterActor = system.actorOf(Props( new MasterActor(testProbe.ref)))
          testProbe.send(masterActor,InitializeWorkers(10))
          testProbe.expectMsg(FetchSuperstorePurchases)
       // }
      }
  }

  "Child Actor" should{
    "get the record as a list and move to validation state" in {
     // val childActor : TestActorRef[ChildActorFSM] = TestFSMRef(new ChildActorFSM
      val childActor = TestFSMRef(new ChildActorFSM)

     // val childActor: TestActorRef[ChildActorFSM] = childActor

      import Utility._
      assert(childActor.stateData == UninitializedData)
      childActor ! ExecuteProcess(invalidRecordList)
      assert(childActor.stateName == ValidateData)
      assert(childActor.stateData == CsvFileData(invalidRecordList))
    }

    "try to validate the record list but fails due to incorrect data" in {
      val childActor = TestFSMRef(new ChildActorFSM)
      import Utility._
      EventFilter.error(message = "Error detected in the record",occurrences = 1) intercept {
        childActor ! ExecuteProcess(invalidRecordList)
        assert(childActor.stateName == ValidateData)
        assert(childActor.stateData == CsvFileData(invalidRecordList))
        childActor ! ValidateStream
        assert(childActor.stateName == ProcessErroneousData)
      }
    }
    "create a record object post validation and move to process stream state" in {
      val childActor = TestFSMRef(new ChildActorFSM)
      import Utility._

        childActor ! ExecuteProcess(recordList)
        assert(childActor.stateName == ValidateData)
        assert(childActor.stateData == CsvFileData(recordList))
        childActor ! ValidateStream
        assert(childActor.stateName == ProcessValidData)
    }

  }

}

object Utility {
  val record : Record = new Record(new Date(),new Date(),"shipMode","customerName","segment","country","city","state","region","category","subCategory","name",12.00,1,10.00,100.00)

  val invalidRecordList = List(new Date().toString,new Date().toString,"shipMode","customerName","segment","country","city","state","region","category","subCategory","name","12.00","1","10.00","100.00")

  val recordList = List("11/8/2017","11/8/2017","shipMode","customerName","segment","country","city","state","region","category","subCategory","name","12.00","1","10.00","100.00")

}
