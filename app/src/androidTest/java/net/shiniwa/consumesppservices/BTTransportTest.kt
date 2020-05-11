package net.shiniwa.consumesppservices

import android.os.AsyncTask
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BTTransportTest {
    private val TAG = BTTransportTest::class.java.simpleName

    inner class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Log.d(TAG, "onPostExecute")
        }
    }

    @Test
    fun testStartThread() {
        var sppResources = 0
        val btTransport = BTTransportDummy(object: BTTransportDummy.BTTransportCallback {
            override fun listened() {
                sppResources++
                Assert.assertEquals(1, sppResources)
                Log.d(TAG, "listened got called")
            }

            override fun failed() {
                Assert.assertEquals(1, sppResources)
                Log.d(TAG, "failed got called")
            }
        })

        doAsync {
            btTransport.startThread()
        }.execute()
    }
}