package com.karasiq.scalacache.larray

import scala.concurrent.{ExecutionContext, Future}

import akka.util.ByteString
import xerial.larray.LByteArray

import com.karasiq.scalacache.{Cache, SimpleLRUCache}

object LArrayAsyncLRUCache {
  def apply[K](maxSize: Int = 500)(implicit ec: ExecutionContext): LArrayAsyncLRUCache[K] = {
    new LArrayAsyncLRUCache(maxSize)
  }
}

class LArrayAsyncLRUCache[K](maxSize: Int)(implicit ec: ExecutionContext) extends Cache[K, Future[ByteString]] {
  protected val lruCache = new SimpleLRUCache[K, Future[LByteArray]](maxSize) {
    override protected def clearOldEntries(count: Int): Seq[(K, Entry)] = {
      val deleted = super.clearOldEntries(count)
      deleted.foreach(_._2.value.foreach(_.free))
      deleted 
    }
  }

  def getCached(key: K, getValue: () ⇒ Future[ByteString]): Future[ByteString] = {
    val arrayFuture = lruCache.getCached(key, { () ⇒
      val arrayFuture = getValue().map { bs ⇒
        val array = new LByteArray(bs.length)
        for (i ← bs.indices) array.putByte(i, bs(i))
        array
      }
      arrayFuture.onComplete(_.failed.foreach(_ ⇒ lruCache.clearCache(key)))
      arrayFuture
    })

    arrayFuture.map { array ⇒
      val byteArray = new Array[Byte](array.length.toInt)
      array.writeToArray(0, byteArray, 0, byteArray.length)
      ByteString.fromArrayUnsafe(byteArray)
    }
  }

  def clearCache(key: K): Unit = {
    lruCache.clearCache(key)
  }
}
