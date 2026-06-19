package de.fridolin1.idMap

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.*

// Generated with Claude Sonnet.
class IdMapFunctionTest {

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    /** Builds a small, non-sequential map: storageIDs 10, 20, 30 → "ten", "twenty", "thirty" */
    private fun sparseMap(): IdMap<String> = IdMap<String>().apply {
        set(10, "ten")
        set(20, "twenty")
        set(30, "thirty")
    }

    // ─────────────────────────────────────────────
    // size
    // ─────────────────────────────────────────────

    @Nested
    inner class SizeTests {
        private lateinit var idMap: IdMap<String>

        @BeforeEach
        fun setUp() {
            idMap = IdMap<String>().apply { repeat(1.shl(15)) { set(it, "$it :)") } }
        }

        @Test
        fun `initial population`() = assertEquals(1.shl(15), idMap.size)

        @Test
        fun `empty map`() = assertEquals(0, IdMap<String>().size)

        @Test
        fun `increases on new insert`() {
            idMap[1_000_000] = "big id"
            assertEquals(1.shl(15) + 1, idMap.size)
        }

        @Test
        fun `unchanged on overwrite`() {
            val before = idMap.size
            idMap[1234] = "overwrite"
            assertEquals(before, idMap.size)
        }

        @Test
        fun `decreases on remove`() {
            val before = idMap.size
            idMap.remove(42)
            assertEquals(before - 1, idMap.size)
        }
    }

    // ─────────────────────────────────────────────
    // set / get
    // ─────────────────────────────────────────────

    @Nested
    inner class SetAndGetTests {

        @Test
        fun `basic insert and retrieve`() {
            val m = IdMap<String>()
            m[7] = "hello"
            assertEquals("hello", m[7])
        }

        @Test
        fun `get returns null for unknown id`() = assertNull(IdMap<String>()[42])

        @Test
        fun `get returns null for id beyond storage`() = assertNull(IdMap<String>()[999])

        @Test
        fun `overwrite updates value`() {
            val m = IdMap<String>()
            m[5] = "first"
            m[5] = "second"
            assertEquals("second", m[5])
        }

        @Test
        fun `overwrite does not affect other entries`() {
            val m = sparseMap()
            m[20] = "TWENTY"
            assertEquals("ten", m[10])
            assertEquals("TWENTY", m[20])
            assertEquals("thirty", m[30])
        }

        @Test
        fun `sparse ids work correctly`() {
            val m = IdMap<Int>()
            m[0] = 0
            m[500] = 500
            m[10_000] = 10_000
            assertEquals(0, m[0])
            assertEquals(500, m[500])
            assertEquals(10_000, m[10_000])
            assertEquals(3, m.size)
        }

        @Test
        fun `storage expands for id larger than initial capacity`() {
            val m = IdMap<String>(4) // very small capacity
            m[1000] = "big"
            assertEquals("big", m[1000])
        }

        @Test
        fun `previously stored entries survive storage expansion`() {
            val m = IdMap<String>(4)
            m[0] = "zero"
            m[1000] = "big"        // triggers expansion
            assertEquals("zero", m[0])
            assertEquals("big", m[1000])
        }
    }

    // ─────────────────────────────────────────────
    // remove (by storage ID)
    // ─────────────────────────────────────────────

    @Nested
    inner class RemoveTests {

        @Test
        fun `element is gone after remove`() {
            val m = sparseMap()
            m.remove(20)
            assertNull(m[20])
            assertFalse(m.containsID(20))
        }

        @Test
        fun `decreases size`() {
            val m = sparseMap()
            m.remove(20)
            assertEquals(2, m.size)
        }

        @Test
        fun `preserves other entries`() {
            val m = sparseMap()
            m.remove(20)
            assertEquals("ten", m[10])
            assertEquals("thirty", m[30])
        }

        @Test
        fun `swapped-in element still accessible`() {
            // When an element is removed the last list-element is moved into its slot.
            // That last element must still be reachable under its original storage ID.
            val m = sparseMap()
            // elementList = [10, 20, 30]; removing 10 moves 30 to list slot 0
            m.remove(10)
            assertEquals("thirty", m[30])
        }

        @Test
        fun `remove last element`() {
            val m = sparseMap()
            m.remove(30)   // 30 is the last in elementList → no swap, just pop
            assertNull(m[30])
            assertEquals(2, m.size)
        }

        @Test
        fun `remove single element empties map`() {
            val m = IdMap<String>()
            m[42] = "only"
            m.remove(42)
            assertTrue(m.isEmpty())
            assertNull(m[42])
        }

        @Test
        fun `remove unknown id does nothing`() {
            val m = sparseMap()
            m.remove(1_000_000)
            assertEquals(3, m.size)
        }

        @Test
        fun `remove id beyond storage does nothing`() {
            val m = IdMap<String>(4)
            m[0] = "zero"
            m.remove(999)          // way beyond current storage
            assertEquals(1, m.size)
        }

        @Test
        fun `remove same id twice`() {
            val m = sparseMap()
            m.remove(20)
            m.remove(20)           // second call should be a no-op
            assertEquals(2, m.size)
        }

        @Test
        fun `remove all elements one by one`() {
            val m = IdMap<Int>()
            repeat(10) { m[it] = it }
            repeat(10) { m.remove(it) }
            assertEquals(0, m.size)
            assertTrue(m.isEmpty())
        }

        @Test
        fun `iterator stays consistent after remove`() {
            val m = sparseMap()
            m.remove(20)
            val ids = m.toList()
            assertEquals(2, ids.size)
            assertFalse(ids.contains(20))
            assertTrue(ids.containsAll(listOf(10, 30)))
        }
    }

