package com.ngapp.contentprovidercontacts.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: Long,
    val name: String,
    val surname: String,
    val emails: List<String>,
    val phones: List<String>
) : Parcelable
