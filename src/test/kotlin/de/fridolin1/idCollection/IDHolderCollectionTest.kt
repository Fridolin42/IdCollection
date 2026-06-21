/*
 * Suggested location:
 *   - Kotlin/JVM project:           src/test/kotlin/de/fridolin1/idMap/DynamicIdCollectionTest.kt
 *   - Kotlin Multiplatform project: src/commonTest/kotlin/de/fridolin1/idMap/DynamicIdCollectionTest.kt
 *
 * Test framework: kotlin.test (works on JVM via JUnit and on all other KMP targets without changes).
 */
package de.fridolin1.idCollection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Minimal concrete [MultiIDHolder] used as the element type under test.
 *
 * [IDHolderCollection] relies on reference identity for [MultiIDHolder.dynamicIDs],
 * so a plain data holder with a readable [label] is enough to write expressive assertions.
 */
private class TestDynamicElement(name: String) : MultiIDHolder() {
    val label = name
    override fun toString(): String = "TestDynamicElement($label)"
}

/**
 * General, contract-level tests for [IDHolderCollection] as an implementation of [MutableCollection].
 *
 * The tests are written against the behavior a [MutableCollection] is expected to provide
 * (as documented for [MutableCollection] / [Collection]), not against incidental implementation
 * details such as internal array layout or iteration order. Where a test depends on a specific
 * implementation detail of [IDHolderCollection] (e.g. swap-removal via the last element), this is
 * called out in a comment.
 */
@Suppress("DEPRECATION")
class IDHolderCollectionTest {

    private fun newCollection(initialCapacity: Int = 64): IDHolderCollection<TestDynamicElement> =
        IDHolderCollection(initialCapacity)

    private fun elementsNamed(vararg names: String): List<TestDynamicElement> =
        names.map { TestDynamicElement(it) }

    // region construction and empty state

    @Test
    fun `a freshly created collection is empty`() {
        val collection = newCollection()

        assertEquals(0, collection.size)
        assertTrue(collection.isEmpty())
        assertFalse(collection.iterator().hasNext())
    }

    @Test
    fun `each collection instance gets a unique listID`() {
        val first = newCollection()
        val second = newCollection()

        assertNotEquals(first.listID, second.listID)
    }

    // endregion

    // region add

    @Test
    fun `add returns true and increases the size`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")

        val added = collection.add(element)

