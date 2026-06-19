package de.fridolin1.idMap

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdMapSpeedTest {
    @Test
    fun speedTest() {
        var sum1 = 0L
        var sum2 = 0L
        repeat(1.shl(12)) {
            sum1 += measureIDMap()
            sum2 += measureHashMap()
        }
        println(sum1 / sum2.toDouble())
        assertTrue { sum1 < sum2 }
    }

    fun measureIDMap(): Long {
        val start1 = System.nanoTime()
        val idMap = IdMap<String>(1.shl(15))
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

    fun measureHashMap(): Long {
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