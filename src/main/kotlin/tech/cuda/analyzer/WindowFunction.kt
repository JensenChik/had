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

import tech.cuda.enums.WindowFunctionType
import tech.cuda.shared.ColumnInfo

class WindowFunction(
        columnInfo: ColumnInfo,
        val type: WindowFunctionType,
        val args: List<Expression>,
        val partitionKeys: List<Expression>,
        val orderKeys: List<Expression>,
        val collation: List<OrderEntry>
) : Expression(columnInfo) {
    override fun deepCopy(): Expression {
        return WindowFunction(columnInfo, type, args, partitionKeys, orderKeys, collation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowFunction

        if (type != other.type) return false
        if (partitionKeys.size != other.partitionKeys.size
                || orderKeys.size != other.orderKeys.size
                || args.size != other.args.size) {
            return false
        } else {
            (args zip other.args).forEach {
                if (it.first != it.second) return false
            }
            (partitionKeys zip other.partitionKeys).forEach {
                if (it.first != it.second) return false
            }
            (orderKeys zip other.orderKeys).forEach {
                if (it.first != it.second) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + partitionKeys.hashCode()
        result = 31 * result + orderKeys.hashCode()
        result = 31 * result + collation.hashCode()
        return result
    }

    override fun toString(): String {
        return "WindowFunction($type ${args.joinToString( " ")})"
    }

}