package tech.cuda.shared

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec


class SQLTypeTest : StringSpec({
    "bigint should be number" {
        SQLType.BIGINT.isInteger() shouldBe true
    }
})






