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
package tech.cuda.shared

import tech.cuda.enums.ColumnType
import java.text.SimpleDateFormat
import java.util.*

sealed class Datum(var value: Any?) {

    class BOOLEAN(value: Boolean?) : Datum(value) {
        override fun toString() = if (value as Boolean) "t" else "f"
    }

    class TINY_INT(value: Byte?) : Datum(value) {
        override fun toString() = (value as Byte).toString()
    }

    class SMALL_INT(value: Short?) : Datum(value) {
        override fun toString() = (value as Short).toString()
    }

    class INT(value: Int?) : Datum(value) {
        override fun toString() = (value as Int).toString()
    }

    class BIG_INT(value: Long?) : Datum(value) {
        override fun toString() = (value as Long).toString()
    }

    class FLOAT(value: Float?) : Datum(value) {
        override fun toString() = (value as Float).toString()
    }

    class DOUBLE(value: Double?) : Datum(value) {
        override fun toString() = (value as Double).toString()
    }

    class ARRAY(value: VarLenDatum?) : Datum(value) {
        override fun toString() = "ARRAY"
    }

    class STRING(value: String?) : Datum(value) {
        override fun toString() = value as String
    }

    infix fun formatString(columnInfo: ColumnInfo): String {
        if (columnInfo.type != ColumnType.TIME
                && columnInfo.type != ColumnType.TIMESTAMP
                && columnInfo.type != ColumnType.DATE
                && columnInfo.type != ColumnType.INTERVAL_DAY_TIME
                && columnInfo.type != ColumnType.INTERVAL_YEAR_MONTH
        ) {
            value.toString()
        } else {
            when (columnInfo.type) {
                ColumnType.TIME, ColumnType.TIMESTAMP ->
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(value as Long))
                ColumnType.DATE ->
                    SimpleDateFormat("yyyy-MM-dd").format(Date(value as Long))
                ColumnType.INTERVAL_DAY_TIME -> "$value ms (day-time interval)"
                ColumnType.INTERVAL_YEAR_MONTH -> "$value month(s) (year-month interval)"
                else -> throw Exception("Internal error: invalid type ${columnInfo.type}")
            }
        }


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Datum

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }


}

