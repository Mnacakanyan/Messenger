package com.example.messenger

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.Keys.G
import com.example.messenger.Keys.P
import com.example.messenger.Keys.privateKey
import com.example.messenger.databinding.ActivityMainBinding
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private var Adash = 0.0

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
            Thread{
                try {
                    socket?.getOutputStream()?.write(text.toString().length+Adash.toInt())
                    val bytes = ByteArray(text.toString().length)
                    for (i in bytes.indices) {
                        bytes[i] = (text.toString()[i].code+Adash.toInt()).toByte()
                    }
                    socket?.getOutputStream()?.write(bytes)
                }catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }.start()
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
                //deffie-helman
                socket = Socket(Server.ADDRESS, Server.PORT)
                socket?.getOutputStream()?.write(P.toInt())
                socket?.getOutputStream()?.write(G.toInt())
                val A = ((Math.pow(G.toDouble(), privateKey.toDouble())) % P)
                socket?.getOutputStream()?.write(A.toInt())
                val serverB = socket?.getInputStream()?.read()
                Adash = ((Math.pow(serverB?.toDouble() ?: 1.0, privateKey.toDouble())) % P)
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
                val length = socket?.getInputStream()?.read()?.minus(Adash.toInt()) ?: 0
                for (i in 1..length) {
                    str.append((socket?.getInputStream()?.read()?.minus(Adash.toInt()))?.toChar().toString())
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
