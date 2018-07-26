package com.karasiq.scalacache.test

import com.karasiq.scalacache.{SimpleLRUCache, SimpleTapeCache}

class SimpleCacheTest extends CacheTest {
  testCache("Simple LRU cache", new SimpleLRUCache[String, String](123))
  testCache("Simple tape cache", new SimpleTapeCache[String, String](123))
}
