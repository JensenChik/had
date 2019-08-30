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

import tech.cuda.enums.StatementType


/**
 * parse tree for a query
 */
data class Query(
        var isDistinct: Boolean = false,
        var wherePredicate: Expression? = null,
        var havingPredicate: Expression? = null,
        var orderBy: OrderEntry? = null,
        var groupBy: Expression? = null,
        var nextQuery: Query? = null,
        var isUnionAll: Boolean = false,
        var statementType: StatementType = StatementType.SELECT,
        var numAggs: Int = 0,
        var resultTableId: Int = 0,
        var resultColumnList: List<Int>,
        var limit: Long = 0,
        var offset: Long = 0,
        val targetList: MutableList<TargetEntry>,
        val rangeTable: MutableList<RangeTableEntry>

) {

    fun getRteIdx(rangeVarName: String): Int {
        rangeTable.forEachIndexed { index, entry ->
            if (entry.rangeVar == rangeVarName) {
                return index
            }
        }
        return -1
    }

    fun getRte(rteIdx: Int) = rangeTable[rteIdx]


    fun addRte(rte: RangeTableEntry) {
        rangeTable.add(rte)
    }

    fun addTLE(tle: TargetEntry) = targetList.add(tle)


}