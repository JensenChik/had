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
 * expression for the LIKE predicate.
 * arg must evaluate to char, varchar or text.
 */
class LikeExpr(
        val arg: Expression,
        val likeExpr: Expression,
        val escapeExpr: Expression?,
        val isILike: Boolean,
        val isSimple: Boolean) : Expression(ColumnType.BOOLEAN) {

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        arg.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        arg.collectColumnVar(colVarSet, hasAgg)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return LikeExpr(arg.rewriteWithTargetList(targetList),
                likeExpr.deepCopy(), escapeExpr?.deepCopy(), isILike, isSimple)
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return LikeExpr(arg.rewriteWithChildTargetList(targetList),
                likeExpr.deepCopy(), escapeExpr?.deepCopy(), isILike, isSimple)
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return LikeExpr(arg.rewriteAggToVar(targetList),
                likeExpr.deepCopy(), escapeExpr?.deepCopy(), isILike, isSimple)
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

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            arg.findExpression(matchFunc, exprList)
            likeExpr.findExpression(matchFunc, exprList)
            escapeExpr?.findExpression(matchFunc, exprList)
        }
    }

    override fun deepCopy(): Expression {
        return LikeExpr(arg.deepCopy(), likeExpr.deepCopy(), escapeExpr?.deepCopy(), isILike, isSimple)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LikeExpr

        if (arg != other.arg) return false
        if (likeExpr != other.likeExpr) return false
        if (isILike != other.isILike) return false
        if (escapeExpr != other.escapeExpr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arg.hashCode()
        result = 31 * result + likeExpr.hashCode()
        result = 31 * result + (escapeExpr?.hashCode() ?: 0)
        result = 31 * result + isILike.hashCode()
        result = 31 * result + isSimple.hashCode()
        return result
    }

    override fun toString(): String {
        return "(LIKE $arg $likeExpr ${escapeExpr?.toString()})"
    }

}