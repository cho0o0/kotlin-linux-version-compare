package com.hitoda.kotlinLinuxVersionCompare

open class RedhatVersion(verString: String) : Version<Redhat>() {
    val epoch: Int
    val version: String
    val release: String

    init {
        var ver = verString.trim()
        val spliced = ver.split(delimiters = *arrayOf(":"), limit = 2)
        if (spliced.size == 1) {
            this.epoch = 0
            ver = spliced[0]
        } else {
            this.epoch = spliced[0].toIntOrNull() ?: 0
            ver = spliced[1]
        }
        val index = ver.indexOfFirst { it == '-' }
        if (index >= 0) {
            this.version = ver.take(index)
            this.release = ver.drop(index + 1)
        } else {
            this.version = ver
            this.release = ""
        }
    }

    override fun compareTo(other: Version<Redhat>): Int {
        if (other !is RedhatVersion) {
            throw RuntimeException("not comparable: $this, $other")
        }

        if (this.epoch > other.epoch) return 1
        if (this.epoch < other.epoch) return -1

        val result = rpmVersionCompare(this.version, other.version)
        return if (result != 0) {
            result
        } else {
            rpmVersionCompare(this.release, other.release)
        }
    }

    private fun rpmVersionCompare(a: String, b: String): Int {
        val alphanumRegex = Regex("""([a-zA-Z]+)|([0-9]+)|(~)""")
        val segmentsA = alphanumRegex.findAll(a).map { it.value }.toList()
        val segmentsB = alphanumRegex.findAll(b).map { it.value }.toList()
        val comparedResult = generateSequence(0) { it + 1 }
            .takeWhile { segmentsA.size > it && segmentsB.size > it }
            .map {
                compareEachSegment(segmentsA[it], segmentsB[it])
            }
            .firstOrNull { it != 0 }
        if (comparedResult != null) {
            return comparedResult
        }

        if (segmentsA.size == segmentsB.size) return 0

        if (segmentsA.size > segmentsB.size && segmentsA[segmentsB.size][0] == '~') {
            return -1
        } else if (segmentsB.size > segmentsA.size && segmentsB[segmentsA.size][0] == '~') {
            return 1
        }

        return if (segmentsA.size > segmentsB.size) 1 else -1
    }

    private fun compareEachSegment(left: String, right: String): Int {
        var a = left
        var b = right

        val tilde = '~'
        if (a[0] == tilde || b[0] == tilde) {
            if (a[0] != tilde) return 1
            if (b[0] != tilde) return -1
        }

        if (a[0].isDigit()) {
            if (!b[0].isDigit()) return 1
            a = a.trimStart('0')
            b = b.trimStart('0')

            if (a.length > b.length) {
                return 1
            } else if (a.length < b.length) {
                return -1
            }
        } else if (b[0].isDigit()) {
            return -1
        }

        if (a < b) {
            return -1
        } else if (a > b) {
            return 1
        }
        return 0
    }

    override fun hashCode(): Int {
        var result = epoch
        result = 31 * result + version.hashCode()
        result = 31 * result + release.hashCode()
        return result
    }

    override fun toString(): String {
        return "$epoch:$version" + if (release.isBlank()) "" else "-$release"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is RedhatVersion) {
            this.compareTo(other) == 0
        } else {
            false
        }
    }
}
