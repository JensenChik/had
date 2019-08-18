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

import tech.cuda.enums.CompressionType
import tech.cuda.enums.ColumnType
import tech.cuda.enums.canBeCastTo

enum class PackagingType { CHUNK, STANDARD_BUFFER }

/**
 *  a structure to capture all type information including
 *  length, precision, scale, etc.
 */
class ColumnInfo(
        val type: ColumnType = ColumnType.NULL,
        val subType: ColumnType = ColumnType.NULL,
        val dimension: Int = 0,
        val scale: Int = 0,
        val compression: CompressionType = CompressionType.NULL,
        val compParam: Int = 0
) {

    var packagingType: PackagingType = PackagingType.CHUNK
    val isStandardBufferPackaging: Boolean get() = packagingType == PackagingType.STANDARD_BUFFER
    val isChunkPackagingType: Boolean get() = packagingType == PackagingType.CHUNK
    fun setStandardBufferPackaging() {
        this.packagingType = PackagingType.STANDARD_BUFFER
    }


    val precision: Int get() = dimension
    val inputSrid: Int get() = dimension
    val outputSrid: Int get() = scale
    val size: Int
        get() = when (type) {
            ColumnType.BOOLEAN -> 1
            ColumnType.TINYINT -> 1
            ColumnType.SMALLINT -> when (compression) {
                CompressionType.NULL -> 2
                setOf(CompressionType.FIXED, CompressionType.SPARSE) -> compParam / 8
                setOf(CompressionType.RL, CompressionType.DIFF) -> -1
                else -> throw Exception("invalid compression encoding type")
            }
            ColumnType.INT -> when (compression) {
                CompressionType.NULL -> 4
                setOf(CompressionType.FIXED, CompressionType.SPARSE) -> compParam / 8
                setOf(CompressionType.RL, CompressionType.DIFF) -> -1
                else -> throw Exception("invalid compression encoding type")
            }
            setOf(ColumnType.BIGINT, ColumnType.DECIMAL, ColumnType.NUMERIC) -> when (compression) {
                CompressionType.NULL -> 8
                setOf(CompressionType.FIXED, CompressionType.SPARSE) -> compParam / 8
                setOf(CompressionType.RL, CompressionType.DIFF) -> -1
                else -> throw Exception("invalid compression encoding type")
            }
            ColumnType.FLOAT -> when (compression) {
                CompressionType.NULL -> 4
                else -> throw Exception("invalid compression encoding type for float")
            }
            ColumnType.DOUBLE -> when (compression) {
                CompressionType.NULL -> 8
                else -> throw Exception("invalid compression encoding type for double")
            }
            setOf(ColumnType.TIMESTAMP, ColumnType.TIME, ColumnType.INTERVAL_DAY_TIME,
                    ColumnType.INTERVAL_YEAR_MONTH, ColumnType.DATE) -> when (compression) {
                CompressionType.NULL -> 8
                CompressionType.FIXED -> if (type == ColumnType.TIMESTAMP && dimension > 0)
                    throw Exception("invalid compression encoding type") else compParam / 8
                CompressionType.DATE_IN_DAYS -> when (compParam) {
                    0 -> 4
                    16 -> 2
                    32 -> 4
                    else -> throw Exception("invalid compression encoding type")
                }
                else -> throw Exception("invalid compression encoding type")
            }
            setOf(ColumnType.TEXT, ColumnType.VARCHAR, ColumnType.CHAR) -> if (compression == CompressionType.DICT) 4 else -1
            setOf(ColumnType.ARRAY, ColumnType.POINT, ColumnType.LINESTRING, ColumnType.POLYGON, ColumnType.MULTIPOLYGON) -> -1
            else -> throw Exception("invalid SQL Type")
        }

    val logicalSize: Int
        get() = when (compression) {
            CompressionType.DICT -> 4
            setOf(CompressionType.FIXED, CompressionType.DATE_IN_DAYS) -> ColumnInfo(
                    type, subType = subType, dimension = dimension, scale = scale
            ).size
            else -> size
        }

    val arrayLogicalSize: Int
        get() = if (type.isString && compression in setOf(
                        CompressionType.NULL,
                        CompressionType.DICT,
                        CompressionType.FIXED)
        ) 4 else logicalSize


    val physicalCols: Int
        get() = when (type) {
            ColumnType.POINT -> 1
            ColumnType.LINESTRING -> 2
            ColumnType.POLYGON -> 4
            ColumnType.MULTIPOLYGON -> 5
            else -> 0
        }

    val physicalCoordCols: Int
        get() = when (type) {
            ColumnType.POINT -> 1
            ColumnType.LINESTRING -> 1
            ColumnType.POLYGON -> 2
            ColumnType.MULTIPOLYGON -> 3
            else -> 0
        }

    val hasBounds: Boolean
        get() {
            return type in setOf(ColumnType.LINESTRING, ColumnType.POLYGON, ColumnType.MULTIPOLYGON)
        }

    val hasRenderGroup: Boolean get() = type.isGeoPoly

    val isDate: Boolean get() = type == ColumnType.DATE

    val isDateInDays: Boolean
        get() = isDate && compression == CompressionType.DATE_IN_DAYS

    val isTimestamp: Boolean get() = type == ColumnType.TIMESTAMP

    val isHighPrecisionTimestamp: Boolean get() = isTimestamp && dimension > 0


    val typeName: String
        get() {
            if (this.type.isGeo) {
                return "${this.subType}(${this.type}, $outputSrid)"
            }
            val ps = when {
                this.type.isDecimal || this.subType.isDecimal -> "($dimension, $scale)"
                this.type == ColumnType.TIMESTAMP -> "($dimension)"
                else -> ""
            }
            return if (this.type.isArray)
                "${this.subType}$ps[${this.size / this.element().size}]"
            else
                "${this.type}$ps"
        }

    val compressionName: String
        get() {
            return this.compression.toString()
        }

    infix fun canBeCastTo(another: ColumnInfo): Boolean {
        return this.type canBeCastTo another.type
                && this.subType canBeCastTo another.subType
    }


    fun element(): ColumnInfo {
        return ColumnInfo(
                type = this.subType,
                dimension = this.dimension,
                scale = this.scale,
                compression = this.compression,
                compParam = this.compParam
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other !is ColumnInfo) false
        else type == other.type
                && subType == other.subType
                && dimension == other.dimension
                && scale == other.scale
                && compression == other.compression
                && (compression == CompressionType.NULL
                || compParam == other.compParam)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + subType.hashCode()
        result = 31 * result + dimension
        result = 31 * result + scale
        result = 31 * result + compression.hashCode()
        result = 31 * result + compParam
        result = 31 * result + packagingType.hashCode()
        return result
    }


}