        assertTrue(added)
        assertEquals(1, collection.size)
        assertFalse(collection.isEmpty())
    }

    @Test
    fun `add makes the element discoverable via contains`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")

        collection.add(element)

        assertTrue(collection.contains(element))
    }

    @Test
    fun `add records the index for this collection's listID on the element`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")

        collection.add(element)

        assertEquals(0, element.dynamicIDs[collection.listID])
    }

    @Test
    fun `adding several elements keeps all of them retrievable`() {
        val collection = newCollection()
        val elements = elementsNamed("a", "b", "c", "d", "e")

        elements.forEach { collection.add(it) }

        assertEquals(elements.size, collection.size)
        elements.forEach { assertTrue(collection.contains(it)) }
    }

    @Test
    fun `add grows the backing storage once the initial capacity is exceeded`() {
        val collection = newCollection(initialCapacity = 2)
        val elements = elementsNamed(*Array(10) { "e$it" })

        elements.forEach { assertTrue(collection.add(it)) }

        assertEquals(elements.size, collection.size)
        elements.forEach { assertTrue(collection.contains(it)) }
    }

    // endregion

    // region contains / containsAll

    @Test
    fun `contains is false for an element that was never added`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        assertFalse(collection.contains(TestDynamicElement("never added")))
    }

    @Test
    fun `contains is false on an empty collection`() {
        val collection = newCollection()

        assertFalse(collection.contains(TestDynamicElement("a")))
    }

    @Test
    fun `containsAll is true only when every requested element is present`() {
        val collection = newCollection()
        val (a, b, c) = elementsNamed("a", "b", "c")
        collection.add(a)
        collection.add(b)

        assertTrue(collection.containsAll(listOf(a, b)))
        assertFalse(collection.containsAll(listOf(a, b, c)))
    }

    @Test
    fun `containsAll is true for an empty argument collection`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        assertTrue(collection.containsAll(emptyList()))
    }

    // endregion

    // region addAll

    @Test
    fun `addAll adds every element of the given collection`() {
        val collection = newCollection()
        val elements = elementsNamed("a", "b", "c")

        val changed = collection.addAll(elements)

        assertTrue(changed)
        assertEquals(elements.size, collection.size)
        elements.forEach { assertTrue(collection.contains(it)) }
    }

    @Test
    fun `addAll with an empty collection does not change the size`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        collection.addAll(emptyList())

        assertEquals(1, collection.size)
    }

    // endregion

    // region remove(element)

    @Test
    fun `remove deletes the element and decreases the size`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")
        collection.add(element)

        val removed = collection.remove(element)

        assertTrue(removed)
        assertEquals(0, collection.size)
        assertFalse(collection.contains(element))
    }

    @Test
    fun `remove clears the dynamicID entry that belongs to this collection`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")
        collection.add(element)

        collection.remove(element)

        assertNull(element.dynamicIDs[collection.listID])
    }

    @Test
    fun `remove of an element that was never added throws`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")

        assertFailsWith<Exception> { collection.remove(element) }
    }

    @Test
    fun `removing an element keeps the other elements accessible`() {
        val collection = newCollection()
        val (a, b, c) = elementsNamed("a", "b", "c")
        collection.add(a)
        collection.add(b)
        collection.add(c)

        collection.remove(b)

        assertEquals(2, collection.size)
        assertTrue(collection.contains(a))
        assertTrue(collection.contains(c))
        assertFalse(collection.contains(b))
    }

    @Test
    fun `an element can be re-added after being removed`() {
        val collection = newCollection()
        val element = TestDynamicElement("a")
        collection.add(element)
        collection.remove(element)

        val added = collection.add(element)

        assertTrue(added)
        assertEquals(1, collection.size)
        assertTrue(collection.contains(element))
    }

    // endregion

    // region remove(index) - DynamicIdCollection specific API

    @Test
    fun `remove by index removes the element stored at that index`() {
        val collection = newCollection()
        val (a, b) = elementsNamed("a", "b")
        collection.add(a)
        collection.add(b)

        collection.remove(0)

        assertEquals(1, collection.size)
        assertFalse(collection.contains(a))
    }

    @Test
    fun `remove by an index beyond the current size throws`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        assertFailsWith<IndexOutOfBoundsException> { collection.remove(5) }
    }

    @Test
    fun `remove by a negative index throws`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        assertFailsWith<IndexOutOfBoundsException> { collection.remove(-1) }
    }

    // endregion

    // region removeAll

    @Test
    fun `removeAll removes every element that is present`() {
        val collection = newCollection()
        val (a, b, c) = elementsNamed("a", "b", "c")
        collection.add(a)
        collection.add(b)
        collection.add(c)

        val changed = collection.removeAll(listOf(a, c))

        assertTrue(changed)
        assertEquals(1, collection.size)
        assertTrue(collection.contains(b))
        assertFalse(collection.contains(a))
        assertFalse(collection.contains(c))
    }

    @Test
    fun `removeAll with an empty collection leaves this collection unchanged`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        collection.removeAll(emptyList())

        assertEquals(1, collection.size)
    }

    // NOTE: java.util.Collection#removeAll (and the Kotlin MutableCollection contract it mirrors)
    // is expected to silently ignore elements that are not part of the collection. The current
    // implementation forwards each element to remove(element), which throws if the element is
    // absent. This test documents that *actual* behavior rather than the documented contract -
    // it is worth revisiting removeAll() if contract-compliant behavior is required.
    @Test
    fun `removeAll currently throws if one of the given elements is not part of the collection`() {
        val collection = newCollection()
        val a = TestDynamicElement("a")
        val notAdded = TestDynamicElement("not added")
        collection.add(a)

        assertFailsWith<Exception> { collection.removeAll(listOf(a, notAdded)) }
    }

    // endregion

    // region retainAll

    @Test
    fun `retainAll keeps only the elements that are part of the given collection`() {
        val collection = newCollection()
        val (a, b, c) = elementsNamed("a", "b", "c")
        collection.add(a)
        collection.add(b)
        collection.add(c)

        val changed = collection.retainAll(listOf(b))

        assertTrue(changed)
        assertEquals(1, collection.size)
        assertTrue(collection.contains(b))
        assertFalse(collection.contains(a))
        assertFalse(collection.contains(c))
    }

    @Test
    fun `retainAll returns false when every element is already retained`() {
        val collection = newCollection()
        val a = TestDynamicElement("a")
        collection.add(a)

        val changed = collection.retainAll(listOf(a))

        assertFalse(changed)
        assertEquals(1, collection.size)
    }

    // endregion

    // region clear

    @Test
    fun `clear empties a collection with a single element`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))

        collection.clear()

        assertEquals(0, collection.size)
        assertTrue(collection.isEmpty())
    }

    // NOTE: this test expresses the documented contract of MutableCollection#clear(): afterwards
    // the collection must be empty, regardless of how many elements it held before. clear() is
    // implemented as `forEach { remove(it) }`, which iterates with a forward index while remove()
    // simultaneously shrinks the backing array via swap-with-last. For three or more elements this
    // can move an unvisited element into an already-visited slot, so the loop may finish before
    // every element has been removed. If this test fails, clear() is the place to look.
    @Test
    fun `clear empties a collection with several elements`() {
        val collection = newCollection()
        val elements = elementsNamed("a", "b", "c", "d", "e")
        elements.forEach { collection.add(it) }

        collection.clear()

        assertEquals(0, collection.size)
        assertTrue(collection.isEmpty())
        elements.forEach { assertFalse(collection.contains(it)) }
    }

    @Test
    fun `clear on an already empty collection is a no-op`() {
        val collection = newCollection()

        collection.clear()

        assertEquals(0, collection.size)
        assertTrue(collection.isEmpty())
    }

    // endregion

    // region iterator

    @Test
    fun `iterator visits every added element exactly once`() {
        val collection = newCollection()
        val elements = elementsNamed("a", "b", "c", "d")
        elements.forEach { collection.add(it) }

        val visited = mutableListOf<TestDynamicElement>()
        val iterator = collection.iterator()
        while (iterator.hasNext()) visited.add(iterator.next())

        assertEquals(elements.toSet(), visited.toSet())
        assertEquals(elements.size, visited.size)
    }

    @Test
    fun `iterator on an empty collection has no elements`() {
        val collection = newCollection()

        assertFalse(collection.iterator().hasNext())
    }

    @Test
    fun `iterator next throws once exhausted`() {
        val collection = newCollection()
        collection.add(TestDynamicElement("a"))
        val iterator = collection.iterator()
        iterator.next()

        assertFalse(iterator.hasNext())
        assertFailsWith<Exception> { iterator.next() }
    }

    @Test
    fun `iterator remove decreases the size by one`() {
        val collection = newCollection()
        elementsNamed("a", "b", "c").forEach { collection.add(it) }
        val iterator = collection.iterator()
        iterator.next()

        iterator.remove()

        assertEquals(2, collection.size)
    }

    // NOTE: per the MutableIterator contract, remove() should remove the element most recently
    // returned by next(). This test pins down that expectation explicitly. If it fails, compare it
    // against `remove(index--)` in DynamicIdCollection's iterator() implementation.
    @Test
    fun `iterator remove deletes the element that was just returned by next`() {
        val collection = newCollection()
        elementsNamed("a", "b", "c").forEach { collection.add(it) }
        val iterator = collection.iterator()
        val justReturned = iterator.next()

        iterator.remove()

        assertFalse(collection.contains(justReturned))
    }

    // endregion

    // region elements shared between multiple collections

    @Test
    fun `the same element can belong to several independent collections at once`() {
        val first = newCollection()
        val second = newCollection()
        val shared = TestDynamicElement("shared")

        first.add(shared)
        second.add(shared)

        assertTrue(first.contains(shared))
        assertTrue(second.contains(shared))
    }

    @Test
    fun `removing an element from one collection does not affect other collections`() {
        val first = newCollection()
        val second = newCollection()
        val shared = TestDynamicElement("shared")
        first.add(shared)
        second.add(shared)

        first.remove(shared)

        assertFalse(first.contains(shared))
        assertTrue(second.contains(shared))
    }

    // endregion
}