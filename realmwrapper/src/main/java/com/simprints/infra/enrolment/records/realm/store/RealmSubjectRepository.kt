package com.simprints.infra.enrolment.records.realm.store

import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.realm.store.models.RealmSubject
import com.simprints.infra.enrolment.records.realm.store.models.toDbSubject
import com.simprints.infra.enrolment.records.realm.store.models.toRealmSubject
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.types.RealmUUID
import javax.inject.Inject

@Suppress("unused")
class RealmSubjectRepository @Inject constructor(
    private val realmWrapper: RealmWrapper
) {
    companion object {
        const val PROJECT_ID_FIELD = "projectId"
        const val USER_ID_FIELD = "attendantId"
        const val SUBJECT_ID_FIELD = "subjectId"
        const val MODULE_ID_FIELD = "moduleId"
        const val IS_ATTENDANT_ID_TOKENIZED_FIELD = "isAttendantIdTokenized"
        const val IS_MODULE_ID_TOKENIZED_FIELD = "isModuleIdTokenized"
        const val FINGERPRINT_SAMPLES_FIELD = "fingerprintSamples"
        const val FACE_SAMPLES_FIELD = "faceSamples"
        const val FORMAT_FIELD = "format"

    }

    suspend fun count(
        query: RealmSubjectQuery,
    ): Int = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .count()
            .find()
            .toInt()
    }

    suspend fun load(query: RealmSubjectQuery): List<RealmSubject> = realmWrapper.readRealm {
        it.query(DbSubject::class).buildRealmQueryForSubject(query).find().map { it.toRealmSubject() }

    }


    private fun MutableRealm.findSubject(subjectId: RealmUUID): RealmSubject? =
        query(DbSubject::class).query("$SUBJECT_ID_FIELD == $0", subjectId).first().find()?.toRealmSubject()

    suspend fun loadIdentitiesRange(
        query: RealmSubjectQuery,
        range: IntRange,
    ): List<RealmSubject> = realmWrapper.readRealm { realm ->
        realm
            .query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            // subList's second parameter is exclusive, so we need to add 1 to the last index
            .find { it.subList(range.first, range.last + 1) }.map { it.toRealmSubject() }
    }

    suspend fun deleteSubject(
        query: RealmSubjectQuery
    ) = realmWrapper.writeRealm { realm ->
        realm.query(DbSubject::class)
            .buildRealmQueryForSubject(query)
            .find()
            .forEach { realm.delete(it) }
    }

    suspend fun writeSubject(
        subject: RealmSubject,
    ) = realmWrapper.writeRealm { realm ->
        realm.copyToRealm(subject.toDbSubject())
    }

    suspend fun updateSubject(
        subject: RealmSubject,
    ) {
        val dbSubject = subject.toDbSubject()
        realmWrapper.writeRealm { realm ->
            val existingSubject = realm.findSubject(dbSubject.subjectId)
            if (existingSubject != null) {
                realm.copyToRealm(dbSubject, updatePolicy = UpdatePolicy.ALL)
            } else {
                realm.copyToRealm(dbSubject)
            }
        }
    }
    private fun RealmQuery<DbSubject>.buildRealmQueryForSubject(query: RealmSubjectQuery): RealmQuery<DbSubject> {
        var realmQuery = this

        if (query.projectId != null) {
            realmQuery = realmQuery.query("$PROJECT_ID_FIELD == $0", query.projectId)
        }
        if (query.subjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD == $0",
                RealmUUID.from(query.subjectId),
            )
        }
        if (query.subjectIds != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD IN $0",
                query.subjectIds.map { RealmUUID.from(it) },
            )
        }
        if (query.attendantId != null) {
            realmQuery = realmQuery.query("$USER_ID_FIELD == $0", query.attendantId)
        }
        if (query.moduleId != null) {
            realmQuery = realmQuery.query("$MODULE_ID_FIELD == $0", query.moduleId)
        }
        if (query.fingerprintSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY ${FINGERPRINT_SAMPLES_FIELD}.${FORMAT_FIELD} == $0",
                query.fingerprintSampleFormat,
            )
        }
        if (query.faceSampleFormat != null) {
            realmQuery = realmQuery.query(
                "ANY ${FACE_SAMPLES_FIELD}.${FORMAT_FIELD} == $0",
                query.faceSampleFormat,
            )
        }
        if (query.afterSubjectId != null) {
            realmQuery = realmQuery.query(
                "$SUBJECT_ID_FIELD >= $0",
                RealmUUID.from(query.afterSubjectId),
            )
        }
        if (query.hasUntokenizedFields != null) {
            realmQuery = realmQuery.query(
                "$IS_ATTENDANT_ID_TOKENIZED_FIELD == $0 OR $IS_MODULE_ID_TOKENIZED_FIELD == $1",
                false,
                false,
            )
        }
        if (query.sort) {
            realmQuery = realmQuery.sort(SUBJECT_ID_FIELD, Sort.ASCENDING)
        }
        return realmQuery
    }
}