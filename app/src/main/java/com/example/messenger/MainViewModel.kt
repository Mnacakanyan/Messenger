package com.example.messenger

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.Socket

class MainViewModel: ViewModel() {

    companion object {
        private const val TAG = "MessengerTag"
    }
    private var socket: Socket? = null
    private lateinit var bufferStream: BufferedOutputStream
    private lateinit var bw: BufferedWriter
    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private val _data = MutableLiveData(MainUiState())
    val data: LiveData<MainUiState> = _data

    init {
        setupServer()
    }

    fun send(message: String) {
        _state.update {
            it.copy(lastSendedMessage = message, sended = ++it.sended)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    socket?.getOutputStream()?.write(message.length)
                    socket?.getOutputStream()?.write(message.encodeToByteArray())
//                    Log.d(TAG, message)
//                    socket.outputStream.write(message.encodeToByteArray())
                }catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }
        }
        _data.value = MainUiState(message, _state.value.sended)

    }

    fun getMessageFlow() {
        val str = StringBuilder()
        val thread = Thread {
            while (true) {
                val length = socket?.getInputStream()?.read() ?: 0
                for (i in 1..length) {
                    str.append(socket?.getInputStream()?.read()?.toChar().toString())
                }
                _data.postValue(
                    MainUiState(
                        lastSendedMessage = str.toString(),
                        ++_state.value.sended
                    )
                )
                str.clear()
            }
        }
        thread.isDaemon = true
        thread.start()
    }

    private fun setupServer() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    socket = Socket(Server.ADDRESS, Server.PORT)
                    socket?.let {
                        it.getOutputStream().write("Hello server".length)
                        it.getOutputStream().write("Hello server".encodeToByteArray())
                    }

                    Log.d(TAG, "Connected $socket")
                    val receivedLength = socket?.getInputStream()?.read() ?: 0
                    val received = StringBuilder()
                    for (i in 1..receivedLength){
                        received.append(socket?.getInputStream()?.read()?.toChar().toString())
                    }
                    _state.update {
                        it.copy(lastSendedMessage = received.toString(), sended = ++it.sended)
                    }
                    _data.postValue(MainUiState(received.toString(), _state.value.sended))
                }catch (e: Exception) {
                    Log.d(TAG, "Error $e")
                }
            }
        }
    }


    data class MainUiState(
        val lastSendedMessage: String? = "",
        var sended: Int = 0
    )
}