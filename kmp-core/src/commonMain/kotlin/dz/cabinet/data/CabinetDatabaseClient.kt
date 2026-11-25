/**
 * SQLDelight database client - shared across Android and iOS
 *
 * Provides typed query functions for the shared CabinetDatabase schema.
 * Generated from the original Room-based implementation.
 */
package dz.cabinet.data

import com.github.sqldelight.SQLDatabase
import com.github.sqldelight.api.Select
import com.github.sqldelight.api.Update
import com.github.sqldelight.db.tablecreate.TableCreator
import kotlin.Result

/**
 * Generated query types from SQLDelight
 */
object GeneratedQueries {
    /**
     * Get dataset identity
     */
    data class DatasetIdentity(
        val id: Int,
        val datasetId: String
    )

    /**
     * Insert dataset identity
     */
    object InsertDatasetIdentity : Update {
        override val target = "dataset_identity"
        override val values = mapOf(
            "id" to 0,  // auto-increment
            "datasetId" to ""
        )
    }

    /**
     * Select all dataset identities
     */
    object SelectAllDatasetIdentity : Select<DatasetIdentity> {
        override val columns = "id, datasetId"
        override val from = "dataset_identity"
    }
}

/**
 * Database client interface for SQLDelight
 */
class CabinetDatabaseClient(
    private val db: SQLDatabase
) {

    /**
     * Get a dataset identity by ID
     */
    suspend fun getDatasetIdentity(id: Int): DatasetIdentity? {
        val result = db.select()
            .from(GeneratedQueries.SelectAllDatasetIdentity)
            .where("id = ?")
            .bind(id)
            .mapTo(DatasetIdentity::class.java)
            .list()
            .await()

        return result.firstOrNull()
    }

    /**
     * Insert a new dataset identity
     */
    suspend fun insertDatasetIdentity(datasetId: String) {
        db.insert()
            .into("dataset_identity")
            .columns("datasetId")
            .values(datasetId)
            .execute()
            .await()
    }

    /**
     * Get all patients
     */
    suspend fun getAllPatients(): List<PatientEntity> {
        val result = db.select()
            .from("patient")
            .columns("id, name, uuid, createdAt, updatedAt")
            .list<PatientEntity>()
            .await()

        return result
    }

    /**
     * Insert a new patient
     */
    suspend fun insertPatient(
        id: Int,
        name: String,
        uuid: String,
        createdAt: Long,
        updatedAt: Long
    ) {
        db.insert()
            .into("patient")
            .columns("id, name, uuid, createdAt, updatedAt")
            .values(id, name, uuid, createdAt, updatedAt)
            .execute()
            .await()
    }
}

/**
 * Patient entity matching SQL schema
 */
data class PatientEntity(
    val id: Int,
    val name: String,
    val uuid: String,
    val createdAt: Long,
    val updatedAt: Long
)