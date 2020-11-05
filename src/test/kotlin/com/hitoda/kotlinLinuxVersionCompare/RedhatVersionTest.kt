package com.hitoda.kotlinLinuxVersionCompare

import com.hitoda.kotlinLinuxVersionCompare.RedhatVersionTest.Relationship.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe

internal class RedhatVersionTest : StringSpec({
    "redhat versions can be parsed" {
        forAll(
            // Test 0
            row(RedhatVersion("0"), 0, "0", ""),
            row(RedhatVersion("0:0"), 0, "0", ""),
            row(RedhatVersion("0:0-"), 0, "0", ""),
            row(RedhatVersion("0:0-0"), 0, "0", "0"),
            row(RedhatVersion("0:0.0-0.0"), 0, "0.0", "0.0"),
            // Test epoch
            row(RedhatVersion("1:0"), 1, "0", ""),
            row(RedhatVersion("5:1"), 5, "1", ""),
            // Test multiple hyphens
            row(RedhatVersion("0:0-0-0"), 0, "0", "0-0"),
            row(RedhatVersion("0:0-0-0-0"), 0, "0", "0-0-0"),
            // Test multiple colons
            row(RedhatVersion("0:0:0-0"), 0, "0:0", "0"),
            row(RedhatVersion("0:0:0:0-0"), 0, "0:0:0", "0"),
            // Test multiple hyphens and colons
            row(RedhatVersion("0:0:0-0-0"), 0, "0:0", "0-0"),
            row(RedhatVersion("0:0-0:0-0"), 0, "0", "0:0-0"),
            // Test version with leading and trailing spaces
            row(RedhatVersion("  \t0:0-1"), 0, "0", "1"),
            // Test empty version
            row(RedhatVersion(""), 0, "", ""),
            // Test version not starting with a digit
            row(RedhatVersion("0:abc3-0"), 0, "abc3", "0"),
            // Test actual version
            row(RedhatVersion("1.2.3"), 0, "1.2.3", ""),
            row(RedhatVersion("1:1.2.3"), 1, "1.2.3", ""),
            row(RedhatVersion("A:1.2.3"), 0, "1.2.3", ""),
            row(RedhatVersion("-1:1.2.3"), -1, "1.2.3", ""),
            row(RedhatVersion("6.0-4.el6.x86_64"), 0, "6.0", "4.el6.x86_64"),
            row(RedhatVersion("c105b9de-4e0fd3a3"), 0, "c105b9de", "4e0fd3a3"),
            row(RedhatVersion("4.999.9-0.5.beta.20091007git.el6"), 0, "4.999.9", "0.5.beta.20091007git.el6")
        ) { redhatVersion, epoch, version, release ->
            redhatVersion.epoch shouldBe epoch
            redhatVersion.version shouldBe version
            redhatVersion.release shouldBe release
        }
    }

    "redhat versions can be compared" {
        forAll(
            // Oracle Linux corner cases.
            row("2.9.1-6.0.1.el7_2.3", GREATER, "2.9.1-6.el7_2.3"),
            row("3.10.0-327.28.3.el7", GREATER, "3.10.0-327.el7"),
            row("3.14.3-23.3.el6_8", GREATER, "3.14.3-23.el6_7"),
            row("2.23.2-22.el7_1", LESS, "2.23.2-22.el7_1.1"),
            // Tests imported from tests/rpmvercmp.at
            row("1.0", EQUAL, "1.0"),
            row("1.0", LESS, "2.0"),
            row("2.0", GREATER, "1.0"),
            row("2.0.1", EQUAL, "2.0.1"),
            row("2.0", LESS, "2.0.1"),
            row("2.0.1", GREATER, "2.0"),
            row("2.0.1a", EQUAL, "2.0.1a"),
            row("2.0.1a", GREATER, "2.0.1"),
            row("2.0.1", LESS, "2.0.1a"),
            row("5.5p1", EQUAL, "5.5p1"),
            row("5.5p1", LESS, "5.5p2"),
            row("5.5p2", GREATER, "5.5p1"),
            row("5.5p10", EQUAL, "5.5p10"),
            row("5.5p1", LESS, "5.5p10"),
            row("5.5p10", GREATER, "5.5p1"),
            row("10xyz", LESS, "10.1xyz"),
            row("10.1xyz", GREATER, "10xyz"),
            row("xyz10", EQUAL, "xyz10"),
            row("xyz10", LESS, "xyz10.1"),
            row("xyz10.1", GREATER, "xyz10"),
            row("xyz.4", EQUAL, "xyz.4"),
            row("xyz.4", LESS, "8"),
            row("8", GREATER, "xyz.4"),
            row("xyz.4", LESS, "2"),
            row("2", GREATER, "xyz.4"),
            row("5.5p2", LESS, "5.6p1"),
            row("5.6p1", GREATER, "5.5p2"),
            row("5.6p1", LESS, "6.5p1"),
            row("6.5p1", GREATER, "5.6p1"),
            row("6.0.rc1", GREATER, "6.0"),
            row("6.0", LESS, "6.0.rc1"),
            row("10b2", GREATER, "10a1"),
            row("10a2", LESS, "10b2"),
            row("1.0aa", EQUAL, "1.0aa"),
            row("1.0a", LESS, "1.0aa"),
            row("1.0aa", GREATER, "1.0a"),
            row("10.0001", EQUAL, "10.0001"),
            row("10.0001", EQUAL, "10.1"),
            row("10.1", EQUAL, "10.0001"),
            row("10.0001", LESS, "10.0039"),
            row("10.0039", GREATER, "10.0001"),
            row("4.999.9", LESS, "5.0"),
            row("5.0", GREATER, "4.999.9"),
            row("20101121", EQUAL, "20101121"),
            row("20101121", LESS, "20101122"),
            row("20101122", GREATER, "20101121"),
            row("2_0", EQUAL, "2_0"),
            row("2.0", EQUAL, "2_0"),
            row("2_0", EQUAL, "2.0"),
            row("a", EQUAL, "a"),
            row("a+", EQUAL, "a+"),
            row("a+", EQUAL, "a_"),
            row("a_", EQUAL, "a+"),
            row("+a", EQUAL, "+a"),
            row("+a", EQUAL, "_a"),
            row("_a", EQUAL, "+a"),
            row("+_", EQUAL, "+_"),
            row("_+", EQUAL, "+_"),
            row("_+", EQUAL, "_+"),
            row("+", EQUAL, "_"),
            row("_", EQUAL, "+"),
            row("1.0~rc1", EQUAL, "1.0~rc1"),
            row("1.0~rc1", LESS, "1.0"),
            row("1.0", GREATER, "1.0~rc1"),
            row("1.0~rc1", LESS, "1.0~rc2"),
            row("1.0~rc2", GREATER, "1.0~rc1"),
            row("1.0~rc1~git123", EQUAL, "1.0~rc1~git123"),
            row("1.0~rc1~git123", LESS, "1.0~rc1"),
            row("1.0~rc1", GREATER, "1.0~rc1~git123"),
            // Test epoch
            row("1:1.0~rc1", GREATER, "0:1.0~rc1"),
            row("1.0~rc1", LESS, "2:1.0~rc1"),
            row("3:1.0~rc1", EQUAL, "3:1.0~rc1"),
            // Test cases from https://fedoraproject.org/wiki/Archive:Tools/RPM/VersionComparison
            row("1.0010", GREATER, "1.9"),
            row("1.05", EQUAL, "1.5"),
            row("1.0", GREATER, "1"),
            row("2.50", GREATER, "2.5"),
            row("fc4", EQUAL, "fc.4"),
            row("FC5", LESS, "fc4"),
            row("2a", LESS, "2.0"),
            row("1.0", GREATER, "1.fc4"),
            row("3.0.0_fc", EQUAL, "3.0.0.fc")

        ) { version1, relationship, version2 ->
            val v1 = RedhatVersion(version1)
            val v2 = RedhatVersion(version2)
            when (relationship) {
                GREATER -> {
                    v1.compareTo(v2) shouldBeGreaterThan 0
                    v1.greaterThan(v2) shouldBe true
                    v1.lessThan(v2) shouldBe false
                    (v1 == v2) shouldBe false
                }
                LESS -> {
                    v1.compareTo(v2) shouldBeLessThan 0
                    v1.greaterThan(v2) shouldBe false
                    v1.lessThan(v2) shouldBe true
                    (v1 == v2) shouldBe false
                }
                EQUAL -> {
                    v1.compareTo(v2) shouldBe 0
                    v1.greaterThan(v2) shouldBe false
                    v1.lessThan(v2) shouldBe false
                    (v1 == v2) shouldBe true
                }
            } 
        }
    }
}) {
    private enum class Relationship {
        GREATER,
        LESS,
        EQUAL
    }
}
