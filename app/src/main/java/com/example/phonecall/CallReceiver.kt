package com.example.phonecall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import java.util.*


class CallReceiver : BroadcastReceiver() {

    companion object {
        var lastState = TelephonyManager.CALL_STATE_IDLE;
        var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        //Log.w("intent " , intent.getAction().toString());
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras!!.getString("android.intent.extra.PHONE_NUMBER")

        } else {
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }

            onCallStateChanged(context, state, number!!)
        }
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
               isIncoming = true
                callStartTime = Date()
                savedNumber = number

                Toast.makeText(context, "Incoming Call Ringing", Toast.LENGTH_SHORT).show()
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    Toast.makeText(context, "Outgoing Call Started", Toast.LENGTH_SHORT).show()
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    Toast.makeText(
                        context, "Ringing but no pickup" + savedNumber + " Call time " + callStartTime + " Date " + Date(),
                        Toast.LENGTH_SHORT).show()
                } else if (isIncoming) {
                    Toast.makeText(context, "Incoming $savedNumber Call time $callStartTime", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "outgoing " + savedNumber + " Call time " + callStartTime + " Date " + Date(),
                        Toast.LENGTH_SHORT).show()
                }
        }
        lastState = state
    }
}