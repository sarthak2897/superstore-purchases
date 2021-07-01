package model

object ErrorMessage

case class ErrorMessage(erroneousRow : String, errorMsg: String, columnName : String)
