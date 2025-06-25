package com.simprints.infra.enrolment.records.realm.store.models

data class RealmFace(
    val id: String = "",
    val referenceId: String = "",
    val template: ByteArray = byteArrayOf(),
    val format: String = "",
)

internal fun RealmFace.toDbFaceSample(): DbFaceSample = DbFaceSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.template = template
    sample.format = format
}

internal fun DbFaceSample.toRealmFace(): RealmFace = RealmFace(
    id = id,
    referenceId = referenceId,
    template = template,
    format = format,
)