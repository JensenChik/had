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

import tech.cuda.shared.ColumnInfo
import java.lang.Exception
import java.util.*

/**
 * subquery expression.  Note that the type of the expression is the type of the
 * TargetEntry in the subquery instead of the set.
 */
class SubQuery(columnInfo: ColumnInfo, val parseTree: Query) : Expression(columnInfo) {
    override fun addCast(anotherColumnInfo: ColumnInfo): Expression {
        throw Exception("not supported yet.")
    }

    override fun groupPredicates(scanPredicates: MutableList<Expression>, joinPredicates: MutableList<Expression>, constPredicates: MutableList<Expression>) {
        throw Exception("not supported yet.")
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        throw Exception("not supported yet.")
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        throw Exception("not supported yet.")
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        throw Exception("not supported yet.")
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        throw Exception("not supported yet.")
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        throw Exception("not supported yet.")
    }

    override fun deepCopy(): Expression {
        throw Exception("not supported yet.")
    }

    override fun equals(other: Any?): Boolean {
        throw Exception("not supported yet.")
    }

    override fun hashCode(): Int {
        return parseTree.hashCode()
    }

    override fun toString(): String {
        return "(SubQuery )"
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        throw Exception("not supported yet.")
    }
}