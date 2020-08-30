package com.example.datacollect_android.fragment

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.datacollect_android.R
import com.example.datacollect_android.activity.UserMainActivity
import kotlinx.android.synthetic.main.fragment_pop_up.*
import java.lang.Thread.sleep

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PopUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PopUpFragment : DialogFragment() {
    // TODO: Rename and change types of parameters

    var granted = false
    val appOps = activity!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), activity!!.getPackageName());

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // remove dialog title
        // remove dialog background
        val rootView = inflater.inflate(R.layout.fragment_pop_up, container, false)
        val button_permit = rootView.findViewById<Button>(R.id.button_permit)
        button_permit.setOnClickListener {
            if (button_permit.text == "다음" )
                //TODO
            else
                initUsageStats()
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun onPause() {
        super.onPause()
        sleep(1000)

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (activity!!.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED)
        }

        if (granted == true){
            // 권한이 없을 경우 권한 요구 페이지 이동
            button_permit.text = "다음"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setTitle("권한요청")
    }

    private fun initUsageStats() {
        val TAG = "usageStats"

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (activity!!.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        Log.d(TAG, "===== CheckPhoneState isRooting granted = " + granted);

        if (granted == false){
            // 권한이 없을 경우 권한 요구 페이지 이동
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }else{
            val intent = Intent(context, UserMainActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }
    }
}