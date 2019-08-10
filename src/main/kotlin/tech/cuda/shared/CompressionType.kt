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

enum class CompressionType {
    NULL,          // no encoding
    FIXED,         // Fixed-bit encoding
    RL,            // Run Length encoding
    DIFF,          // Differential encoding
    DICT,          // Dictionary encoding
    SPARSE,        // Null encoding for sparse columns
    GEO_INT,        // Encoding coordinates as integers
    DATE_IN_DAYS,  // Date encoding in days
    LAST
}