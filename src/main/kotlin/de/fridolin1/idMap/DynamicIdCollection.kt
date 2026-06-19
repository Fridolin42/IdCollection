package de.fridolin1.idMap

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
@Suppress("UNCHECKED_CAST")
class DynamicIdCollection<T : DynamicID>(initialCapacity: Int = 64) : MutableCollection<T> {
    private var elements = arrayOfNulls<Any>(initialCapacity)

    override var size = 0
        private set

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: T): Boolean {
        for (i in indices) if (elements[i] == element) return true
        return false
    }

    override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
        var index = 0
        override fun hasNext(): Boolean = index < size
        override fun next(): T = elements[index++] as T
        override fun remove() = remove(--index)
    }

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    companion object {
        private val nextID = AtomicInt(0)
    }

    val listID = nextID.fetchAndIncrement()

    override fun add(element: T): Boolean {
        if (size == elements.size) expandStorage()
        element.dynamicIDs[listID] = size
        elements[size] = element
        size++
        return true
    }

    override fun remove(element: T): Boolean {
        val index = element.dynamicIDs[listID] ?: throw Exception("Element not found")
        val last = elements[size - 1]!!
        (last as T).dynamicIDs[listID] = index
        elements[index] = last
        elements[size - 1] = null
        element.dynamicIDs.remove(listID)
        size--
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean = elements.all { add(it) }

    override fun removeAll(elements: Collection<T>): Boolean = elements.all { remove(it) }

    override fun retainAll(elements: Collection<T>): Boolean {
        var modified = false
        val iterator = iterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (!elements.contains(current)) {
                iterator.remove()
                modified = true
            }
        }
        return modified
    }

    override fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }

    fun remove(index: Int) {
        if (index !in indices) throw IndexOutOfBoundsException("Index: $index, Size: $size")
        remove(elements[index]!!)
    }

    private fun expandStorage() {
        var newSize = elements.size.shl(1)
        if (newSize < 0) newSize = Int.MAX_VALUE

        elements = elements.copyOf(newSize)
    }

    override fun toString(): String {
        val content = elements.copyOfRange(0, size)
        return "DynamicIdCollection(elements=${content.contentToString()}, size=$size, listID=$listID)"
    }
}