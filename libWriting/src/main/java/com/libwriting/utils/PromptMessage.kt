package com.handwrite.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.libwriting.R

class PromptMessage(activity: Activity?, msg: String) {
    var _promptMsg = "promptMsg"
    private var _activity: Activity? = null
    val msgHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val pref: SharedPreferences? = _activity?.getPreferences(Activity.MODE_PRIVATE)
            val prompt: String? = pref?.getString(_promptMsg, "")
            _activity?.let { showMessageDlg(it, prompt, null) }
        }
    }

    companion object {
        private const val THIS_FILE = "PromptMessage"
        fun displayToast(ctx: Context, msgid: Int) {
            val toast: Toast = Toast.makeText(ctx, ctx.resources.getString(msgid), Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }

        fun displayToast(ctx: Context?, msg: String?) {
            val toast: Toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }

        fun showThreadMessage(activity: Activity, promptMsg: String, msgid: Int) {
            val msg = PromptMessage(activity, promptMsg)
            val pref: SharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE)
            val prefEdit: SharedPreferences.Editor = pref.edit()
            prefEdit.putString(promptMsg, activity.getResources().getString(msgid))
            prefEdit.commit()
            msg.msgHandler.sendEmptyMessage(1)
        }

        fun showThreadMessage(activity: Activity, promptMsg: String, msg: String?) {
            val propMsg = PromptMessage(activity, promptMsg)
            val pref: SharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE)
            val prefEdit: SharedPreferences.Editor = pref.edit()
            prefEdit.putString(promptMsg, msg)
            prefEdit.commit()
            propMsg.msgHandler.sendEmptyMessage(1)
        }

        fun showMessageDlg(ctx: Context, prompt: String?, onclicklistener: DialogInterface.OnClickListener?) {
            try {
                val dlg: Dialog = AlertDialog.Builder(ctx, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle(ctx.resources.getString(R.string.app_name))
                        .setMessage(prompt)
                        .setPositiveButton(R.string.ok, onclicklistener).create()
                dlg.show()
            } catch (e: Exception) {
                e.message?.let { Log.e(THIS_FILE, it) }
            }
        }

        fun showMessageDlg(ctx: Context, msgid: Int, onclicklistener: DialogInterface.OnClickListener?) {
            try {
                val dlg: Dialog = AlertDialog.Builder(ctx, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle(ctx.resources.getString(R.string.app_name))
                        .setMessage(ctx.resources.getString(msgid))
                        .setPositiveButton(R.string.ok, onclicklistener).create()
                dlg.show()
            } catch (e: Exception) {
                e.message?.let { Log.e(THIS_FILE, it) }
            }
        }

        fun showMessageDlgWithCancel(ctx: Context, msgid: Int, onOklistener: DialogInterface.OnClickListener?, onCancellistener: DialogInterface.OnClickListener?) {
            try {
                val dlg: Dialog = AlertDialog.Builder(ctx, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle(ctx.resources.getString(R.string.app_name))
                        .setMessage(ctx.resources.getString(msgid))
                        .setPositiveButton(R.string.ok, onOklistener)
                        .setNegativeButton(R.string.cancel, onCancellistener)
                        .setCancelable(true).create()
                dlg.show()
            } catch (e: Exception) {
                e.message?.let { Log.e(THIS_FILE, it) }
            }
        }
    }

    init {
        _activity = activity
        _promptMsg = msg
    }
}