package com.karasiq.scalacache

import scala.collection.concurrent.TrieMap

object SimpleLRUCache {
  def apply[K, V](maxSize: Int = 500): SimpleLRUCache[K, V] = {
    new SimpleLRUCache(maxSize)
  }
}

class SimpleLRUCache[K, V](maxSize: Int) extends Cache[K, V] {
  protected final case class Entry(value: V, usages: Long = 0, time: Long = 0)
  protected val entriesMap = TrieMap.empty[K, Entry]

  def getCached(key: K, getValue: () ⇒ V): V = {
    val entry = entriesMap.getOrElseUpdate(key, Entry(getValue()))
    entriesMap(key) = entry.copy(usages = entry.usages + 1, time = System.nanoTime())
    if (entriesMap.size > maxSize) clearOldEntries(maxSize / 4)
    entry.value
  }

  protected def clearOldEntries(count: Int): Unit = {
    entriesMap.toSeq
      .sortBy(_._2.time)
      .take(count)
      .foreach { case (key, _) ⇒ entriesMap -= key }
  }
}
