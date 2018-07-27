package com.karasiq.scalacache.larray

import scala.collection.mutable

import akka.util.ByteString
import xerial.larray.LByteArray

import com.karasiq.scalacache.Cache

object LArrayTapeCache {
  def apply[K <: AnyRef](sizeBytes: Long = 1024 * 1024 * 32): LArrayTapeCache[K] = {
    new LArrayTapeCache[K](sizeBytes)
  }
}

// Not thread safe
class LArrayTapeCache[K <: AnyRef](sizeBytes: Long) extends Cache[K, ByteString] {
  protected final case class Entry(key: K, start: Long, size: Int)

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

  def getCached(key: K, getData: () ⇒ ByteString): ByteString = {
    def fetchDataAndSave() = {
      val bytes = getData()
      addCacheEntry(key, bytes)
      bytes
    }

    entriesMap.get(key) match {
      case Some(entry) ⇒
        val data = new Array[Byte](entry.size)
        for (i ← data.indices) data(i) = cache(entry.start + i)
        ByteString.fromArrayUnsafe(data)
      case None ⇒
        fetchDataAndSave()
    }
  }

  def clearCache(key: K): Unit = {
    entriesMap.remove(key).foreach { entry ⇒
      entries -= entry.start
    }
  }

  override def finalize(): Unit = {
    cache.free
    super.finalize()
  }
}
