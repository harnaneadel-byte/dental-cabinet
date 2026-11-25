/**
 * iOS Data Layer Placeholder
 *
 * This module provides a shared data access layer for KMP.
 * iOS implementation can use:
 * - SQLDelight for compiled SQL
 * - native SQLite via Kotlin/Native expect/actual
 * - CoreData bridging
 *
 * Expect/Aactual pattern for iOS platform-specific implementations.
 */

package dz.cabinet.data

/**
 * Expected: iOS-specific data layer implementation
 *
 * Android implements this via Room/SQLCipher.
 * iOS should implement via SQLDelight or CoreData.
 */
expect interface CacheManager {
    /** Cache patient data for offline use */
    suspend fun cachePatient(id: String, data: ByteArray)

    /** Retrieve cached patient data */
    suspend fun retrievePatient(id: String): Result<ByteArray>

    /** Clear all cached data */
    suspend fun clearCache()

    /** Check if data is cached */
    suspend fun isCached(id: String): Boolean
}

/**
 * Actual: iOS implementation placeholder
 *
 * Implement this in iosMain using SQLDelight or CoreData.
 */
actual object CacheManager {
    /** No-op default implementation */
    suspend fun cachePatient(id: String, data: ByteArray) {
        // TODO: iOS implementation
    }

    /** No-op default implementation */
    suspend fun retrievePatient(id: String): Result<ByteArray> =
        Result.Failure(ErrorCode.INVALID_AMOUNT)

    /** No-op default implementation */
    suspend fun clearCache() {
        // TODO: iOS implementation
    }

    /** No-op default implementation */
    suspend fun isCached(id: String): Boolean = false
}

/**
 * iOS-specific error codes
 */
actual enum class iOSErrorCode {
    /** Data not found */
    DATA_NOT_FOUND,

    /** Database locked */
    DATABASE_LOCKED,

    /** Migration failed */
    MIGRATION_FAILED,

    /** Custom error from iOS */
    CUSTOM_ERROR(String): ErrorCode
}