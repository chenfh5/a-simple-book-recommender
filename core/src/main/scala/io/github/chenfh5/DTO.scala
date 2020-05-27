package io.github.chenfh5

case class Books(books: List[Book])
case class Book(name: String, url: String, pageNum: Int = 0, bookLists: List[BookList] = List())
case class BookList(url: Long, names: List[String] = List())
