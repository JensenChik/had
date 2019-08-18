/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.cuda.analyzer

import tech.cuda.enums.ColumnType

/**
 * represents predicate expr IN (v1, v2, ...) for the case where the right
 * hand side is a list of integers or dictionary-encoded strings generated
 * by a IN subquery. Avoids the overhead of storing a list of shared pointers
 * to Constant objects, making it more suitable for IN sub-queries usage.
 * v1, v2, ... are integers
 */
class InIntegerSet(val arg: Expression, val valueList: List<Long>)
    : Expression(ColumnType.BOOLEAN) {

    override fun deepCopy(): Expression {
        return InIntegerSet(arg, valueList)
    }

    override fun toString(): String {
        return "(IN_INTEGER_SET $arg( ${valueList.joinToString(" ")} )"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InIntegerSet

        if (arg != other.arg) return false
        if (valueList != other.valueList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arg.hashCode()
        result = 31 * result + valueList.hashCode()
        return result
    }

}