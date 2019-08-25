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

import tech.cuda.enums.DateTruncField
import tech.cuda.shared.ColumnInfo
import java.util.*

class DatediffExpr(
        columnInfo: ColumnInfo,
        val field: DateTruncField,
        val start: Expression,
        val end: Expression
) : Expression(columnInfo) {
    override fun checkGroupBy(groupBy: List<Expression>) {
        start.checkGroupBy(groupBy)
        end.checkGroupBy(groupBy)
    }

    override fun groupPredicates(scanPredicates: MutableList<Expression>, joinPredicates: MutableList<Expression>, constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        start.collectRangeTableEntriesIndexTo(rteIdxSet)
        end.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        start.collectRangeTableEntriesIndexTo(rteSet)
        end.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        start.collectColumnVar(colVarSet, hasAgg)
        end.collectColumnVar(colVarSet, hasAgg)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return DatediffExpr(columnInfo, field,
                start.rewriteWithTargetList(targetList),
                end.rewriteWithTargetList(targetList)
        )
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return DatediffExpr(columnInfo, field,
                start.rewriteWithChildTargetList(targetList),
                end.rewriteWithChildTargetList(targetList)
        )
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return DatediffExpr(columnInfo, field,
                start.rewriteAggToVar(targetList),
                end.rewriteAggToVar(targetList)
        )
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            start.findExpression(matchFunc, exprList)
            end.findExpression(matchFunc, exprList)
        }
    }

    override fun deepCopy(): Expression {
        return DatediffExpr(columnInfo, field, start.deepCopy(), end.deepCopy())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DatediffExpr

        if (field != other.field) return false
        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }

    override fun toString(): String {
        return "DATEDIFF($field START $start END $end)"
    }

}

