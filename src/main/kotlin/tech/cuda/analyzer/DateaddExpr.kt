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

import tech.cuda.enums.DateAddField
import tech.cuda.shared.ColumnInfo
import java.util.*

class DateaddExpr(
        columnInfo: ColumnInfo,
        val field: DateAddField,
        val number: Expression,
        val datetime: Expression
) : Expression(columnInfo) {

    override fun checkGroupBy(groupBy: List<Expression>) {
        number.checkGroupBy(groupBy)
        datetime.checkGroupBy(groupBy)
    }

    override fun groupPredicates(scanPredicates: MutableList<Expression>, joinPredicates: MutableList<Expression>, constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        number.collectRangeTableEntriesIndexTo(rteIdxSet)
        datetime.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        number.collectColumnVar(colVarSet, hasAgg)
        datetime.collectColumnVar(colVarSet, hasAgg)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return DateaddExpr(columnInfo, field,
                number.rewriteWithTargetList(targetList),
                datetime.rewriteWithTargetList(targetList)
        )
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return DateaddExpr(columnInfo, field,
                number.rewriteWithChildTargetList(targetList),
                datetime.rewriteWithChildTargetList(targetList)
        )
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return DateaddExpr(columnInfo, field,
                number.rewriteAggToVar(targetList),
                datetime.rewriteAggToVar(targetList)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DateaddExpr

        return field == other.field && number == other.number && datetime == other.datetime
    }


    override fun deepCopy(): Expression {
        return DateaddExpr(columnInfo, field, number.deepCopy(), datetime.deepCopy())
    }

    override fun toString(): String {
        return "DATEADD($field NUMBER $number DATETIME $datetime)"
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            number.findExpression(matchFunc, exprList)
            datetime.findExpression(matchFunc, exprList)
        }
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + datetime.hashCode()
        return result
    }
}

