package io.github.chenfh5

class PlaceHolder {

  def process(): Unit = {
    println("???")
  }

}

object PlaceHolder {

  def apply(): PlaceHolder = new PlaceHolder()

  def main(args: Array[String]): Unit = {
    val placeHolder = apply()
    placeHolder.process()
  }

}
