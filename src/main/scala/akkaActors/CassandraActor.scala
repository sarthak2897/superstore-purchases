package akkaActors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import akkaActors.LoggerActor.{Debug, Info}
import akkaActors.Util.loggerActor
import model.Record

class CassandraActor extends PersistentActor {
  override def receiveRecover: Receive = {
    case RecoveryCompleted => loggerActor ! Info("Recovery done")
    case record : Record => loggerActor ! Info(s"Recovered $record")
  }

  override def receiveCommand: Receive = {
    case record : List[String] => persist(record){
      _ =>
        loggerActor ! Info(s"Persisting $record")
    }
  }

  override def persistenceId: String = "financial-system-cassandra-actor"
}


