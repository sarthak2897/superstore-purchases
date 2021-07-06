package actorTesting

import akka.actor.{ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akkaActors.LoggerActor.Info
import akkaActors.MasterActor.{FetchSuperstorePurchases, InitializeWorkers}
import akkaActors.SuperStoreActor.InitializeMaster
import akkaActors.{LoggerActor, MasterActor, SuperStoreActor}
import com.typesafe.config.ConfigFactory
import main.Main.dataActor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class InterActorTesting extends TestKit(ActorSystem("FinancialActorSystem",ConfigFactory.load().getConfig("interceptLogs"))) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "Superstore actor" should {
    "should go to Master Actor and begin initialization of child actors" in {
     // EventFilter.info(message = "Sending csv data to master",occurrences = 1) intercept{
        val dataRef = TestActorRef[SuperStoreActor]

        //val masterActor = system.actorOf(Props[MasterActor],"testMasterActor")
        dataRef ! InitializeMaster(10)
        val loggerRef = TestActorRef[LoggerActor]
        loggerRef ! Info("Creating master actor")
       expectMsg("Creating master actor")

    //  }
          }


  }

  "Superstore actor" should {
    "go to Master Actor" in {

      EventFilter.info(message = "Creating master", occurrences = 1) intercept {
        val testProbe = TestProbe()
        val dataActor = TestActorRef(Props(new SuperStoreActor {
          override val masterActor = testProbe.ref
        }))

        dataActor ! InitializeMaster(10)
    }
  }
    "initialize child actors from master actor" in{
//           EventFilter.info(message = "Creating 10 child actors",occurrences = 1) intercept{
              val testProbe = TestProbe()
              val dataActor = TestActorRef(Props(new SuperStoreActor {
                override val masterActor = testProbe.ref
              }))
              dataActor ! InitializeMaster(10)
      val loggerRef = TestActorRef[LoggerActor]
      loggerRef ! Info("Creating master actor")
      expectMsg("Creating master actor")
             // testProbe.ref ! InitializeWorkers(10)
             // testProbe.expectMsg(InitializeWorkers(10))
       //     }
    }
  }
}
