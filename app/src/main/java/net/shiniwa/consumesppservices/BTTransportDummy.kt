/*
 * Copyright (c) 2020 Xevo, Inc.
 * Author: swatanabe@xevo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.shiniwa.consumesppservices

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.util.*
import kotlin.concurrent.withLock

class BTTransportDummy(callback: BTTransportCallback) {
    private val TAG = BTTransportDummy::class.java.simpleName
    private val lock = java.util.concurrent.locks.ReentrantLock()
    // in Java: private static final UUID SERVER_UUID = new UUID(0x886DA01F9ABD4D9DL, 0x80C702AF85C822A8L);
    private val SERVER_UUID =
        UUID(-0x77925fe06542b263L, -0x7f38fd507a37dd58L)
    private val mAdapter = BluetoothAdapter.getDefaultAdapter()
    private val SVC_NAME = "ConsumingSPPServices"
    private var mThread: AcceptThread? = null
    private var mCallback: BTTransportCallback? = null

    init {
        mCallback = callback
    }

    fun startThread() {
        if (mThread == null) {
            mThread = AcceptThread(true)
            mThread!!.start()
        }
    }

    fun stop() {
        if (mThread != null) {
            mThread!!.setCanceled()
        }
    }

    inner class AcceptThread @SuppressLint("NewApi") @RequiresPermission(Manifest.permission.BLUETOOTH) constructor(
        secure: Boolean
    ) :
        Thread() {
        // The local server socket
        private var mSocketType: String? = null
        private var mmServerSocket: BluetoothServerSocket? = null
        private var mCanceled = false


        @RequiresPermission(Manifest.permission.BLUETOOTH)
        override fun run() {
            lock.withLock {
                val handler = Handler(Looper.getMainLooper())
                Log.d(
                    TAG, "Socket Type: " + mSocketType +
                            " BEGIN mAcceptThread" + this
                )
                name = "AcceptThread$mSocketType"
                var socket: BluetoothSocket? = null
                var listenAttempts = 0

                // Listen to the server socket if we're not connected
                while (!mCanceled) {
                    try {
                        Log.d(TAG, "SDL Bluetooth Accept thread is running.")

                        handler.post {
                            mCallback?.listened()
                        }
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket?.accept()
                    } catch (e: IOException) {
                        Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed")
                        handler.post {
                            mCallback?.failed()
                        }
                        return
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        Log.d(TAG, "Socket has been accepted")
                    }
                }
            }
            cleanup()
        }

        @Synchronized
        fun cleanup() {
            //Log.d(TAG, mState + " Socket Type " + mSocketType + " cancel ");
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                //Log.e(TAG, mState + " Socket Type " + mSocketType + " close() of server failed "+ Arrays.toString(e.getStackTrace()));
            }
        }

        @Synchronized
        fun setCanceled() {
            mCanceled = true
        }

        init {
            lock.withLock {

                //Log.d(TAG, "Creating an Accept Thread");
                var tmp: BluetoothServerSocket? = null
                mSocketType = if (secure) "Secure" else "Insecure"
                // Create a new listening server socket
                try {
                    if (secure) {
                        tmp = mAdapter.listenUsingRfcommWithServiceRecord(SVC_NAME, SERVER_UUID)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
                    //Let's try to shut down this thead
                } catch (e2: SecurityException) {
                    //Log.e(TAG, "<LIVIO> Security Exception in Accept Thread - "+e2.toString());
                    interrupt()
                }
                mmServerSocket = tmp
            }
        }
    }

    public interface BTTransportCallback {
        fun listened()
        fun failed()
    }
}