package model

import java.util.Date

object Record

case class Record(orderDate : Date,shipDate : Date,shipMode : String,customerName : String,
                  segment : String,country : String,city : String,state : String,
                  region : String,category : String,subCategory : String,name : String,
                  sales : Double,quantity : Int,discount : Double,profit : Double){
//  override def toString: String = "orderDate : " + orderDate + " shipDate : "+ shipDate + " shipMode : "+ shipMode + " customerName : "+ customerName + "segment : "+segment + " country : "+country + " city : "+city + " state : "+state

  override def toString: String = orderDate.toString+";"+shipDate.toString+";"+shipMode+";"+customerName+";"+segment+";"+country+";"+city+";"+state+";"+region+";"+category+";"+subCategory+";"+name+";"+sales.toString+";"+quantity.toString+";"+discount.toString+";"+profit.toString
    //+ category : "+ category
}
