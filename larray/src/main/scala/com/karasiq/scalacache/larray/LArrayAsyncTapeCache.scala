package com.karasiq.scalacache.larray

import java.util.concurrent.Executors

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

import akka.util.ByteString
import xerial.larray.LByteArray

import com.karasiq.scalacache.Cache

object LArrayAsyncTapeCache {
  def apply[K <: AnyRef](sizeBytes: Long = 1024 * 1024 * 32): LArrayAsyncTapeCache[K] = {
    new LArrayAsyncTapeCache(sizeBytes)
  }
}

class LArrayAsyncTapeCache[K <: AnyRef](sizeBytes: Long) extends Cache[K, Future[ByteString]] {
  protected final case class Entry(key: K, start: Long, size: Int)
  protected implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  protected val cache = new LByteArray(sizeBytes)
  protected val entries = mutable.TreeMap.empty[Long, Entry]
  protected val entriesMap = mutable.AnyRefMap.empty[K, Entry]
  protected var currentPosition = 0

  protected def addCacheEntry(key: K, data: ByteString): Unit = {
    if (data.isEmpty || data.length > sizeBytes || entriesMap.contains(key)) return
    val position = if (data.length > sizeBytes - currentPosition) 0 else currentPosition
    for (i ← data.indices) cache(position + i) = data(i)

    entries.range(position, position + data.length).foreach { case (position, entry) ⇒
      entries -= position
      entriesMap -= entry.key
    }

    val entry = Entry(key, position, data.length)
    entries(position) = entry
    entriesMap(key) = entry
    currentPosition = position + data.length
    // println(s"Cache at $position = $chunk")
  }

  def getCached(key: K, getData: () ⇒ Future[ByteString]): Future[ByteString] = {
    def fetchDataAndSave() = {
      val future = getData()
      future.map { bs ⇒
        addCacheEntry(key, bs)
        bs 
      }
    }

    if (entriesMap.contains(key)) {
      val future = Future {
        entriesMap.get(key) match {
          case Some(entry) ⇒
            val data = new Array[Byte](entry.size)
            for (i ← data.indices) data(i) = cache(entry.start + i)
            Future.successful(ByteString.fromArrayUnsafe(data))
          case None ⇒
            fetchDataAndSave()
        }
      }
      future.flatten
    } else {
      fetchDataAndSave()
    }
  }

  def clearCache(key: K): Unit = {
    entriesMap.remove(key).foreach { entry ⇒
      entries -= entry.start
    }
  }

  override def finalize(): Unit = {
    executionContext.shutdown()
    cache.free
    super.finalize()
  }
}
