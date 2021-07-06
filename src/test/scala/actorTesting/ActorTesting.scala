package actorTesting

import akka.actor.{ActorContext, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akkaActors.LoggerActor.Info
import akkaActors.MasterActor.{FetchSuperstorePurchases, InitializeWorkers, ReadSuperstorePurchases}
import akkaActors.{LoggerActor, MasterActor, SuperStoreActor}
import akkaActors.SuperStoreActor.InitializeMaster
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

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
      EventFilter.info(pattern = "Creating [0-9]+ child actors", occurrences = 1) intercept {

        //val dataActor = system.actorOf(Props[SuperStoreActor], "testSuperStoreActor")
        val masterActor = TestActorRef[MasterActor]
        masterActor ! InitializeWorkers(10)
      }
    }
      "child actor should readPurchases" in {
     //   EventFilter.info(message = "Sending csv data to master",occurrences = 1) intercept{
       // EventFilter.debug(message = "forwarding data to child workers",occurrences = 1) intercept{
         // val superStoreActor = system.actorOf(Props[SuperStoreActor])
         val masterActor= TestActorRef[MasterActor]
          masterActor ! InitializeWorkers(10)
          expectMsg(FetchSuperstorePurchases)
      //  }
      }
  }

}
