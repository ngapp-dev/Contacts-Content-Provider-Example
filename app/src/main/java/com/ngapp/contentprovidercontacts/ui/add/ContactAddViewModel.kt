package com.ngapp.contentprovidercontacts.ui.add

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngapp.contentprovidercontacts.R
import com.ngapp.contentprovidercontacts.data.Contact
import com.ngapp.contentprovidercontacts.data.ContactRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ContactAddViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = ContactRepository(application)
    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)
    private val saveSuccessEventChannel = Channel<Unit>(Channel.BUFFERED)

    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()

    val saveSuccessFlow: Flow<Unit>
        get() = saveSuccessEventChannel.receiveAsFlow()

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            runCatching {
                repository.saveContact(contact)
            }.onSuccess {
                saveSuccessEventChannel.send(Unit)
            }.onFailure { t ->
                Log.e("ContactAddViewModel", "contact add error", t)
                toastEventChannel.send(R.string.save_contact_error)
            }

        }
    }
}