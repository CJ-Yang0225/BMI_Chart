package com.example.jerry.bmi_chart

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 判斷註冊是否成功
        bt_signup.setOnClickListener {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(ed_email.text.toString(),
                    ed_password.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    AlertDialog.Builder(this)
                        .setTitle("Sign Up")
                        .setMessage("Account created")
                        .setPositiveButton("OK") { dialog, which ->
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Sign Up")
                        .setMessage(it.exception?.message)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
