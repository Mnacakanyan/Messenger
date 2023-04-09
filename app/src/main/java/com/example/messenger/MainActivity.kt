package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var viewBinding: ActivityMainBinding
    private val messagesList = mutableListOf<MessageType>()
    private val adapter = MessagesAdapter(messagesList)
    companion object {
        private const val TAG = "MessengerTag"
    }
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setupRecycler()
        setupListeners()
        setServer()
    }


    private fun setupRecycler() {
        with(viewBinding.messages) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        viewBinding.frameLayout.setOnClickListener {
            val text = viewBinding.message.text
            if (text.toString().isBlank()) {
                return@setOnClickListener
            }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    socket?.getOutputStream()?.write(text.toString().length)
                    socket?.getOutputStream()?.write(text.toString().encodeToByteArray())
                }catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }
        }
        messagesList.add(
            MessageType(text.toString(), Sender.User)
        )
        adapter.notifyItemInserted(messagesList.size)
        viewBinding.messages.smoothScrollToPosition(messagesList.size)
        text.clear()
        }
    }

    private fun setServer() {
        val thread = Thread {
            try {
                socket = Socket(Server.ADDRESS, Server.PORT)
                Log.d(TAG, "Connected $socket")

                getMessage()
            }catch (e: Exception) {
                Log.d(TAG, "Error $e")
            }
        }
        thread.isDaemon = true
        thread.start()
    }

    private fun getMessage() {
        val str = StringBuilder()
            while (true) {
                val length = socket?.getInputStream()?.read() ?: 0
                for (i in 1..length) {
                    str.append(socket?.getInputStream()?.read()?.toChar().toString())
                }
                messagesList.add(
                    MessageType(str.toString(), Sender.Server)
                )
                runOnUiThread {
                    adapter.notifyItemInserted(messagesList.size)
                    viewBinding.messages.smoothScrollToPosition(messagesList.size)
                }
                str.clear()
            }
    }
}
