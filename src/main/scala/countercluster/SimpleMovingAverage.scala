package countercluster
import com.google.common.collect.EvictingQueue

case class SimpleMovingAverage(
    intervalSec: Long,
    numOfIntervals: Int,
    initialCount: Int = 0,
    _startedAt: Option[Long] = None,
    _queue: Option[EvictingQueue[Int]] = None) {
  import SimpleMovingAverage._
  require(intervalSec > 0)
  private var startedAt = _startedAt.getOrElse(System.currentTimeMillis() / 1000)
  private val queue = _queue.getOrElse(EvictingQueue.create[Int](numOfIntervals))
  private var _count = initialCount

  def increment: Unit = { refresh; this._count += 1 }

  def refresh: Unit = {
    val now = System.currentTimeMillis() / 1000
    val expiredSlots = (now - startedAt) / intervalSec
    if (expiredSlots > 0) {
      queue.add(_count)
      this.startedAt = now
      this._count = 0
    }
    (2L to expiredSlots).foreach(_ => queue.add(0))
  }

  def value: Double =
    if (queue.size == 0) { 0 }
    else { values.sum.toDouble / values.size }

  def values: List[Int] = {
    var result: List[Int] = Nil
    val it = queue.iterator
    while (it.hasNext) { result = it.next :: result }
    result
  }
  def count = _count

  def serialize =
    (intervalSec :: numOfIntervals :: startedAt :: _count :: values).
      mkString(delimiter)
}

object SimpleMovingAverage {
  val delimiter = ";"
  def makeQueue(size: Int, values: List[String]) = {
    val queue = EvictingQueue.create[Int](size)
    values.map(_.toInt).reverse.foreach(queue.add(_))
    queue
  }
  def parse(string: String) = string.split(delimiter).toList match {
    case intervalSec :: _size :: _startedAt :: count :: values =>
      val size = _size.toInt
      val startedAt = _startedAt.toLong
      SimpleMovingAverage(intervalSec.toLong, size, count.toInt, Some(startedAt), Some(makeQueue(size, values)))
    case _ => sys.error(s"Malformed SimpleMovingAverage '$string'")
  }
}
