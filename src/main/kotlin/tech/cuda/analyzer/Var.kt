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

import tech.cuda.enums.WhichRow
import tech.cuda.exception.NotFoundException
import tech.cuda.shared.ColumnInfo
import java.lang.Exception

/**
 * expression that evaluates to the value of a column in a given row generated
 * from a query plan node. It is only used in plan nodes above Scan nodes.
 * The row can be produced by either the inner or the outer plan in case of a join.
 * It inherits from ColumnVar to keep track of the lineage through the plan nodes.
 * The table_id will be set to 0 if the Var does not correspond to an original column
 * value.
 */


class Var(columnInfo: ColumnInfo, tblID: Int = 0, colId: Int = 0, rteIdx: Int = -1,
          var whichRow: WhichRow, var varNum: Int)
    : ColumnVar(columnInfo, tblID, colId, rteIdx) {

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        rteSet.add(-1)
    }

    override fun deepCopy(): Expression {
        return Var(columnInfo, tableId, columnId, rteIdx, whichRow, varNum)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return this.deepCopy()
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return this.deepCopy()
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        targetList.forEachIndexed { varNum, it ->
            if (it.expression == this) {
                return Var(it.expression.columnInfo, whichRow = WhichRow.INPUT_OUTER, varNum = varNum)
            }
        }
        throw NotFoundException("Var from having clause", "targetList")
    }

    override fun checkGroupBy(groupBy: List<Expression>) {
        if (whichRow != WhichRow.GROUP_BY) {
            throw Exception("Internal error: invalid VAR in GROUP BY or HAVING.")
        }
    }

    override fun toString(): String {
        return """(Var table: $tableId column: $columnId
            |rte: $rteIdx which_row: $whichRow varNum: $varNum)""".trimMargin()
    }


}