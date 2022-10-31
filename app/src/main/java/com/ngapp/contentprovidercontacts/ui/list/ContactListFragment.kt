package com.ngapp.contentprovidercontacts.ui.list

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngapp.contentprovidercontacts.R
import com.ngapp.contentprovidercontacts.databinding.FragmentListContactBinding
import com.ngapp.contentprovidercontacts.ui.list.adapter.ContactListAdapter
import com.ngapp.contentprovidercontacts.utils.ViewBindingFragment
import com.ngapp.contentprovidercontacts.utils.launchAndCollectIn
import permissions.dispatcher.*
import permissions.dispatcher.ktx.constructPermissionsRequest

@RuntimePermissions
class ContactListFragment :
    ViewBindingFragment<FragmentListContactBinding>(FragmentListContactBinding::inflate) {

    private val viewModel: ContactListViewModel by viewModels()
    private lateinit var contactListAdapter: ContactListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        bindViewModel()

        binding.addFAB.setOnClickListener {
            findNavController().navigate(ContactListFragmentDirections.actionContactListFragmentToContactAddFragment())
        }

        Handler(Looper.getMainLooper()).post {
            constructPermissionsRequest(
                Manifest.permission.READ_CONTACTS,
                onShowRationale = ::showRationaleForContacts,
                onPermissionDenied = ::onContactsDenied,
                onNeverAskAgain = ::onContactsNeverAskAgain,
                requiresPermission = ::loadContacts
            ).launch()
        }
    }


    private fun initList() {
        contactListAdapter = ContactListAdapter(
            onItemClick = { contact ->
                val action =
                    ContactListFragmentDirections.actionContactsFragmentToContactsDetailed(contact)
                findNavController().navigate(action)
            },
            onItemLongClick = { contact ->
                callToPhone(contact.phones.firstOrNull())
            }
        )
        with(binding.contactsList) {
            adapter = contactListAdapter
            setHasFixedSize(true)

            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            addItemDecoration(dividerItemDecoration)

            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
        }
    }


    private fun bindViewModel() {
        viewModel.contactsFlow.launchAndCollectIn(viewLifecycleOwner) { contacts ->
            contactListAdapter.submitList(contacts)
            binding.contactsList.scrollToPosition(0)
        }
        viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun callToPhone(phone: String?) {
        if (!phone.isNullOrEmpty()) {
            Intent(Intent.ACTION_DIAL)
                .apply { data = Uri.parse("tel:$phone") }
                .also { startActivity(it) }
        }
    }


    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun loadContacts() {
        viewModel.loadList()
    }

    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    fun showRationaleForContacts(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_contacts_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    fun onContactsDenied() {
        Toast.makeText(context, R.string.permission_contacts_denied, Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    fun onContactsNeverAskAgain() {
        Toast.makeText(context, R.string.permission_contacts_never_ask_again, Toast.LENGTH_SHORT)
            .show()
    }
}