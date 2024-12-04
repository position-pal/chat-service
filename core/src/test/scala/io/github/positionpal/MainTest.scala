package io.github.positionpal

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

class MainTest extends AnyFlatSpec with Matchers:

  "A trivial test" should "pass" in:
    true shouldBe true
