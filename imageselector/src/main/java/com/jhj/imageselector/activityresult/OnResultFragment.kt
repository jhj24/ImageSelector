package com.zgdj.qualitycontrol.utils.activityresult

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.os.Bundle


class OnResultFragment : Fragment() {
    private var body: (Intent?) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun startActivityForResult(mTargetActivity: Class<out Activity>, bundle: Bundle, body: (Intent?) -> Unit) {
        this.body = body
        val intent = Intent(activity, mTargetActivity)
        intent.putExtras(bundle)
        startActivityForResult(intent, ACTIVITY_CODE)
    }

    fun startActivityForResult(intent: Intent, body: (Intent?) -> Unit) {
        this.body = body
        startActivityForResult(intent, ACTIVITY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            body(data)
        }
    }


    companion object {

        internal var ACTIVITY_CODE = 1
    }
}
