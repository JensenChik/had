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

import tech.cuda.enums.CompressionType
import tech.cuda.enums.WhichRow
import tech.cuda.exception.NotFoundException
import tech.cuda.shared.ColumnInfo
import java.util.*

/**
 * expression that evaluates to the value of a column in a given row from a base
 * table. It is used in parse trees and is only used in Scan nodes in a query plan for
 * scanning a table while Var nodes are used for all other plans.
 */
open class ColumnVar(columnInfo: ColumnInfo, val tableId: Int, val columnId: Int, val rteIdx: Int)
    : Expression(columnInfo) {

    val compression: CompressionType get() = columnInfo.compression
    val compPram: Int get() = columnInfo.compParam

    infix fun lessThan(other: ColumnVar): Boolean {
        return when {
            tableId < other.tableId -> true
            tableId == other.tableId -> columnId < other.columnId
            else -> false
        }
    }

    override fun checkGroupBy(groupBy: List<Expression>) {
        if (groupBy.isNotEmpty()) {
            groupBy.forEach {
                it as ColumnVar
                if (tableId == it.tableId && columnId == it.columnId) {
                    return
                }
            }
        }
        throw Exception("""expressions in the SELECT or HAVING clause must be an
            aggregate function or an expression over GROUP BY columns.""".trimIndent())
    }

    override fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>) {
        if (columnInfo.type.isBoolean) {
            scanPredicates.add(this)
        }
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEach {
            val colVar = it.expression as ColumnVar
            if (tableId == colVar.tableId && columnId == colVar.columnId) {
                return colVar.deepCopy()
            }
        }
        throw NotFoundException("ColumnVar", "targetList")
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEachIndexed { varNum, it ->
            val colVar = it.expression as ColumnVar
            if (tableId == colVar.tableId && columnId == colVar.columnId) {
                return Var(colVar.columnInfo, colVar.tableId, colVar.columnId, colVar.rteIdx,
                        WhichRow.INPUT_OUTER, varNum)
            }
        }
        throw NotFoundException("ColumnVar", "targetList")
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEachIndexed { varNum, it ->
            if (it.expression is ColumnVar) {
                val colVar = it.expression
                if (tableId == colVar.tableId && columnId == colVar.columnId) {
                    return Var(colVar.columnInfo, colVar.tableId, colVar.columnId, colVar.rteIdx,
                            WhichRow.INPUT_OUTER, varNum)
                }
            }
        }
        throw NotFoundException("ColumnVar from having clause", "targetList")
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        super.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        colVarSet.add(this)
    }

    override fun deepCopy(): Expression {
        return ColumnVar(columnInfo, tableId, columnId, rteIdx)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColumnVar) {
            return false
        } else {
            if (rteIdx != -1) {
                return tableId == other.tableId
                        && columnId == other.columnId
                        && rteIdx == other.rteIdx
            }
            this as Var
            other as Var
            return this.whichRow == other.whichRow
                    && this.varNum == other.varNum
        }
    }

    override fun hashCode(): Int {
        var result = tableId
        result = 31 * result + columnId
        result = 31 * result + rteIdx
        return result
    }

    override fun toString(): String {
        return "ColumnVar table: $tableId column: $columnId rte=$rteIdx ${columnInfo.typeName})"
    }

}
