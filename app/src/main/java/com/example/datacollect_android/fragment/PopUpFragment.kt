package com.example.datacollect_android.fragment

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.datacollect_android.R
import com.example.datacollect_android.activity.Tutorial3Activity
import com.example.datacollect_android.activity.UserMainActivity
import kotlinx.android.synthetic.main.fragment_pop_up.*
import kotlin.properties.Delegates

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
    lateinit var appOps: AppOpsManager
    var mode by Delegates.notNull<Int>()
    lateinit var activity: Activity
//    val activity_for_method =  context as Tutorial3Activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        appOps = activity!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // remove dialog title
        // remove dialog background


        val rootView = inflater.inflate(R.layout.fragment_pop_up, container, false)
        val button_permit = rootView.findViewById<Button>(R.id.button_permit)
        if (ifPermitted()){
            // 권한이 있을 경우 다음 버튼
            button_permit.text = "다음"
        }
        button_permit.setOnClickListener {
            if (button_permit.text == "다음" ) {
                val intent = Intent(context, UserMainActivity::class.java)
                startActivity(intent)
                activity!!.finish()
            }else getPermission()
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (ifPermitted()){
            // 권한이 있을 경우 다음 버튼
            button_permit.text = "다음"
        }
    }

    override fun onPause() {
        super.onPause()
        if (ifPermitted()){
            // 권한이 없을 경우 권한 요구 페이지 이동
            button_permit.text = "다음"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setTitle("권한요청")
    }


    fun ifPermitted(): Boolean{
        mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), activity!!.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (activity!!.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
            Log.d("frafraif",granted.toString() + "1")
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED)
            Log.d("frafraif",granted.toString() + "2")
        }
        return granted
    }

    private fun getPermission() {
        // 권한이 없을 경우 권한 요구 페이지 이동
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}