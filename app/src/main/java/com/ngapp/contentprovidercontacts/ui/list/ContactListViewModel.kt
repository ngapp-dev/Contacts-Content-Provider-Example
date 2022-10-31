package com.ngapp.contentprovidercontacts.ui.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngapp.contentprovidercontacts.data.ContactRepository
import kotlinx.coroutines.launch
import com.ngapp.contentprovidercontacts.data.Contact
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ContactListViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = ContactRepository(application)
    private val courseMutableStateFlow = MutableStateFlow<List<Contact>?>(null)
    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)

    val contactsFlow: Flow<List<Contact>?>
        get() = courseMutableStateFlow.asStateFlow()

    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()

    fun loadList() {
        viewModelScope.launch {
            runCatching {
                repository.getContactList()
            }.onSuccess {
                courseMutableStateFlow.value = it
            }.onFailure { t ->
                courseMutableStateFlow.value = null
                Log.e("ContactListViewModel", "Contact load list error", t)
            }
        }
    }
}