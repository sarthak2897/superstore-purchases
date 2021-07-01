package model

import java.util.Date

object Record

case class Record(orderDate : Date,shipDate : Date,shipMode : String,customerName : String,
                  segment : String,country : String,city : String,state : String,
                  region : String,category : String,subCategory : String,name : String,
                  sales : Double,quantity : Int,discount : Double,profit : Double){
  override def toString: String = "orderDate : " + orderDate + " shipDate : "+ shipDate + " shipMode : "+ shipMode + " customerName : "+ customerName + " category : "+ category
}
