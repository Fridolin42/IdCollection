package de.fridolin1.idMap

class IdMap<T>(initialCapacity: Int = 64) : Iterable<Int> {
    /** The actual stored elements */
    @Suppress("UNCHECKED_CAST")
    private var storage = arrayOfNulls<Any>(initialCapacity) as Array<T?>

    /** The reference for the elements-list */
    private var listReferences = IntArray(initialCapacity) { -1 }

    /** A list in form of an array, that list in a range of 0 until size the id's of the stored elements */
    private var elementList = IntArray(initialCapacity) { -1 }

    /** The amount of stored elements */
    var size = 0
        private set

    /** Save under the id the value */
    operator fun set(id: Int, value: T) {
        if (id >= storage.size) expandStorage(id)
        if (storage[id] == null) {
            elementList[size] = id
            storage[id] = value
            listReferences[id] = size
            size++
        } else
            storage[id] = value
    }

    fun remove(id: Int) {
        if (id >= storage.size) return
        if (storage[id] == null) return
        //swap
        val lastListElementID = elementList[size - 1]
        listReferences[lastListElementID] = listReferences[id]
        elementList[listReferences[id]] = lastListElementID
        //and pop
        storage[id] = null
        elementList[size - 1] = -1
        size--
    }

    operator fun get(id: Int): T? = if (id < storage.size) storage[id] else null

    private fun expandStorage(minStorageSize: Int) {
        var newSize = storage.size
        while (newSize in 1..minStorageSize) newSize = newSize.shl(1)
        if (newSize < 0) newSize = Int.MAX_VALUE

        storage = storage.copyOf(newSize)
        listReferences = listReferences.copyOf(newSize)
        elementList = elementList.copyOf(newSize)
    }

    fun containsID(id: Int): Boolean = id < storage.size && storage[id] != null

    fun isEmpty(): Boolean = size == 0

    fun contains(element: T): Boolean {
        for (i in 0 until size)
            if (storage[elementList[i]] == element) return true
        return false
    }

    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun next(): Int = elementList[index++]
    }
}