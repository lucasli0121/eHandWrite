package com.handwrite

import android.Manifest
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.handwrite.databinding.ActivityMainBinding
import com.handwrite.fragment.*
import com.handwrite.utils.PromptMessage
import com.libwriting.utils.PermissionUtil
import com.updatelibrary.UpdateMgr
import com.ycl.tabview.library.TabViewChild
import java.util.*


class MainActivity : AppCompatActivity() {
    val TAG : String = "MainActivity"
    lateinit var binding: ActivityMainBinding
    var hasInit: Boolean = false
    var tabPos: Int = 0
    val lstTabViews: MutableList<TabViewChild> = mutableListOf()
    var exerciseFrag: BaseFragment = ExerciseFragment()
    var sampleFragment: BaseFragment = TkSampleFragment()
    var recordFragment: BaseFragment = RecordFragment()
    var blueProxy: BluetoothProfile? = null
    var blueFlag = -1
    val blueProxyListener = object: BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                blueProxy = proxy
                proxy?.connectedDevices?.forEach{
                    Log.i(TAG, "name=${it.name}, address=${it.address}")
                }
            }

        }

        override fun onServiceDisconnected(profile: Int) {
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //initBluetooth()
        //openBluetoothListen()
        allowPermisions()
    }

    private fun allowPermisions() {
        var permissions = ArrayList<String>()
        if (!PermissionUtil.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissions.size > 0) {
            val params = arrayOfNulls<String>(permissions.size)
            permissions.toArray(params)
            PermissionUtil.showPermissionGroupDialog(this, params)
        } else {
            initTabs()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                PromptMessage.displayToast(this, "请允许必要的权限")
                finish()
            }
        }
        initTabs()
    }

    private fun initTabs() {
        var app = this.application as MainApp
        UpdateMgr(this).apply{
            packName = "com.handwrite"
            serverUri = "http://116.63.178.81:9080/public/file/"
            checkListener = object: UpdateMgr.UpdateCheckListener {
                override fun getId(): String {
                    return app.teacher.id
                }
            }
        }.checkUpdate()
        if (!hasInit) {
            lstTabViews.add(TabViewChild(
                R.mipmap.select_record,
                R.mipmap.unselect_record, "整篇写", exerciseFrag))
            lstTabViews.add(TabViewChild(
                R.mipmap.sel_person,
                R.mipmap.unsel_person, "单字写", sampleFragment))
            lstTabViews.add(TabViewChild(
                R.mipmap.select_sheet,
                R.mipmap.unselect_sheet, "记录", recordFragment))
            binding.tabView.setTabViewChild(lstTabViews, supportFragmentManager)
            binding.tabView.setTabViewDefaultPosition(0)
            binding.tabView.setOnTabChildClickListener { position, _, _ ->
                if(tabPos != position) {
                    sendFragmentVisibilityChange(tabPos, false)
                    sendFragmentVisibilityChange(position, true)
                    tabPos = position
                }
            }
            hasInit = true
        }
        sendFragmentVisibilityChange(0, true)
    }

    private fun sendFragmentVisibilityChange(position: Int, visibility: Boolean) {
        try {
            val fragment: Fragment? = getFragmentAt(position)
            fragment?.let {
                if (fragment is BaseFragment) {
                    fragment.onVisibilityChanged(visibility)
                }
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Fragment not anymore managed")
        }
    }
    private fun getFragmentAt(position: Int): Fragment? {
        if(lstTabViews.size > 0) {
            return lstTabViews[position].getmFragment()
        }
        return null
    }

    override fun onPostResume() {
        super.onPostResume()
        sendFragmentVisibilityChange(tabPos, true)
    }
//
//    private fun openBluetoothListen() {
//        closeBluetoothListen()
//        var adapter = BluetoothAdapter.getDefaultAdapter() ?: return
//        blueFlag = adapter.getProfileConnectionState(BluetoothProfile.HID_DEVICE)
//        if(blueFlag == BluetoothProfile.STATE_CONNECTED) {
//            adapter.getProfileProxy(this, blueProxyListener, BluetoothProfile.HID_DEVICE)
//        }
//    }
//    private fun closeBluetoothListen() {
//        var adapter = BluetoothAdapter.getDefaultAdapter() ?: return
//        blueProxy ?: return
//        adapter.closeProfileProxy(blueFlag, blueProxy)
//    }
//    private fun initBluetooth() {
//        var blueAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        var devices = blueAdapter.bondedDevices
//        for (dev in devices) {
//            if(dev.bondState == BluetoothDevice.BOND_BONDED) {
//                Log.i(TAG, "name: ${dev.name}, address:${dev.address}, state:${dev.bondState}")
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        //closeBluetoothListen()
    }

}