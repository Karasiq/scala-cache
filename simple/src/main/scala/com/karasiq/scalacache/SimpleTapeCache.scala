package com.karasiq.scalacache

import scala.collection.concurrent.TrieMap

object SimpleTapeCache {
  def apply[K, V](maxSize: Int = 500): SimpleTapeCache[K, V] = {
    new SimpleTapeCache(maxSize)
  }
}

class SimpleTapeCache[K, V](maxSize: Int) extends Cache[K, V] {
  protected var keysSeq = Seq.empty[K]
  protected val valuesMap = TrieMap.empty[K, V]

  def getCached(key: K, getValue: () ⇒ V): V = {
    val value = valuesMap.getOrElseUpdate(key, {
      keysSeq :+= key
      getValue()
    })
    if (keysSeq.length > maxSize) clearOldEntries(maxSize / 4)
    value
  }

  protected def clearOldEntries(i: Int): Unit = {
    val (drop, keep) = keysSeq.splitAt(i)
    drop.foreach(valuesMap -= _)
    keysSeq = keep
  }
}
