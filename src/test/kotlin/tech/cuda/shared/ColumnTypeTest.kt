package tech.cuda.shared

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import tech.cuda.enums.DataType
import tech.cuda.enums.canBeCastTo


class DataTypeTest : StringSpec({
    "bigint should be number" {
        DataType.BIGINT.isInteger shouldBe true
    }

    "bigint should be cast to timestamp"{
        DataType.BIGINT canBeCastTo DataType.TIMESTAMP shouldBe true
    }

    "bigint to string should be BIGINT"{
        "${DataType.BIGINT}" shouldBe "BIGINT"
    }
})






