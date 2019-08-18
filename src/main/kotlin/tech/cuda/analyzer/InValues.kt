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
import java.util.*

/**
 * represents predicate expr IN (v1, v2, ...)
 * v1, v2, ... are can be either Constant or Parameter.
 */
class InValues(val arg: Expression, val valueList: List<Expression>)
    : Expression(ColumnType.BOOLEAN) {

    override fun deepCopy(): Expression {
        return InValues(arg.deepCopy(), valueList.map { it.deepCopy() })
    }

    override fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        arg.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return InValues(arg.rewriteWithTargetList(targetList), valueList.map { it.deepCopy() })
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return InValues(arg.rewriteWithChildTargetList(targetList), valueList.map { it.deepCopy() })
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return InValues(arg.rewriteAggToVar(targetList), valueList.map { it.deepCopy() })
    }

    override fun toString(): String {
        return "(IN $arg(${valueList.joinToString(" ")})"
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            arg.findExpression(matchFunc, exprList)
            valueList.forEach { it.findExpression(matchFunc, exprList) }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InValues

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