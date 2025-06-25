package com.simprints.infra.enrolment.records.realm.store.config

data class LocalDbKey(
    val projectId: String,
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalDbKey) return false
        return value.contentEquals(other.value) && projectId == other.projectId
    }

    override fun hashCode() = value.contentHashCode() + projectId.hashCode()
}