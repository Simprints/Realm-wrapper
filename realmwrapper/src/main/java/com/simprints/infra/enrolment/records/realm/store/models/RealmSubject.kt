package com.simprints.infra.enrolment.records.realm.store.models

import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmUUID
import java.util.Date

data class RealmSubject(
    var subjectId: String = "",
    var projectId: String = "",
    var attendantId: String = "",
    var moduleId: String = "",
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var fingerprintSamples: List<RealmFingerprint> = listOf(),
    var faceSamples: List<RealmFace> = listOf(),
    var isAttendantIdTokenized: Boolean = false,
    var isModuleIdTokenized: Boolean = false,
)


internal fun RealmSubject.toDbSubject(): DbSubject = DbSubject().also { subject ->
    subject.subjectId = RealmUUID.from(subjectId)
    subject.projectId = projectId
    subject.attendantId = attendantId
    subject.moduleId = moduleId
    subject.createdAt = createdAt?.toRealmInstant()
    subject.updatedAt = updatedAt?.toRealmInstant()
    subject.fingerprintSamples =
        fingerprintSamples.map { it.toDbFingerprintSample() }.toRealmList()
    subject.faceSamples = faceSamples.map { it.toDbFaceSample() }.toRealmList()
    subject.isModuleIdTokenized = isModuleIdTokenized
    subject.isAttendantIdTokenized = isAttendantIdTokenized
}

internal fun DbSubject.toRealmSubject(): RealmSubject = RealmSubject(
    subjectId = subjectId.toString(),
    projectId = projectId,
    attendantId = attendantId,
    moduleId = moduleId,
    createdAt = createdAt?.toDate(),
    updatedAt = updatedAt?.toDate(),
    fingerprintSamples = fingerprintSamples.map { it.toRealmFingerprint() },
    faceSamples = faceSamples.map { it.toRealmFace() },
    isAttendantIdTokenized = isAttendantIdTokenized,
    isModuleIdTokenized = isModuleIdTokenized
)