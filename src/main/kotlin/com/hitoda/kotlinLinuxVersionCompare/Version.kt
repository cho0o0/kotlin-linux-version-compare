package com.hitoda.kotlinLinuxVersionCompare

abstract class Version<T : VersionType> : Comparable<Version<T>> {

    abstract override fun hashCode(): Int
    abstract override fun toString(): String
    abstract override fun compareTo(other: Version<T>): Int
    abstract override fun equals(other: Any?): Boolean

    fun lessThan(another: Version<T>): Boolean {
        return this < another
    }
    fun greaterThan(another: Version<T>): Boolean {
        return this > another
    }
}

interface VersionType
class Debian : VersionType
class Redhat : VersionType
