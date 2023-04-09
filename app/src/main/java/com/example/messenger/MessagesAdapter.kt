package com.example.messenger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter(
    private val messagesList: MutableList<MessageType>
): RecyclerView.Adapter<MessagesAdapter.BaseViewHolder>() {

    companion object {
        private const val USER = 0
        private const val CLIENT = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return when(viewType) {
            USER -> {
                val view = layoutInflater.inflate(R.layout.user_message_layout, parent, false)
                UserViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.client_message_layout, parent, false)
                ClientViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when(messagesList[position].sender) {
            Sender.User -> {
                (holder as UserViewHolder).bind(messagesList[position].messageType)
            }
            else -> {
                (holder as ClientViewHolder).bind(messagesList[position].messageType)
            }
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(messagesList[position].sender) {
            Sender.User -> USER
            else -> CLIENT
        }
    }


    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(text: String)
    }

    private class UserViewHolder(itemView: View) : BaseViewHolder(itemView){
        private val messageView = itemView.findViewById<TextView>(R.id.message)

        override fun bind(text: String) {
            messageView.text = text
        }
    }

    private class ClientViewHolder(itemView: View) : BaseViewHolder(itemView){
        private val messageView = itemView.findViewById<TextView>(R.id.message)

        override fun bind(text: String) {
            messageView.text = text
        }
    }
}