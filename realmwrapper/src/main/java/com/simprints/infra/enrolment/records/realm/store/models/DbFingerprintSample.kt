package com.simprints.infra.enrolment.records.realm.store.models

import androidx.annotation.Keep
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
internal class DbFingerprintSample : RealmObject {
    @PrimaryKey
    var id: String = ""
    var referenceId = ""
    var fingerIdentifier: Int = -1
    var template: ByteArray = byteArrayOf()
    var format: String = ""
}