    // ─────────────────────────────────────────────
    // containsID
    // ─────────────────────────────────────────────

    @Nested
    inner class ContainsIDTests {

        @Test
        fun `true for existing id`() = assertTrue(sparseMap().containsID(20))

        @Test
        fun `false after remove`() {
            val m = sparseMap()
            m.remove(20)
            assertFalse(m.containsID(20))
        }

        @Test
        fun `false for never-set id`() = assertFalse(sparseMap().containsID(999))

        @Test
        fun `false for id beyond storage`() = assertFalse(IdMap<String>().containsID(999))

        @Test
        fun `true for all inserted ids`() {
            val m = sparseMap()
            assertTrue(m.containsID(10))
            assertTrue(m.containsID(20))
            assertTrue(m.containsID(30))
        }
    }

    // ─────────────────────────────────────────────
    // contains (by value)
    // ─────────────────────────────────────────────

    @Nested
    inner class ContainsTests {

        @Test
        fun `true for existing value`() = assertTrue(sparseMap().contains("twenty"))

        @Test
        fun `false after remove`() {
            val m = sparseMap()
            m.remove(20)
            assertFalse(m.contains("twenty"))
        }

        @Test
        fun `false for value never inserted`() = assertFalse(sparseMap().contains("nope"))

        @Test
        fun `true for updated value`() {
            val m = sparseMap()
            m[20] = "TWENTY"
            assertTrue(m.contains("TWENTY"))
            assertFalse(m.contains("twenty"))  // old value gone
        }
    }

    // ─────────────────────────────────────────────
    // isEmpty
    // ─────────────────────────────────────────────

    @Nested
    inner class IsEmptyTests {

        @Test
        fun `true on fresh map`() = assertTrue(IdMap<String>().isEmpty())

        @Test
        fun `false after insert`() = assertFalse(sparseMap().isEmpty())

        @Test
        fun `true after removing only element`() {
            val m = IdMap<String>()
            m[1] = "a"
            m.remove(1)
            assertTrue(m.isEmpty())
        }

        @Test
        fun `false after partial removes`() {
            val m = sparseMap()
            m.remove(10)
            m.remove(20)
            assertFalse(m.isEmpty())
        }
    }

    // ─────────────────────────────────────────────
    // iterator
    // ─────────────────────────────────────────────

    @Nested
    inner class IteratorTests {

        @Test
        fun `covers all inserted ids`() {
            val ids = sparseMap().toList()
            assertEquals(3, ids.size)
            assertTrue(ids.containsAll(listOf(10, 20, 30)))
        }

        @Test
        fun `empty on fresh map`() = assertEquals(0, IdMap<String>().toList().size)

        @Test
        fun `count matches size`() {
            val m = sparseMap()
            assertEquals(m.size, m.count())
        }

        @Test
        fun `excludes removed ids`() {
            val m = sparseMap()
            m.remove(20)
            val ids = m.toList()
            assertFalse(ids.contains(20))
            assertEquals(2, ids.size)
        }

        @Test
        fun `contains moved id after remove`() {
            // After remove(10), id 30 is swapped into list slot 0; it must still appear
            val m = sparseMap()
            m.remove(10)
            assertTrue(m.toList().contains(30))
        }

        @Test
        fun `all ids resolve to correct values`() {
            val m = sparseMap()
            m.forEach { id ->
                assertNotEquals(m[id], null, "id $id should resolve to a value")
            }
        }
    }
}