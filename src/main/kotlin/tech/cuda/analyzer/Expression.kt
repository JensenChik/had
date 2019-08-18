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

import tech.cuda.exception.CastException
import tech.cuda.enums.CompressionType
import tech.cuda.enums.ColumnType
import tech.cuda.enums.SQLOps
import tech.cuda.shared.ColumnInfo
import java.util.*

/**
 * super class for all expressions in parse trees and in query plans
 */
abstract class Expression(var columnInfo: ColumnInfo, var hasAgg: Boolean = false) {

    constructor(type: ColumnType, dimension: Int = 0, scale: Int = 0)
            : this(ColumnInfo(type, dimension = dimension, scale = scale))

    open fun addCast(anotherColumnInfo: ColumnInfo): Expression {
        if (this.columnInfo == anotherColumnInfo) {
            return this
        }
        if (this.columnInfo.type.isString && anotherColumnInfo.type.isString
                && this.columnInfo.compression == CompressionType.DICT
                && anotherColumnInfo.compression == CompressionType.DICT
                && this.columnInfo.compParam == anotherColumnInfo.compParam
        ) {
            return this
        }
        if (!(this.columnInfo canBeCastTo anotherColumnInfo)) {
            throw CastException(this.columnInfo.typeName, anotherColumnInfo.typeName)
        }
        return UnaryOperator(anotherColumnInfo, this.hasAgg, SQLOps.CAST, this)
    }

    open fun checkGroupBy(groupBy: List<Expression>) {
    }


    /**
     * separate conjunctive predicates into [scanPredicates],
     * [joinPredicates] and [constPredicates].
     */
    open fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>
    ) {
    }

    /**
     * collects the indices of all the range table
     * entries involved in an expression
     */
    open fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {

    }


    /**
     * collects all unique ColumnVar nodes in an expression
     * If [hasAgg] = false, it does not include to ColumnVar nodes inside
     * the argument to AggExpr's.  Otherwise, they are included.
     * It does not make copies of the ColumnVar
     */
    open fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {

    }

    /**
     * rewrite ColumnVar's in expression with entries in a [targetList].
     * [targetList] expressions are expected to be only Var's or AggExpr's.
     * returns a new expression copy
     */
    open fun rewriteWithTargetList(targetList: List<TargetEntry>): Expression {
        return deepCopy()
    }

    /**
     * rewrite ColumnVar's in expression with entries in a child plan's [targetList].
     * [targetList] expressions are expected to be only Var's or * ColumnVar's
     * returns a new expression copy
     */
    open fun rewriteWithChildTargetList(targetList: List<TargetEntry>): Expression {
        return deepCopy()
    }

    /**
     * rewrite ColumnVar's in expression with entries in an AggPlan's [targetList].
     * [targetList] expressions are expected to be only Var's or ColumnVar's or AggExpr's
     * All AggExpr's are written into Var's.
     * returns a new expression copy
     */
    open fun rewriteAggToVar(targetList: List<TargetEntry>): Expression {
        return deepCopy()
    }

    /**
     * add self to [exprList] if self not exists in [exprList]
     */
    open fun addUnique(exprList: ExpressionList) {
        if (exprList.contains(this)) {
            return
        } else {
            exprList.add(this)
        }
    }

    /**
     * traverse Expr hierarchy and adds the node pointer to
     * the [exprList] if the function [matchFunc] returns true.
     * Duplicate Expr's are not added the list.
     */
    open fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        }
    }

    /**
     * decompress adds cast operator to decompress encoded result
     */
    open fun decompress(): Expression {
        if (this.columnInfo.compression == CompressionType.NULL) {
            return this
        }
        return UnaryOperator(
                ColumnInfo(
                        type = this.columnInfo.type,
                        subType = this.columnInfo.subType,
                        dimension = this.columnInfo.dimension,
                        scale = this.columnInfo.scale
                ),
                this.hasAgg,
                SQLOps.CAST,
                this
        )
    }

    /**
     * perform domain analysis on Expr and fill in domain information in [domainSet].
     * Empty [domainSet] means no information.
     */
    open fun getDomain(domainSet: DomainSet) {
        domainSet.clear()
    }

    abstract fun deepCopy(): Expression


}