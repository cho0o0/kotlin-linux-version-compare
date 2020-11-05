package com.hitoda.kotlinLinuxVersionCompare

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe

internal class DebianVersionTest : StringSpec({
    "debian versions can be parsed" {
        forAll(
            row(DebianVersion("2:7.4.052-1ubuntu3"), 2, "7.4.052", "1ubuntu3"),
            row(DebianVersion("2:7.4.052-1"), 2, "7.4.052", "1"),
            row(DebianVersion("7.4.052-1"), 0, "7.4.052", "1"),
            row(DebianVersion("1:7.4.052"), 1, "7.4.052", ""),
            row(DebianVersion("7.4.052"), 0, "7.4.052", "")
        ) { version, epoch, upstreamVersion, debianRevision ->
            version.epoch shouldBe epoch
            version.upstreamVersion shouldBe upstreamVersion
            version.debianRevision shouldBe debianRevision
        }
    }

    "equals works" {
        forAll(
            row(DebianVersion("2:7.4.052-1ubuntu3"), DebianVersion("2:7.4.052-1ubuntu3.1"), false),
            row(DebianVersion("2:7.4.052-1ubuntu3"), DebianVersion("2:7.4.052-1ubuntu2"), false),
            row(DebianVersion("0:7.4.052-1ubuntu3"), DebianVersion("7.4.052-1ubuntu3"), true),
            row(DebianVersion("7.4.052"), DebianVersion("0:7.4.052"), true)
        ) { version1, version2, expectedResult ->
            (version1 == version2) shouldBe expectedResult
        }
    }

    "comparison works" {
        forAll(
            // Redhat
            row(DebianVersion("6.4.052"), DebianVersion("7.4.052"), true),
            row(DebianVersion("6.4.052"), DebianVersion("6.5.052"), true),
            row(DebianVersion("6.4.052"), DebianVersion("6.4.053"), true),
            row(DebianVersion("7.4.629-3"), DebianVersion("7.4.629-5"), true),
            row(DebianVersion("7.4.622-1"), DebianVersion("7.4.629-1"), true),
            row(DebianVersion("6.0-4.el6.x86_64"), DebianVersion("6.0-5.el6.x86_64"), true),
            row(DebianVersion("6.0-4.el6.x86_64"), DebianVersion("6.0-3.el7.x86_64"), false),
            row(DebianVersion("7.0-4.el6.x86_64"), DebianVersion("6.1-3.el2.x86_64"), false),
            // Debian
            row(DebianVersion("2:7.4.052-1ubuntu3"), DebianVersion("2:7.4.052-1ubuntu3.1"), true),
            row(DebianVersion("2:7.4.052-1ubuntu2"), DebianVersion("2:7.4.052-1ubuntu3"), true),
            row(DebianVersion("2:7.4.052-1"), DebianVersion("2:7.4.052-1ubuntu3"), true),
            row(DebianVersion("2:7.4.052"), DebianVersion("2:7.4.052-1"), true),
            row(DebianVersion("1:7.4.052"), DebianVersion("2:7.4.052"), true),
            row(DebianVersion("1:7.4.052"), DebianVersion("7.4.052"), false),
            row(DebianVersion("2:7.4.052-1ubuntu3.2"), DebianVersion("2:7.4.052-1ubuntu3.1"), false)
        ) { version1, version2, isNegative ->
            if (isNegative) {
                version1.compareTo(version2) shouldBeLessThan 0
                version1.lessThan(version2) shouldBe true
                version1.greaterThan(version2) shouldBe false
            } else {
                version1.compareTo(version2) shouldBeGreaterThan 0
                version1.lessThan(version2) shouldBe false
                version1.greaterThan(version2) shouldBe true
            }
        }
    }

    "toString should work" {
        forAll(
            row(DebianVersion("6.4.052"), "0:6.4.052"),
            row(DebianVersion("2:6.4.052"), "2:6.4.052"),
            row(DebianVersion("0:7.4.052-1ubuntu2"), "0:7.4.052-1ubuntu2")
        ) { debianVersion, s ->
            debianVersion.toString() shouldBe s
        }
    }
})
