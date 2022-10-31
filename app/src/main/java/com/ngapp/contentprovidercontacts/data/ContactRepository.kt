package com.ngapp.contentprovidercontacts.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class ContactRepository(
    private val context: Context
) {
    private val phonePattern = Pattern.compile("^\\+?[0-9]{3}-?[0-9]{6,12}\$")

    suspend fun deleteContact(contactId: Long) = withContext(Dispatchers.IO) {
        val uri: Uri =
            Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId.toString())
        context.contentResolver.delete(uri,  null)
    }

    suspend fun saveContact(contact: Contact) =
        withContext(Dispatchers.IO) {
            if (phonePattern.matcher(contact.phones[0]).matches().not() || contact.name.isBlank()) {
            }
            val contactId = saveRawContact()
            saveContactName(contactId, contact.name)
            saveContactSurname(contactId, contact.surname)
            saveContactPhone(contactId, contact.phones[0])
            if (!contact.emails[0].isNullOrEmpty()) {
                saveContactEmail(contactId, contact.emails[0])
            }
        }

    private fun saveRawContact(): Long {
        val uri = context.contentResolver.insert(
            ContactsContract.RawContacts.CONTENT_URI,
            ContentValues()
        )
        return uri?.lastPathSegment?.toLongOrNull() ?: error("cannot save raw contact")
    }

    private fun saveContactName(contactId: Long, name: String) {
        val contentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    private fun saveContactPhone(contactId: Long, phone: String) {
        val contentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    private fun saveContactSurname(contactId: Long, surname: String) {
        val contentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, surname)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    private fun saveContactEmail(contactId: Long, email: String) {
        val contentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
    }

    suspend fun getContactList(): List<Contact> = withContext(Dispatchers.IO) {
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use {
            getContactsFromCursor(it)
        }.orEmpty()
    }

    private fun getContactsFromCursor(cursor: Cursor): List<Contact> {
        if (cursor.moveToFirst().not()) return emptyList()
        val list = mutableListOf<Contact>()

        do {
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val id = cursor.getLong(idIndex)

            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME_PRIMARY)
            val name = cursor.getString(nameIndex).orEmpty()

            list.add(
                Contact(
                    id = id,
                    name = name,
                    surname = "getSurnameForContact(id)",
                    phones = getPhonesForContact(id),
                    emails = getEmailsForContact(id)
                )
            )
        } while (cursor.moveToNext())

        return list
    }

    private fun getPhonesForContact(contactId: Long): List<String> {
        return context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use {
            getPhonesFromCursor(it)
        }.orEmpty()
    }

    private fun getPhonesFromCursor(cursor: Cursor): List<String> {
        if (cursor.moveToFirst().not()) return emptyList()
        val list = mutableListOf<String>()
        do {
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val number = cursor.getString(numberIndex)
            list.add(number)

        } while (cursor.moveToNext())

        return list
    }


    private fun getEmailsForContact(contactId: Long): List<String> {
        return context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use {
            getEmailsFromCursor(it)
        }.orEmpty()
    }

    private fun getEmailsFromCursor(cursor: Cursor): List<String> {
        if (cursor.moveToFirst().not()) return emptyList()
        val list = mutableListOf<String>()
        do {
            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            val email = cursor.getString(emailIndex)
            list.add(email)

        } while (cursor.moveToNext())

        return list
    }
}