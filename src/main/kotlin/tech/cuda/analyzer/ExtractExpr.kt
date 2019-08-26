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

import tech.cuda.enums.ExtractField
import tech.cuda.shared.ColumnInfo
import java.util.*

class ExtractExpr(
        columnInfo: ColumnInfo,
        hasAgg: Boolean,
        val field: ExtractField,
        val fromExpr: Expression
) : Expression(columnInfo, hasAgg) {
    override fun checkGroupBy(groupBy: List<Expression>) {
        fromExpr.checkGroupBy(groupBy)
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            fromExpr.findExpression(matchFunc, exprList)
        }
    }

    override fun groupPredicates(scanPredicates: MutableList<Expression>, joinPredicates: MutableList<Expression>, constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        fromExpr.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        fromExpr.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        fromExpr.collectColumnVar(colVarSet, hasAgg)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return ExtractExpr(columnInfo, hasAgg, field,
                fromExpr.rewriteWithTargetList(targetList)
        )
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return ExtractExpr(columnInfo, hasAgg, field,
                fromExpr.rewriteWithChildTargetList(targetList)
        )
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return ExtractExpr(columnInfo, hasAgg, field,
                fromExpr.rewriteAggToVar(targetList)
        )
    }

    override fun deepCopy(): Expression {
        return ExtractExpr(columnInfo, hasAgg, field, fromExpr.deepCopy())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtractExpr

        if (field != other.field) return false
        if (fromExpr != other.fromExpr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + fromExpr.hashCode()
        return result
    }

    override fun toString(): String {
        return "EXTRACT($field FROM $fromExpr)"
    }


}

