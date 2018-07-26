package com.karasiq.scalacache.larray

import akka.util.ByteString
import xerial.larray.LByteArray

import com.karasiq.scalacache.{Cache, SimpleLRUCache}

object LArrayLRUCache {
  def apply[K](maxSize: Int = 500): LArrayLRUCache[K] = {
    new LArrayLRUCache[K](maxSize)
  }
}

class LArrayLRUCache[K](maxSize: Int) extends Cache[K, ByteString] {
  protected val lruCache = new SimpleLRUCache[K, LByteArray](maxSize)

  def getCached(key: K, getValue: () ⇒ ByteString): ByteString = {
    val value = lruCache.getCached(key, { () ⇒
      val bytes = getValue()
      val array = new LByteArray(bytes.length)
      for (i ← bytes.indices) array.putByte(i, bytes(i))
      array
    })

    val array = new Array[Byte](value.length.toInt)
    value.writeToArray(0, array, 0, array.length)
    ByteString.fromArrayUnsafe(array)
  }
}
