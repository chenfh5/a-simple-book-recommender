package io.github.chenfh5

import java.io.{FileInputStream, ObjectInputStream}

import org.slf4j.LoggerFactory

class Merger(amplifyFactor: Int = 2) {
  private val LOG = LoggerFactory.getLogger(getClass)

  /**
    * if one bookList includes all favor books (10X weight)
    *  else 1X weight
    * @param books [[Books]]
    * @return (name -> (bookListUrl -> (bookName, weight)))
    */
  def assignWeight(books: List[Book]): Map[String, Map[Long, Map[String, Int]]] = {
    // init weight
    val weightMap = initWeight(books)

    // assign weight
    val favorBooks = books.map(_.name)
    val map =
      if (favorBooks.size > 1) {
        // find each bookList
        val tmp = weightMap.map {
          case (n, m) =>
            val tmp = m.map {
              case (url, bl) =>
                val tmp = favorBooks.map(bl.contains)
                if (tmp.contains(false)) (url, bl)
                else {
                  // assign to 10X
                  LOG.info("bookList = %s need amplify".format(url))
                  (url, bl.map(e => (e._1, e._2 + favorBooks.size * amplifyFactor)))
                }
            }
            (n, tmp)
        }
        tmp
      }
      else weightMap

    map
  }

  // convert books to Map in convenience of weight assignment
  def initWeight(books: List[Book]): Map[String, Map[Long, Map[String, Int]]] = {
    books.map {
      case Book(n, _, _, bl) =>
        val tmp = bl.map {
          case BookList(u, ns) =>
            val tmp = ns.map(e => (e, 1)).toMap // init weight to 1
            (u, tmp)
        }.toMap
        (n, tmp)
    }.toMap
  }

  def map2Weight(map: Map[String, Map[Long, Map[String, Int]]]): List[BookWeight] = {
    val res = map.map {
      case (n, m) =>
        val tmp = m.map {
          case (_, bl) =>
            bl
        }.toList
        (n, tmp)
    }
    res.map(e => BookWeight(e._1, e._2.flatten.toMap)).toList
  }

}
