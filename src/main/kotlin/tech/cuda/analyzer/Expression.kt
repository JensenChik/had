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
import tech.cuda.shared.CompressionType
import tech.cuda.shared.DataType
import tech.cuda.shared.SQLOps
import tech.cuda.shared.SQLTypeInfo
import java.util.*

/**
 * super class for all expressions in parse trees and in query plans
 */
abstract class Expression(val typeInfo: SQLTypeInfo, val hasAgg: Boolean = false) {

    constructor(type: DataType, dimension: Int = 0, scale: Int = 0)
            : this(SQLTypeInfo(type, dimension = dimension, scale = scale))

    open fun addCast(another: SQLTypeInfo): Expression {
        if (typeInfo == another) {
            return this
        }
        if (another.type.isString
                && typeInfo.type.isString
                && another.compression == CompressionType.DICT
                && typeInfo.compression == CompressionType.DICT
                && another.compParam == typeInfo.compParam
        ) {
            return this
        }
        if (!(typeInfo canBeCastTo another)) {
            throw CastException(typeInfo.typeName, another.typeName)
        }
        return UnaryOperator(another, this.hasAgg, SQLOps.CAST, this)
    }

    abstract fun checkGroupBy(groupBy: List<Expression>)

    abstract fun deepCopy(): Expression

    /**
     * separate conjunctive predicates into [scanPredicates],
     * [joinPredicates] and [constPredicates].
     */
    abstract fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>
    )

    /**
     * collects the indices of all the range table
     * entries involved in an expression
     */
    abstract fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>)


    /**
     * collects all unique ColumnVar nodes in an expression
     * If [hasAgg] = false, it does not include to ColumnVar nodes inside
     * the argument to AggExpr's.  Otherwise, they are included.
     * It does not make copies of the ColumnVar
     */
    abstract fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean)

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

    abstract fun addUnique(exprList: ExpressionList)

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
     * perform domain analysis on Expr and fill in domain information in [domainSet].
     * Empty [domainSet] means no information.
     */
    open fun getDomain(domainSet: DomainSet) {
    }

}