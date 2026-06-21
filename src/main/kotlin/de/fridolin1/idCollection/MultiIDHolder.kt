package de.fridolin1.idCollection

/** Required to store elements in the IDHolderCollection */
abstract class MultiIDHolder {
    internal val dynamicIDs = IDMap<Int>()
}