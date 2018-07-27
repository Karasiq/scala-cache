package com.karasiq.scalacache.larray.test

import scala.concurrent.{ExecutionContext, Future}

import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import com.karasiq.scalacache.larray.{LArrayAsyncLRUCache, LArrayAsyncTapeCache, LArrayLRUCache, LArrayTapeCache}

class LArrayCacheTest extends FlatSpec with Matchers with ScalaFutures {
  "LArray async tape cache" should "cache bytes" in {
    val value = ByteString("123")
    val cache = LArrayAsyncTapeCache[String]()
    cache.getCached("test", () ⇒ Future.successful(value)).futureValue shouldBe value
    cache.getCached("test", () ⇒ throw new Exception).futureValue shouldBe value
    cache.clearCache("test")
    intercept[Exception](cache.getCached("test", () ⇒ throw new Exception).futureValue)
  }

  "LArray async LRU cache" should "cache bytes" in {
    val value = ByteString("123")
    val cache = LArrayAsyncLRUCache[String]()(ExecutionContext.global)
    cache.getCached("test", () ⇒ Future.successful(value)).futureValue shouldBe value
    cache.getCached("test", () ⇒ throw new Exception).futureValue shouldBe value
    cache.clearCache("test")
    intercept[Exception](cache.getCached("test", () ⇒ throw new Exception).futureValue)
  }

  "LArray tape cache" should "cache bytes" in {
    val value = ByteString("123")
    val cache = LArrayTapeCache[String]()
    cache.getCached("test", () ⇒ value) shouldBe value
    cache.getCached("test", () ⇒ throw new Exception) shouldBe value
    cache.clearCache("test")
    intercept[Exception](cache.getCached("test", () ⇒ throw new Exception))
  }

  "LArray LRU cache" should "cache bytes" in {
    val value = ByteString("123")
    val cache = LArrayLRUCache[String]()
    cache.getCached("test", () ⇒ value) shouldBe value
    cache.getCached("test", () ⇒ throw new Exception) shouldBe value
    cache.clearCache("test")
    intercept[Exception](cache.getCached("test", () ⇒ throw new Exception))
  }
}
