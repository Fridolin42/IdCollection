package de.fridolin1.idCollection

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IDMapSpeedTest {
    @Nested
    inner class FillChangeAndEmptySpeedTest {
        @Test
        fun speedTestFillChangeAndEmpty() {
            var sum1 = 0L
            var sum2 = 0L
            repeat(1.shl(12)) {
                sum1 += measureIDMapFillChangeAndEmpty()
                sum2 += measureHashMapFillChangeAndEmpty()
            }
            println("Speed Test 2: " + sum1 / sum2.toDouble())
            assertTrue { sum1 < sum2 }
        }

        fun measureIDMapFillChangeAndEmpty(): Long {
            val start1 = System.nanoTime()
            val idMap = IDMap<String>(1.shl(15))
            repeat(1.shl(15)) {
                idMap[it] = "Hello World $it"
            }
            for (id in idMap) {
                idMap[id] = "$id changed"
            }
            for (id in idMap.toList()) {
                assertEquals("$id changed", idMap[id])
                idMap.remove(id)
            }
            assertEquals(0, idMap.size)
            return System.nanoTime() - start1
        }

        fun measureHashMapFillChangeAndEmpty(): Long {
            val start1 = System.nanoTime()
            val hashMap = HashMap<Int, String>()
            repeat(1.shl(15)) {
                hashMap[it] = "Hello World $it"
            }
            for (id in hashMap.keys) {
                hashMap[id] = "$id changed"
            }
            for (id in hashMap.keys.toList()) {
                assertEquals("$id changed", hashMap[id])
                hashMap.remove(id)
            }
            assertEquals(0, hashMap.size)
            return System.nanoTime() - start1
        }
    }

    @Nested
    inner class FillAndEmptySpeedTest {
        @Test
        fun speedTestFillAndEmpty() {
            var sum1 = 0L
            var sum2 = 0L
            repeat(1.shl(12)) {
                sum1 += measureIDMapFillAndEmpty()
                sum2 += measureHashMapFillAndEmpty()
            }
            println("Speed Test 1: " + sum1 / sum2.toDouble())
            assertTrue { sum1 < sum2 }
        }

        fun measureIDMapFillAndEmpty(): Long {
            val start1 = System.nanoTime()
            val idMap = IDMap<String>(1.shl(15))
            repeat(1.shl(15)) {
                idMap[it] = "Hello World $it"
            }
            for (id in idMap.toList()) {
                assertEquals("Hello World $id", idMap[id])
                idMap.remove(id)
            }
            assertEquals(0, idMap.size)
            return System.nanoTime() - start1
        }

        fun measureHashMapFillAndEmpty(): Long {
            val start1 = System.nanoTime()
            val hashMap = HashMap<Int, String>()
            repeat(1.shl(15)) {
                hashMap[it] = "Hello World $it"
            }
            for (id in hashMap.keys.toList()) {
                assertEquals("Hello World $id", hashMap[id])
                hashMap.remove(id)
            }
            assertEquals(0, hashMap.size)
            return System.nanoTime() - start1
        }
    }
}