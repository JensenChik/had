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
 * expression for the KEY_FOR_STRING expression.
 * [arg] must be a dict encoded column, not str literal.
 */
class KeyForStringExpr(val arg: Expression)
    : Expression(ColumnType.INT) {


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

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        arg.collectColumnVar(colVarSet, hasAgg)
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        arg.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return KeyForStringExpr(arg.rewriteWithTargetList(targetList))
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return KeyForStringExpr(arg.rewriteWithChildTargetList(targetList))
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return KeyForStringExpr(arg.rewriteAggToVar(targetList))
    }

    override fun deepCopy(): Expression {
        return KeyForStringExpr(arg.deepCopy())
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyForStringExpr

        if (arg != other.arg) return false

        return true
    }

    override fun hashCode(): Int {
        return arg.hashCode()
    }

    override fun toString(): String {
        return "KEY_FOR_STRING($arg)"
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            arg.findExpression(matchFunc, exprList)
        }
    }
}