package com.simprints.infra.enrolment.records.realm.store

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.simprints.infra.enrolment.records.realm.store.config.LocalDbKey
import com.simprints.infra.enrolment.records.realm.store.config.RealmConfig
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject


class RealmWrapper @Inject constructor (
    private val appContext: Context,
    private val configFactory: RealmConfig = RealmConfig()  ,
    private val localDbKey: LocalDbKey = LocalDbKey("default_project_id", "default_key".toByteArray()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
 )  {
    // Kotlin-realm claims to be thread-safe and there is no need to handle closing manually
    // https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/frozen-arch/#thread-safe-realms
    private lateinit var realm: Realm
    private lateinit var config: RealmConfiguration
    private val mutex = Mutex()

    private suspend fun getRealm(): Realm {
        if (!this::realm.isInitialized) {
            config = createAndSaveRealmConfig()
            realm = createRealm()
        }
        return realm
    }

    private suspend fun createRealm(): Realm = mutex.withLock {
        //Simber.d("[RealmWrapper] getting new realm instance", tag = REALM_DB)
        try {
            try {
                Realm.open(config)
            } catch (ex: IllegalStateException) {
                // On the schema update realm it is not being closed correctly and there
                // is no legitimate way to forcefully restart the realm instance,
                // so we need to catch this exception and try opening realm again.
                // If the exception repeats, it should be propagated up the call stack.
                if (!isFileCorruptException(ex)) {
                    Realm.open(config)
                } else {
                    throw ex
                }
            }
        } catch (ex: Exception) {
            if (isFileCorruptException(ex)) {
                // DB corruption detected; either DB file or key is corrupt
                // 1. Delete DB file in order to create a new one at next init
                Realm.deleteRealm(config)
                // 2. Recreate the DB key
          //      recreateLocalDbKey()
                // 3. Log exception after recreating the key so we get extra info
       //         Simber.e("Realm DB recreated due to corruption", ex, tag = DB_CORRUPTION)
                // 4. Update Realm config with the new key
                config = createAndSaveRealmConfig()
                // 5. Delete "last sync" info and start new sync
                resetDownSyncState()
                // 6. Retry operation with new file and key
                Realm.open(config)
            } else {
                throw ex
            }
        }
    }

    private fun isFileCorruptException(ex: Exception) = ex is IllegalStateException &&
        ex.message?.contains("RLM_ERR_INVALID_DATABASE") == true

    /**
     * Executes provided block ensuring a valid Realm instance is used and closed.
     */
    suspend fun <R> readRealm(block: suspend (Realm) -> R): R = withContext(dispatcher) { block(getRealm()) }

    /**
     * Executes provided block in a transaction ensuring a valid Realm instance is used and closed.
     */
    suspend fun <R> writeRealm(block: (MutableRealm) -> R) {
        withContext(dispatcher) { getRealm().write(block) }
    }

    private fun createAndSaveRealmConfig(): RealmConfiguration {
        return configFactory.get(localDbKey.projectId)
    }



    private fun resetDownSyncState() {
        // This is a workaround to avoid a circular module dependency
        val intent = Intent()
        intent.component = ComponentName(
            "com.simprints.id",
            "com.simprints.id.services.sync.events.down.EventDownSyncResetService",
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        } catch (ex: Exception) {
           // Simber.e("Unable to start sync reset service", ex, tag = REALM_DB)
        }
    }
}
