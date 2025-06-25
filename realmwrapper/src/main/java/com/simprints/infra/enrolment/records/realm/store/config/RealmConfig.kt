package com.simprints.infra.enrolment.records.realm.store.config

import androidx.annotation.Keep
import com.simprints.infra.enrolment.records.realm.store.migration.RealmMigrations
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbProject
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import io.realm.kotlin.RealmConfiguration
import javax.inject.Inject

@Keep
class RealmConfig @Inject constructor()  {
    fun get(
        databaseName: String,
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
//        .migration(
//            migration = RealmMigrations(),
//            resolveEmbeddedObjectConstraints = true,
//        )
        .build()

    companion object {
        private const val REALM_SCHEMA_VERSION: Long = 17
    }
}
