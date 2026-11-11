package com.momsafe.duallock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class LimitedHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (!dpm.isDeviceOwnerApp(packageName)) {
            finish()
            return
        }

        try {
            if (dpm.isLockTaskPermitted(packageName)) startLockTask()
        } catch (_: Exception) {
            // The app remains usable as a limited launcher; device policy still controls enforcement.
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
        }
        setContentView(layout)

        layout.addView(TextView(this).apply {
            text = "MOM SAFE"
            textSize = 28f
            gravity = Gravity.CENTER
        }, fullWidth())

        layout.addView(TextView(this).apply {
            text = "Choose an allowed app"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }, fullWidth())

        addAppButton(layout, "YouTube") {
            launchPackage("com.google.android.youtube")
        }
        addAppButton(layout, "Phone") {
            try {
                startActivity(Intent(Intent.ACTION_DIAL))
            } catch (_: Exception) {
                toast("Phone app is not available")
            }
        }
        addAppButton(layout, "WhatsApp") {
            launchPackage("com.whatsapp")
        }

        addAppButton(layout, "Owner Unlock") {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    private fun launchPackage(packageName: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch == null) {
            toast("App is not installed")
            return
        }
        try {
            startActivity(launch)
        } catch (_: Exception) {
            toast("This app is not allowed in Mom Safe mode")
        }
    }

    private fun addAppButton(
        layout: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        layout.addView(Button(this).apply {
            text = label
            textSize = 18f
            setOnClickListener { action() }
        }, fullWidth())
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    @Deprecated("Back is intentionally disabled while Mom Safe is active")
    override fun onBackPressed() {
        // Stay on the restricted home screen.
    }
}
