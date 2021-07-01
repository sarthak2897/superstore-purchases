package service

import akkaActors.LoggerActor.Info
import main.Main.loggerActor
import model.Record

import java.text.SimpleDateFormat


object DataToRecordConversionService {


  def convert(record : List[String]) : Record ={
    val dateFormatter = new SimpleDateFormat("MM/dd/yyyy")
    Record(dateFormatter.parse(record(0)),dateFormatter.parse(record(1)),record(2),record(3),
      record(4),record(5),record(6),record(7),record(8),record(9),record(10),record(11),
      record(12).toDouble,record(13).toInt,record(14).toDouble,record(15).toDouble)
  }

}
