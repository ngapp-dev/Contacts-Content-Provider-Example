package com.ngapp.contentprovidercontacts.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ngapp.contentprovidercontacts.data.Contact
import com.ngapp.contentprovidercontacts.databinding.ItemContactBinding

class ContactListAdapter(
    private val onItemClick: (contact: Contact) -> Unit,
    private val onItemLongClick: (contact: Contact) -> Unit
) : ListAdapter<Contact, ContactListAdapter.Holder>(ContactsDiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(
            ItemContactBinding.inflate(inflater, parent, false),
            onItemClick,
            onItemLongClick
        )
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class Holder(
        private val binding: ItemContactBinding,
        private val onItemClick: (contact: Contact) -> Unit,
        private val onItemLongClick: (contact: Contact) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            contact: Contact
        ) {
            binding.root.setOnClickListener {
                onItemClick(contact)
            }
            binding.root.setOnLongClickListener {
                onItemLongClick(contact)
                true
            }
            binding.nameTextView.text = contact.name
            binding.phoneTextView.text = contact.phones.firstOrNull() ?: contact.emails.firstOrNull()
        }

    }

    class ContactsDiffUtilCallBack : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }


}