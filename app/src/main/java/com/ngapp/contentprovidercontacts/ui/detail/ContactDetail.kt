package com.ngapp.contentprovidercontacts.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ngapp.contentprovidercontacts.R
import com.ngapp.contentprovidercontacts.data.Contact
import com.ngapp.contentprovidercontacts.databinding.FragmentDetailContactBinding
import com.ngapp.contentprovidercontacts.utils.launchAndCollectIn


class ContactDetail : Fragment() {
    lateinit var binding: FragmentDetailContactBinding
    private val viewModel: ContactDetailViewModel by viewModels()
    private val args: ContactDetailArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        val view = binding.root

        args.currentContact.let {
            binding.detailedContactNameTextView.text = "Name: ${it.name}"
            binding.detailedContactSurnameTextView.text = "Surname: ${it.surname}"
            val phones = "Phones: ${it.phones}"
            val emails = "Emails: ${it.emails}"
            binding.detailedContactPhonesTextView.text = phones.replace("[", "").replace("]", "")
            binding.detailedContactEmailTextView.text = emails.replace("[", "").replace("]", "")

            binding.deleteButton.setOnClickListener {
                showOnDeleteAlertDialog(R.string.on_delete_alert_dialog, args.currentContact)
            }

        }
        return view
    }

    private fun showOnDeleteAlertDialog(@StringRes messageResId: Int, currentContact: Contact) {
        AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.button_yes) { dialog, _ ->
                viewModel.delete(currentContact.id)
                viewModel.deleteSuccessFlow.launchAndCollectIn(viewLifecycleOwner) {
                    findNavController().navigate(ContactDetailDirections.actionContactDetailToContactListFragment())
                }
            }
            .setNegativeButton(R.string.button_no) { dialog, _ ->
                viewModel.toastFlow.launchAndCollectIn(viewLifecycleOwner) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setMessage(messageResId)
            .show()
    }
}