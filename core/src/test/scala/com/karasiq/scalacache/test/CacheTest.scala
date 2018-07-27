package com.karasiq.scalacache.test

import org.scalatest.{FlatSpecLike, Matchers}

import com.karasiq.scalacache.Cache

//noinspection AccessorLikeMethodIsEmptyParen
trait CacheTest extends FlatSpecLike with Matchers {
  def testCache(name: String, cache: Cache[String, String]): Unit = {
    var valueGet = false
    def getValue(): String = {
      if (valueGet) {
        throw new Exception
      } else {
        valueGet = true
        "123"
      }
    }

    name.capitalize + " cache" should "cache value" in {
      cache.getCached("test", getValue) shouldBe "123"
      cache.getCached("test", getValue) shouldBe "123"
      cache.clearCache("test")
      intercept[Exception](cache.getCached("test", getValue))
    }
  }
}
