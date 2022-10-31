package com.ngapp.contentprovidercontacts.ui.add

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ngapp.contentprovidercontacts.R
import com.ngapp.contentprovidercontacts.data.Contact
import com.ngapp.contentprovidercontacts.databinding.FragmentAddContactBinding
import com.ngapp.contentprovidercontacts.utils.ViewBindingFragment
import com.ngapp.contentprovidercontacts.utils.launchAndCollectIn
import permissions.dispatcher.*
import permissions.dispatcher.ktx.constructPermissionsRequest

class ContactAddFragment :
    ViewBindingFragment<FragmentAddContactBinding>(FragmentAddContactBinding::inflate) {

    private val viewModel: ContactAddViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
    }

    private fun bindViewModel() {
        binding.saveButton.setOnClickListener { saveContactWithPermissionCheck() }
        viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        viewModel.saveSuccessFlow.launchAndCollectIn(viewLifecycleOwner) {
            findNavController().navigate(
                ContactAddFragmentDirections.actionContactAddFragmentToContactListFragment()
            )
        }
    }

    private fun saveContactWithPermissionCheck() {
        constructPermissionsRequest(
            Manifest.permission.WRITE_CONTACTS,
            onShowRationale = ::showRationaleForContacts,
            onPermissionDenied = ::onContactsDenied,
            onNeverAskAgain = ::onContactsNeverAskAgain,
            requiresPermission = ::saveContacts
        ).launch()

    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }

    @NeedsPermission(Manifest.permission.WRITE_CONTACTS)
    fun saveContacts() {
        val name = if (binding.nameEditText.text.isEmpty()) {
            binding.nameEditText.error = getString(R.string.addNameError)
            return
        } else {
            binding.nameEditText.text.toString()
        }
        val surname = if (binding.surnameEditText.text.isEmpty()) {
            binding.surnameEditText.error = getString(R.string.addSurnameError)
            return
        } else {
            binding.surnameEditText.text.toString()
        }
        val phone = if (binding.phoneEditText.text.isEmpty()) {
            binding.phoneEditText.error = getString(R.string.addPhoneError)
            return
        } else {
            binding.phoneEditText.text.toString()
        }
        val email = binding.emailEditText.text?.toString().orEmpty()
        viewModel.saveContact(Contact(0, name, surname, listOf(phone), listOf(email)))
    }

    @OnShowRationale(Manifest.permission.WRITE_CONTACTS)
    fun showRationaleForContacts(request: PermissionRequest) {
        showRationaleDialog(R.string.permission_contacts_rationale, request)
    }

    @OnPermissionDenied(Manifest.permission.WRITE_CONTACTS)
    fun onContactsDenied() {
        Toast.makeText(context, R.string.permission_contacts_denied, Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_CONTACTS)
    fun onContactsNeverAskAgain() {
        Toast.makeText(context, R.string.permission_contacts_never_ask_again, Toast.LENGTH_SHORT)
            .show()
    }
}