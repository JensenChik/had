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
import kotlin.reflect.KClass

sealed class Datum(var value: Any?) {

    class BOOLEAN(value: Boolean?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val isTrue = value as Boolean
            return when (clazz) {
                TINY_INT::class -> if (isTrue) TINY_INT(1) else TINY_INT(0)
                SMALL_INT::class -> if (isTrue) SMALL_INT(1) else SMALL_INT(0)
                INT::class -> if (isTrue) INT(1) else INT(0)
                BIG_INT::class -> if (isTrue) BIG_INT(1) else BIG_INT(0)
                STRING::class -> if (isTrue) STRING("T") else STRING("F")
                else -> throw Exception("can not cast BOOL to $clazz")
            }
        }

        override fun toString() = if (value as Boolean) "t" else "f"

    }

    class TINY_INT(value: Byte?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Byte
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                TINY_INT::class -> TINY_INT(currentVal)
                SMALL_INT::class -> SMALL_INT(currentVal.toShort())
                INT::class -> INT(currentVal.toInt())
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                FLOAT::class -> FLOAT(currentVal.toFloat())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast TINYINT to $clazz")
            }
        }

        override fun toString() = (value as Byte).toString()
    }

    class SMALL_INT(value: Short?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Short
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                SMALL_INT::class -> SMALL_INT(currentVal)
                INT::class -> INT(currentVal.toInt())
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                FLOAT::class -> FLOAT(currentVal.toFloat())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast SMALLINT to $clazz")
            }
        }

        override fun toString() = (value as Short).toString()
    }

    class INT(value: Int?) : Datum(value) {
        override fun toString() = (value as Int).toString()
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Int
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                INT::class -> INT(currentVal)
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                FLOAT::class -> FLOAT(currentVal.toFloat())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast INT to $clazz")
            }
        }
    }

    class BIG_INT(value: Long?) : Datum(value) {

        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Long
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                BIG_INT::class -> BIG_INT(currentVal)
                FLOAT::class -> FLOAT(currentVal.toFloat())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast BIGINT to $clazz")
            }
        }

        override fun toString() = (value as Long).toString()
    }

    class FLOAT(value: Float?) : Datum(value) {

        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Float
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                TINY_INT::class -> TINY_INT(currentVal.toByte())
                SMALL_INT::class -> SMALL_INT(currentVal.toShort())
                INT::class -> INT(currentVal.toInt())
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast FLOAT to $clazz")
            }
        }

        override fun toString() = (value as Float).toString()
    }

    class DOUBLE(value: Double?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as Double
            return when (clazz) {
                BOOLEAN::class -> BOOLEAN(value == 1)
                TINY_INT::class -> TINY_INT(currentVal.toByte())
                SMALL_INT::class -> SMALL_INT(currentVal.toShort())
                INT::class -> INT(currentVal.toInt())
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                FLOAT::class -> FLOAT(currentVal.toFloat())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast DOUBLE to $clazz")
            }
        }

        override fun toString() = (value as Double).toString()
    }

    class ARRAY(value: VarLenDatum?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            throw Exception("cast for ARRAY is unsupported")
        }

        override fun toString() = "ARRAY"
    }

    class STRING(value: String?) : Datum(value) {
        override fun <T : Datum> castTo(clazz: KClass<T>): Datum {
            val currentVal = value as String
            return when (clazz) {
                BOOLEAN::class -> when {
                    currentVal.equals("T", ignoreCase = true) -> BOOLEAN(true)
                    currentVal.equals("F", ignoreCase = true) -> BOOLEAN(false)
                    else -> BOOLEAN(null)
                }
                TINY_INT::class -> TINY_INT(currentVal.toByte())
                SMALL_INT::class -> SMALL_INT(currentVal.toShort())
                INT::class -> INT(currentVal.toInt())
                BIG_INT::class -> BIG_INT(currentVal.toLong())
                FLOAT::class -> FLOAT(currentVal.toFloat())
                DOUBLE::class -> DOUBLE(currentVal.toDouble())
                STRING::class -> STRING(currentVal.toString())
                else -> throw Exception("can not cast STRING to $clazz")
            }
        }

        override fun toString() = value as String
    }

    abstract fun <T : Datum> castTo(clazz: KClass<T>): Datum


    infix fun toStringWith(columnInfo: ColumnInfo): String {
        return if (columnInfo.type != ColumnType.TIME
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

