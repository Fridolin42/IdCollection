package de.fridolin1.idMap

@Suppress("UNCHECKED_CAST")
class IdMap<T>(initialCapacity: Int = 64) : Iterable<Int> {
    private var storage = arrayOfNulls<StorageElement<T>?>(initialCapacity)
    private var elements = IntArray(initialCapacity) { -1 }

    //    private val elementList = ArrayList<Int>(initialCapacity)
    var size = 0
        private set

    operator fun set(id: Int, value: T) {
        if (id >= storage.size) expandStorage(id)
        if (storage[id] == null) {
            elements[size] = id
            storage[id] = StorageElement(value, size)
            size++
        } else
            storage[id]!!.value = value// = StorageElement(value, storageElement.listID)
    }

    fun setByListID(listID: Int, value: T) = set(elements[listID], value)

    fun remove(id: Int) {
        if (id >= storage.size) return
        storage[id]?.let { oldStorageElement ->
            //swap
            val lastListElement = elements[size - 1]
            storage[lastListElement]!!.listID = oldStorageElement.listID
            elements[oldStorageElement.listID] = lastListElement
            //and pop
            storage[id] = null
            elements[size - 1] = -1
            size--
        }
    }

    fun removeByListID(id: Int) {
        val storageID = elements[id]
        if (storageID == -1) return
        //swap
        val lastListElement = elements[size - 1]
        storage[lastListElement]!!.listID = id
        elements[id] = lastListElement
        //and pop
        storage[storageID] = null
        elements[size - 1] = -1
        size--
    }

    operator fun get(id: Int): T? = if (id < storage.size) storage[id]?.value else null

    private fun expandStorage(minStorageSize: Int) {
        var newSize = storage.size
        while (newSize in 1..minStorageSize) newSize = newSize.shl(1)
        if (newSize < 0) newSize = Int.MAX_VALUE

        storage = storage.copyOf(newSize)
        elements = elements.copyOf(newSize)
//        val oldStorage = storage
//        storage = arrayOfNulls<StorageElement<T>?>(newSize)
//        oldStorage.copyInto(storage)
    }

    fun containsID(id: Int): Boolean = id < storage.size && storage[id] != null

    fun isEmpty(): Boolean = size == 0

    fun contains(element: T): Boolean {
        for (i in 0 until size)
            if (storage[elements[i]]!!.value == element) return true
        return false
    }

    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun next(): Int = elements[index++]
    }
}

private data class StorageElement<T>(var value: T, var listID: Int)