package model

case object FiguresAggregator {
  var sales : Double =0.0
  var quantity: Int=0
  var discount : Double=0.0
  var profit : Double=0.0
  var count : Int = 0
  def aggregate(sales : Double,quantity: Int,discount : Double,profit : Double) ={
    synchronized {
      this.sales += sales
      this.quantity += quantity
      this.discount += discount
      this.profit += profit
      this.count += 1
    }
  }
}
