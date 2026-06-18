package de.fridolin1.idMap

@Suppress("UNCHECKED_CAST")
class IdMap<T>(initialCapacity: Int = 64) : Iterable<Int> {
    private var storage = arrayOfNulls<StorageElement<T>?>(initialCapacity)
    private val elementList = ArrayList<Int>(initialCapacity)
    var size = 0
        private set

    operator fun set(id: Int, value: T) {
        if (id >= storage.size) expandStorage(id)
        val storageElement = storage[id]
        if (storageElement == null) {
            val listIndex = elementList.size
            elementList.add(id)
            storage[id] = StorageElement(value, listIndex)
            size++
        } else
            storage[id] = StorageElement(value, storageElement.listID)
    }

    fun setByListID(id: Int, value: T) = set(elementList[id], value)

    fun remove(id: Int) {
        if (id >= storage.size) return
        storage[id]?.let { oldStorageElement ->
            //swap
            val lastListElement = elementList.last()
            storage[lastListElement]!!.listID = oldStorageElement.listID
            elementList[oldStorageElement.listID] = lastListElement
            //and pop
            storage[id] = null
            elementList.removeLast()
            size--
        }
    }

    fun removeByListID(id: Int) {
        val storageID = elementList[id]
        //swap
        val lastListElement = elementList.last()
        storage[lastListElement]!!.listID = id
        elementList[id] = lastListElement
        //and pop
        storage[storageID] = null
        elementList.removeLast()
        size--
    }

    operator fun get(id: Int): T? = if (id < storage.size) storage[id]?.value else null

    private fun expandStorage(minStorageSize: Int) {
        val oldStorage = storage
        storage = arrayOfNulls<StorageElement<T>?>(minStorageSize + 64)
        oldStorage.copyInto(storage)
    }

    fun containsID(id: Int): Boolean = id < storage.size && storage[id] != null

    fun isEmpty(): Boolean = size == 0

    fun contains(element: T): Boolean = elementList.any { storage[it]?.value == element }

    override fun iterator(): Iterator<Int> = elementList.iterator()
}

private data class StorageElement<T>(var value: T, var listID: Int)