package com.karasiq.scalacache

trait Cache[K, V] {
  def getCached(key: K, getValue: () ⇒ V): V
  def clearCache(key: K): Unit
}
