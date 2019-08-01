package com.example.phonecall

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


const val REQUEST_CALL = 1
const val CALL_TO_COMPANY = 2

class MainActivity : AppCompatActivity() {

    var isCalled = false
    var calledTo: String = ""

    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message?) {
            // Your logic code here.
            Log.d("T", "OK");
        }
    }
    lateinit var mMyContentObserver: MyContentObserver;
    lateinit var mMyPhoneStateListener: MyPhoneStateListener;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val ivCall = findViewById<ImageView>(R.id.ivCall)

        mMyContentObserver = object : MyContentObserver(mHandler, contentResolver) {

        }
        applicationContext.contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI, true, mMyContentObserver
        )

        ivCall.setOnClickListener {
            if (!isCalled) {
                isCalled = true
                makePhoneCall()
                calledTo = etPhoneNumber.text.toString()
            }
        }
    }

    private fun makePhoneCall() {
        val phoneNumber = etPhoneNumber.text.toString()
        if (phoneNumber.trim().isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CALL_LOG
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE),
                    REQUEST_CALL
                )
            } else {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
                startActivityForResult(intent, CALL_TO_COMPANY)
            }
        } else
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall()
            } else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()

        }
    }

/*    override fun onResume() {
        super.onResume()
        if (mMyContentObserver.number.isNotEmpty()) {
            if (calledTo == mMyContentObserver.number) {
                Toast.makeText(this, "Wow", Toast.LENGTH_SHORT).show()
                isCalled = false
            } else {
                Toast.makeText(this, "Number is empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Number is empty", Toast.LENGTH_SHORT).show()
        }

    }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==CALL_TO_COMPANY) {
            if (mMyContentObserver.number.isNotEmpty()) {
                if (calledTo == mMyContentObserver.number) {
                    Toast.makeText(this, "Wow", Toast.LENGTH_SHORT).show()
                    isCalled = false
                } else {
                    Toast.makeText(this, "Number is empty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Number is empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Wrong request", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.contentResolver.unregisterContentObserver(mMyContentObserver);
    }
}

open class MyContentObserver(handler: Handler, val cr: ContentResolver) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean, uri: Uri) {

        logCallLog();
    }

    var dialed: Long? = null
    var number: String = ""
    private var duration: Long? = null
    private fun logCallLog() {
        val columns = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val c: Cursor = cr.query(
            Uri.parse("content://call_log/calls"),
            columns, null, null, "Calls._ID DESC LIMIT 1"
        )!! //last record first
        while (c.moveToNext()) {
            dialed = c.getLong(c.getColumnIndex(CallLog.Calls.DATE))
            number = c.getLong(c.getColumnIndex(CallLog.Calls.NUMBER)).toString()
            duration = c.getLong(c.getColumnIndex(CallLog.Calls.DURATION))
            Log.i(
                "CallLog",
                "type: " + c.getString(4) + "Call to number: " + number + ", registered at: "
                        + Date(dialed!!).toString()
                        + duration
            )
        }
        c.close()
    }
}

class MyPhoneStateListener : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                Log.d("DEBUG", "IDLE")
                // Toast.makeText(this, "Wrong request", Toast.LENGTH_SHORT).show()
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.d("DEBUG", "OFFHOOK")
                // Toast.makeText(this, "Wrong request", Toast.LENGTH_SHORT).show()
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.d("DEBUG", "RINGING")
                // Toast.makeText(this, "Wrong request", Toast.LENGTH_SHORT).show()
            }
        }
    }

}