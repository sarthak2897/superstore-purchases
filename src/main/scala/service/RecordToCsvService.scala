package service

import akkaActors.LoggerActor.{Debug, Error, Info}
import main.Main.loggerActor
import model.Record

import java.io.{BufferedWriter, File, FileWriter}
import java.text.SimpleDateFormat
import java.util.Date

object RecordToCsvService {

  def writeErrorsToCSVFile(list : List[String]): Unit ={
    val todayDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date())
    var csvWriter : FileWriter = null
    try{
      val file = new File("src/main/resources")
      val filteredFiles = file.listFiles().filter(file => file.getName.contains(s"Log_$todayDate"))
      if(filteredFiles.isEmpty) {
        csvWriter = new FileWriter(s"src/main/resources/Error_Log_${todayDate}.csv")
        val headings = Array("Error Message","Erroneous Column Name","Record")
        //Adding the column names to csv
        headings.foreach(field => {
          csvWriter.append(field)
          csvWriter.append(",")
        })
        csvWriter.append("\n")
        //Adding the errors
        list.foreach(data =>{
          csvWriter.append(data)
          csvWriter.append(",")
        })
        csvWriter.append("\n")
        csvWriter.close()
      }
      else{
        csvWriter = new FileWriter(s"src/main/resources/Error_Log_$todayDate.csv",true)
        list.foreach(data =>{
          csvWriter.append(data)
          csvWriter.append(",")
        })
        csvWriter.append("\n")
      }
    }catch {
      case e : Exception => loggerActor ! Error(s"Some error occured while writing error messages to csv file : ${e}")
    }
    finally {
      if(csvWriter != null)
        csvWriter.close()
    }

  }

  def writeToCSVFile(list : List[String],csvFile : String,isBulkReport : Boolean): Unit ={
    //val csvWriter = new CSVWriter(new FileWriter(s"src/main/resources/$csvFile"))
    val csvWriter = new FileWriter(s"src/main/resources/$csvFile")
    var heading : Array[String] = null;
    if(!isBulkReport){
      heading = Array("Financial Year","Category","Total sales","Total quantity","Total Discount","Total Profit")
    }
    else{
      heading = Array("Financial Year","Total sales","Avg. sales","Total quantity","Avg. quantity","Total Discount","Avg. discount","Total Profit","Avg. profit")
    }
    heading.foreach(field => {
      csvWriter.append(field)
      csvWriter.append(",")
    })
    csvWriter.append("\n")
   // csvWriter.writeNext(heading)
    list.foreach(data =>{
      csvWriter.append(data)
      csvWriter.append(",")
    })
    csvWriter.append("\n")
    //csvWriter.writeNext(dataRow)
    csvWriter.close()
    loggerActor ! Info(s"Record written to $csvFile")
  }

  def writeToCSVFile(record : Record,csvFile : String) ={
    loggerActor ! Debug("Writing to CSV file "+csvFile)
    var sinkFile : FileWriter = null;
  try {
    val file = new File(s"src/main/resources")
   // sinkFile = new BufferedWriter(new FileWriter(s"src/main/resources/${csvFile}", true))
   val files = file.listFiles()
    val filteredFiles = files.filter(file => file.getName.contains("Furniture.csv"))
    if (filteredFiles.isEmpty) {
      val headings = Array("Order Date", "Ship Date", "Ship Mode", "Customer Name",
        "Segment", "Country", "City", "State", "Region", "Category", "Sub Category",
        "Name ", "Sales", "Quantity", "Discount", "Profit")
      sinkFile = new FileWriter(s"src/main/resources/${csvFile}")
      headings.foreach(field => {
        sinkFile.append(field)
        sinkFile.append(",")
      })
      sinkFile.append("\n")
      sinkFile.close()
    }
    sinkFile = new FileWriter(s"src/main/resources/${csvFile}",true)
    val sdf = new SimpleDateFormat("MM/dd/yyyy")
    val row = Array(sdf.format(record.orderDate), sdf.format(record.shipDate), record.shipMode, record.customerName, record.segment, record.country, record.city, record.state, record.region, record.category, record.subCategory, record.name, record.sales.toString, record.quantity.toString, record.discount.toString, record.profit.toString)
    //csvWriter.writeNext(row.mkString(","))
    row.foreach(data =>{
      sinkFile.append(data)
      sinkFile.append(",")
    })
    sinkFile.append("\n")
  }
    catch{
      case e : Exception => loggerActor ! Error(s"Error occurred while writing records to csv file : $e")
    }
    finally {
      if(sinkFile != null)
        sinkFile.close()
    }
    loggerActor ! Info(s"Record written to the ${csvFile}")
  }

}
