package io.github.chenfh5.conf

case class Book(name: String, url: String, pageNum: Int = 0, bookLists: List[BookList] = List())
case class BookList(url: Long, names: List[String] = List())

case class BookWeight(name: String, similar: Map[String, Int] = Map())

case class RequestParameter(
    var favorBookList: List[String] = List(),
    var maxBookListSize: Int = 0,
    var maxBookListContentSize: Int = 0,
    var amplifyFactor: Int = 0,
    var topK: Int = 0)
