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

enum class DataType {
    NULL,
    BOOLEAN,
    CHAR,
    VARCHAR,
    TEXT,
    NUMERIC,
    TINYINT,
    SMALLINT,
    INT,
    BIGINT,
    DECIMAL,
    FLOAT,
    DOUBLE,
    TIME,
    TIMESTAMP,
    DATE,
    INTERVAL_DAY_TIME,
    INTERVAL_YEAR_MONTH,
    ARRAY,
    SET,
    MAP,
    POINT,
    LINESTRING,
    POLYGON,
    MULTIPOLYGON,
    GEOMETRY,
    GEOGRAPHY,
    EVAL_CONTEXT_TYPE,
    SQL_TYPE_LAST;

    val isArray: Boolean get() = this == ARRAY
    val isBoolean: Boolean get() = this == BOOLEAN
    val isString: Boolean get() = this in setOf(TEXT, VARCHAR, CHAR)
    val isInteger: Boolean get() = this in setOf(SMALLINT, BIGINT, TINYINT, INT)
    val isFloatPoint: Boolean get() = this in setOf(FLOAT, DOUBLE)
    val isTime: Boolean get() = this in setOf(TIME, TIMESTAMP, DATE)
    val isGeo: Boolean get() = this in setOf(POINT, LINESTRING, POLYGON, MULTIPOLYGON)
    val isInterval: Boolean get() = this in setOf(INTERVAL_YEAR_MONTH, INTERVAL_DAY_TIME)
    val isDecimal: Boolean get() = this in setOf(NUMERIC, DECIMAL)
    val isGeoPoly: Boolean get() = this in setOf(POLYGON, MULTIPOLYGON)
    val isNumber: Boolean
        get() = this in setOf(
                SMALLINT, BIGINT, TINYINT, INT,
                NUMERIC, DECIMAL, FLOAT, DOUBLE
        )
}

infix fun DataType.canBeCastTo(another: DataType) = when {
    this == another -> true
    this.isString or another.isString -> true
    this.isNumber and another.isNumber -> true
    this.isTime and another.isNumber -> true
    this.isNumber and another.isTime -> true
    this.isBoolean and another.isNumber -> true
    this.isNumber and another.isBoolean -> true
    else -> false
}
