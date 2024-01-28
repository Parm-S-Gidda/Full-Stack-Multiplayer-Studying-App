package com.example.shuffle_showdown.messages

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.Enumeration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class MessagesViewModel : ViewModel() {
    // live data for commands sent to this player/user once connection established
    val messagesLiveData: MutableLiveData<String> = MutableLiveData<String>()

    // live data for invites sent to this player
    val invitesLiveData: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()

    val connected: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    companion object {
        const val PORT = 8000
        const val DISCONNECTION_TIMEOUT = 120000 // 2 minutes
    }


    private val sendingQueue: BlockingQueue<String> = LinkedBlockingQueue()
    private var isHost = false
    private var serverSocketOpen = false
    private lateinit var server: ServerSocket
    private lateinit var socket: Socket

    private var listeningThread: Thread = Thread {
        connected.postValue(false)


        // connection listening loop
        while (true) {
            connected.postValue(false)
            // set up the server socket if its closed
            try {
                if (!serverSocketOpen) {
                    server = ServerSocket(8000)
                    serverSocketOpen = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Waiting for connection. This will block until another player connects to current player.
            // Will throw an exception if server.close() is called (which happens if they connect as a client to another player)
            try {
                Log.d("test", "waiting for connection...")
                socket = server.accept()
                socket.soTimeout = DISCONNECTION_TIMEOUT // no response from server in this time indicates disconnection
                isHost = true
                Log.d("test", "you are the host!")
            } catch (e: SocketException) {
                serverSocketOpen = false
                isHost = false
            }
            Log.d("test", "connection established!")

            // indicate to the UI that a connection has been made
            connected.postValue(true)

            // input listening loop
            while (true) {
                var input: String = ""
                try {
                    Log.d("test", "waiting for input...")
                    input = BufferedReader(InputStreamReader(socket.getInputStream())).readLine()
                    Log.d("test", "got input: $input")

                    // .readLine() will have NPE if other player disconnects
                    // Therefore break out of the input listening loop and head back to
                    // connection listening loop
                } catch (e: NullPointerException) {
                    Log.d("test", "connection closed.")
                    break
                } catch (e: SocketException) {
                    Log.d("test", "connection closed.")
                    break
                }

                // If invite message received, store it and close the connection, going back to listening to connections.
                // Invites are meant to be one a time thing, while proper connections should be
                // established once accepted
                val token: String = input.split("-")[0]
                if (token == "invite") {
                    socket.close()


                    val newList = invitesLiveData.value ?: ArrayList()
                    newList.add(input)
                    invitesLiveData.postValue(newList)
                    Log.d("test", "connection closed.!")
                    break

                    // continue connection and update messagesLiveData
                } else {
                    messagesLiveData.postValue(input)
                }
            }

            connected.postValue(false)
        }
    }

    // thread that blocks on empty queue, sends any messages that enter it to the connected socket
    private var sendingThread: Thread = Thread {
        while (true) {
            val msgToSend = sendingQueue.take() // blocking call
            val splitMsg = msgToSend.split("-")

            // delay the thread if specified
            val token: String = splitMsg[0]
            if (token == "delay") {
                Log.d("test", "buffering...")
                val delayTime = splitMsg[1].toLong()
                Thread.sleep(delayTime)
                Log.d("test", "done buffering!")
                continue
            }

            try {
                Log.d("test", "sending: $msgToSend")
                val outputStream = OutputStreamWriter(socket.getOutputStream())
                outputStream.write(msgToSend)
                outputStream.flush()
                Log.d("test", "sent!")

                if (token == "invite") {
                    socket.close()
                    Log.d("test", "connection closed.!")
                }

            } catch (e: IOException) {
                Log.d("test", "sending thread has been closed")
                e.printStackTrace()
            }
        }

    }

    init {
        listeningThread.start()
        sendingThread.start()
        val ip = getPrivateIpAddress()
    }

    // Connect to a player briefly and send an invite
    fun sendInvite(ipAddress: String) {
        val connectThread = Thread {
            try {
                socket = Socket(ipAddress, PORT)
                val localPrivateIP = getPrivateIpAddress()
                sendMessage("invite-$localPrivateIP\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        connectThread.start()
    }

    // Connect to another player via IP and port
    fun acceptInvite(ipAddress: String) {
        val connectThread = Thread {
            try {
                socket = Socket(ipAddress, PORT)
                socket.soTimeout = DISCONNECTION_TIMEOUT // NO responses from client this will indicate disconnection
                // tells other thread to stop waiting for connections and wait for messages instead
                server.close()

                Log.d("test", "Invite accepted! Connection established")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        connectThread.start()
    }

    // put a message to the sending queue for the sending thread to handle
    fun sendMessage(message: String) {
        sendingQueue.put(message)
    }

    // Put a message to the sending queue for the sending thread to handle with a delay.
    // Needed for when multiple messages are sent back to back and listening thread might be in the process
    // of modifying livedata so the listening thread doesn't receive the other message (kotlin sockets don't
    // store packets in a buffer automatically for some reason). Delay gives a window of time for listening thread to go back to the
    // input listening call.
    fun sendMessage(message: String, delayInMs: Long) {
        sendingQueue.put("delay-$delayInMs")
        sendingQueue.put(message)
    }


    fun disconnect() {
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteInvite(position: Int) {
        val newList = invitesLiveData.value ?: ArrayList()
        newList.removeAt(position)
        invitesLiveData.postValue(newList)
    }

    // get the private ip address of the current device
    // sources: https://www.stepstoperform.com/2023/02/retrieving-device-ip-in-kotlin-android.html and
    // CHATGPT for help with distinguishing private IP addresses
     private fun getPrivateIpAddress(): String {
        try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface: NetworkInterface = interfaces.nextElement()
                val addresses: Enumeration<java.net.InetAddress> = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address: java.net.InetAddress = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val ipAddress = address.hostAddress
                        // Check if the IP address is in a private range
                        if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith(
                                "172."
                            )
                        ) {
                            return ipAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1" // default address if unable to obtain a private IP
    }
}
