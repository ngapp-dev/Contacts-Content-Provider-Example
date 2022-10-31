package com.ngapp.contentprovidercontacts.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngapp.contentprovidercontacts.R
import com.ngapp.contentprovidercontacts.data.ContactRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContactDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = ContactRepository(application)
    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)
    private val deleteSuccessEventChannel = Channel<Unit>(Channel.BUFFERED)

    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()

    val deleteSuccessFlow: Flow<Unit>
        get() = deleteSuccessEventChannel.receiveAsFlow()

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteContact(id)
            }.onSuccess {
                deleteSuccessEventChannel.send(Unit)
            }.onFailure { t ->
                toastEventChannel.send(R.string.delete_error)
                Log.e("CourseDetailViewModel", "Course delete error", t)
            }
        }
    }


}