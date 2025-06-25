package com.simprints.infra.enrolment.records.realm.store.config

import androidx.annotation.Keep
import com.simprints.infra.enrolment.records.realm.store.migration.RealmMigrations
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbProject
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import io.realm.kotlin.RealmConfiguration

@Keep
class RealmConfig  {
    fun get(
        databaseName: String,
        key: ByteArray,
    ) = RealmConfiguration
        .Builder(
            setOf(
                DbFingerprintSample::class,
                DbFaceSample::class,
                DbSubject::class,
                DbProject::class,
            ),
        ).name("$databaseName.realm")
        .schemaVersion(REALM_SCHEMA_VERSION)
        .migration(
            migration = RealmMigrations(),
            resolveEmbeddedObjectConstraints = true, // Delete embedded objects if they are not in the schema anymore
        )
        //.let { if (BuildConfig.DB_ENCRYPTION) it.encryptionKey(key) else it }
        .build()

    companion object {
        private const val REALM_SCHEMA_VERSION: Long = 17
    }
}
