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

import tech.cuda.enums.AggregateType
import tech.cuda.enums.ColumnType
import tech.cuda.enums.WhichRow
import tech.cuda.exception.NotFoundException
import tech.cuda.shared.ColumnInfo
import java.util.*

/**
 * expression for builtin SQL aggregates.
 */
class AggExpr(columnInfo: ColumnInfo,
              val aggType: AggregateType,
              val arg: Expression?,
              val isDistinct: Boolean,
              val errorRate: Constant) : Expression(columnInfo) {

    constructor(columnType: ColumnType, aggType: AggregateType, arg: Expression?,
                isDistinct: Boolean, errorRate: Constant
    ) : this(ColumnInfo(columnType), aggType, arg, isDistinct, errorRate)

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        arg?.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        if (hasAgg) {
            arg?.collectColumnVar(colVarSet, hasAgg)
        }
    }

    override fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        arg?.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEach {
            val expr = it.expression
            if (expr is AggExpr && this == expr) {
                return expr.deepCopy()
            }
        }
        throw NotFoundException("AggExpr", "targetList")
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return AggExpr(columnInfo, aggType, arg?.rewriteWithChildTargetList(targetList),
                isDistinct, errorRate)
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEachIndexed { varNum, targetEntry ->
            val expr = targetEntry.expression
            if (expr is AggExpr && this == expr) {
                return Var(expr.columnInfo, whichRow = WhichRow.INPUT_OUTER, varNum = varNum)
            }
        }
        throw NotFoundException("AggExpr", "targetList")

    }

    override fun deepCopy(): Expression {
        return AggExpr(columnInfo, aggType, arg?.deepCopy(), isDistinct, errorRate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AggExpr

        if (aggType != other.aggType) return false
        if (arg != other.arg) return false
        if (isDistinct != other.isDistinct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aggType.hashCode()
        result = 31 * result + (arg?.hashCode() ?: 0)
        result = 31 * result + isDistinct.hashCode()
        result = 31 * result + errorRate.hashCode()
        return result
    }

    override fun toString(): String {
        return "($aggType ${if (isDistinct) "DISTINCT" else ""} ${arg?.toString() ?: "*"})"
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            arg?.findExpression(matchFunc, exprList)
        }
    }

}