package tech.cuda.shared

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec


class SQLTypeTest : StringSpec({
    "bigint should be number" {
        SQLType.BIGINT.isInteger() shouldBe true
    }

    "bigint should be cast to timestamp"{
        SQLType.BIGINT canBeCastTo SQLType.TIMESTAMP shouldBe true
    }

    "bigint to string should be BIGINT"{
        "${SQLType.BIGINT}" shouldBe "BIGINT"
    }
})






