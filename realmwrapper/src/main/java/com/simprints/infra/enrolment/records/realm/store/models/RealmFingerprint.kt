package com.simprints.infra.enrolment.records.realm.store.models

data class RealmFingerprint(
    val id: String = "",
    val referenceId: String = "",
    val fingerIdentifier: Int = -1,
    val template: ByteArray = byteArrayOf(),
    val format: String = "",
)
internal fun RealmFingerprint.toDbFingerprintSample(): DbFingerprintSample = DbFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = fingerIdentifier
    sample.template = template
    sample.format = format
}
internal  fun DbFingerprintSample.toRealmFingerprint(): RealmFingerprint = RealmFingerprint(
    id = id,
    referenceId = referenceId,
    fingerIdentifier = fingerIdentifier,
    template = template,
    format = format,
)