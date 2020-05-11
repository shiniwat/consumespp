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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity(), BTTransportDummy.BTTransportCallback {
    private val TAG = MainActivity::class.java.simpleName
    private var numServices = 0
    private var btTransports = ArrayList<BTTransportDummy>()
    private var failedListenning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate...")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var text = findViewById<TextView>(R.id.resource_info)
        text.setText(R.string.default_msg)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val btTransport = BTTransportDummy(this)
            btTransports.add(btTransport)
            btTransport.startThread()
        }, 1000)
    }

    override fun listened() {
        if (failedListenning) {
            return;
        }
        numServices++
        var text = findViewById<TextView>(R.id.resource_info)
        text.setText("We are consuming $numServices SPP services...")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (!failedListenning) {
                val btTransport = BTTransportDummy(this)
                btTransports.add(btTransport)
                btTransport.startThread()
            }
        }, 1500)
    }

    override fun failed() {
        failedListenning = true
        Log.e(TAG, "failed listenning sockets")
        numServices--
        if (numServices >= 0) {
            var text = findViewById<TextView>(R.id.resource_info)
            text.setText("We are consuming $numServices SPP services...")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy...")
        btTransports.forEach {
            it.stop()
        }
    }
}
