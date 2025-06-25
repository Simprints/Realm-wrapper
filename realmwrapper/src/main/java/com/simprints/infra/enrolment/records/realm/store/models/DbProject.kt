package com.simprints.infra.enrolment.records.realm.store.models

import androidx.annotation.Keep
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
internal class DbProject : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var creator: String = ""
    var imageBucket: String = ""
    var updatedAt: String = ""
}
