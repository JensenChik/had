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
package tech.cuda.shared


enum class SQLOps {
    EQ,
    BW_EQ,
    NE,
    LT,
    GT,
    LE,
    GE,
    AND,
    OR,
    NOT,
    MINUS,
    PLUS,
    MULTIPLY,
    DIVIDE,
    MODULO,
    UMINUS,
    IS_NULL,
    IS_NOT_NULL,
    EXISTS,
    CAST,
    ARRAY_AT,
    UNNEST,
    FUNCTION,
    IN,
    OVERLAPS;

    val isComparison: Boolean
        get() = this in setOf(EQ, BW_EQ, OVERLAPS, NE, LT, GT, LE, GE)
    val isLogic: Boolean get() = this in setOf(AND, OR)
    val isArithmetic: Boolean get() = this in setOf(MINUS, PLUS, MULTIPLY, DIVIDE, MODULO)
    val isUnary: Boolean get() = this in setOf(NOT, UMINUS, IS_NULL, EXISTS, CAST, UNNEST)
    val isEquivalence: Boolean get() = this in setOf(EQ, BW_EQ, OVERLAPS)

    override fun toString(): String {
        return when (this) {
            NOT -> "NOT "
            UMINUS -> "- "
            IS_NULL -> "IS NULL "
            EXISTS -> "EXISTS "
            CAST -> "CAST "
            UNNEST -> "UNNEST "
            else -> ""
        }
    }


}