package com.hitoda.kotlinLinuxVersionCompare

open class DebianVersion(verString: String) : Version<Debian>() {
    val epoch: Int
    val upstreamVersion: String
    val debianRevision: String
    init {
        var ver = verString.trim()
        val spliced = ver.split(delimiters = *arrayOf(":"), limit = 2)
        if (spliced.size == 1) {
            this.epoch = 0
            ver = spliced[0]
        } else {
            this.epoch = spliced[0].toInt()
            if (epoch < 0) {
                throw RuntimeException("epoch should not be less than 0")
            }
            ver = spliced[1]
        }
        val index = ver.lastIndexOf("-")
        if (index >= 0) {
            this.upstreamVersion = ver.take(index)
            this.debianRevision = ver.drop(index + 1)
        } else {
            this.upstreamVersion = ver
            this.debianRevision = ""
        }

        verifyUpstreamVersion(this.upstreamVersion)
        verifyDebianRevision(this.debianRevision)
    }

    private fun verifyUpstreamVersion(upstreamVersion: String) {

        if (upstreamVersion.isEmpty()) throw RuntimeException("upstream version should not be empty")

        if (!upstreamVersion.first().isDigit()) {
            throw RuntimeException("upstream version should start with digit")
        }

        if (!upstreamVersion.all { it.isLetterOrDigit() || listOf('.', '-', '+', '~', ':', '_').contains(it) }) {
            throw RuntimeException("upstream version has disallowed characters")
        }
    }

    private fun verifyDebianRevision(debianRevision: String) {
        if (!debianRevision.all { it.isLetterOrDigit() || listOf('.', '-', '+', '~', ':', '_').contains(it) }) {
            throw RuntimeException("debian revision has disallowed characters")
        }
    }

    override fun compareTo(other: Version<Debian>): Int {
        if (other !is DebianVersion) {
            throw RuntimeException("not comparable: $this, $other")
        }

        if (this.epoch > other.epoch) {
            return 1
        } else if (this.epoch < other.epoch) {
            return -1
        }
        val ret = compare(this.upstreamVersion, other.upstreamVersion)

        if (ret != 0) {
            return ret
        }

        return compare(this.debianRevision, other.debianRevision)
    }

    private fun compare(a: String, b: String): Int {
        val numberRegex = Regex("""[0-9]+""")
        val nonNumberRegex = Regex("""[^0-9]+""")

        if (a == b) {
            return 0
        }

        val numbersInA = numberRegex.findAll(a).map { it.value.toInt() }.toList()
        val numbersInB = numberRegex.findAll(b).map { it.value.toInt() }.toList()
        var stringsInA = nonNumberRegex.findAll(a).map { it.value }.toList()
        var stringsInB = nonNumberRegex.findAll(b).map { it.value }.toList()

        if (a.isNotEmpty() && a.first().isDigit()) {
            stringsInA = listOf("") + stringsInA
            stringsInB = listOf("") + stringsInB
        }

        generateSequence(0) { it + 1 }.forEach {
            var diff = compareString(
                stringsInA.getOrElse(it) { _ -> "" },
                stringsInB.getOrElse(it) { _ -> "" }
            )
            if (diff != 0) return@compare diff

            diff = numbersInA.getOrElse(it) { _ -> 0 } - numbersInB.getOrElse(it) { _ -> 0 }
            if (diff != 0) return@compare diff
        }

        throw RuntimeException("comparison cannot be completed correctly")
    }

    private fun compareString(s1: String, s2: String): Int {
        if (s1 == s2) return 0

        generateSequence(0) { it + 1 }.forEach {
            val a = if (it < s1.length) order(s1[it]) else 0
            val b = if (it < s2.length) order(s2[it]) else 0

            if (a != b) {
                return@compareString a - b
            }
        }
        throw RuntimeException("comparison cannot be completed correctly")
    }

    private fun order(char: Char): Int {
        if (char.isLetter()) return char.toInt()

        if (char == '~') return -1

        return char.toInt() + 256
    }

    override fun hashCode(): Int {
        var result = epoch
        result = 31 * result + upstreamVersion.hashCode()
        result = 31 * result + debianRevision.hashCode()
        return result
    }

    override fun toString(): String {
        return "$epoch:$upstreamVersion" + if (debianRevision.isBlank()) "" else "-$debianRevision"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DebianVersion) {
            this.compareTo(other) == 0
        } else {
            false
        }
    }
}
