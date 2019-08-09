package tech.cuda.exception

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.StringSpec

class OutOfHostMemoryTest : StringSpec({
    "should throw OOM"{
        val exception = shouldThrow<OutOfHostMemoryException> {
            throw OutOfHostMemoryException(100)
        }
        exception.message shouldBe "Failed to allocate 100 bytes of memory"
    }
})