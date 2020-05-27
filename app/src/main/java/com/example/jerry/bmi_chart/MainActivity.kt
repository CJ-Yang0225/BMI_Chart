package com.example.jerry.bmi_chart

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private var isBlank = true
    private lateinit var database: DatabaseReference
    private val RC_SIGNUP = 100
    var sn_Count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 監聽FirebaseAuth的狀態
        auth.addAuthStateListener { auth -> authChanged(auth) }
    }

    // 檢查帳戶是否存在
    private fun authChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            startActivityForResult(Intent(this, SignUpActivity::class.java)
                    , RC_SIGNUP)
        } else {
            Log.d("MainActivity_UID", "authChanged: ${auth.currentUser?.uid}")
            FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            sn_Count = dataSnapshot.childrenCount.toInt()
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    })
        }
    }

    // 計算BMI，回傳結果
    fun submitBMI(view: View) {
        val name = ed_name.checkBlank("名字 內容有誤")!!.toString()
        val height = ed_height.checkBlank("身高(cm) 內容有誤")!!.toFloat()
        val weight = ed_weight.checkBlank("體重(kg) 內容有誤")!!.toFloat()
        val bmi = weight / (height * height / 10000)
        val definition = when {
            (bmi >= 35) -> "重度肥胖"
            (bmi > 29) -> "中度肥胖"
            (bmi > 26) -> "輕度肥胖"
            (bmi > 23) -> "過重"
            (bmi >= 18.5f) -> "健康體位"
            (bmi < 18.5f) -> "過輕"
            (bmi == 0.0f) -> "發生未知的錯誤。"
            else -> {
                "發生未知的錯誤。"
            }
        }

        AlertDialog.Builder(this)
                .setMessage("名字： $name\nBMI： $bmi  ➔  $definition")
                .setTitle("結果")
                .show()

        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ed_weight.windowToken, 0)


        if (auth.currentUser != null && !isBlank) {
            updateData(name, bmi)
        }
    }

    // 上傳至Firebase
    private fun updateData(userName: String, userBMI: Float) {
        val uid = auth.currentUser!!.uid

        Log.d("updateData", "child size- $sn_Count}")
        database = FirebaseDatabase.getInstance().reference
        database.child("users").child(uid).child((++sn_Count).toString()).setValue(userBMI)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_bmi -> true
            R.id.action_chart -> {
                startActivity(Intent(this, ChartActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Method Extension 檢查空值
    private fun EditText.checkBlank(message: String): String? {
        val text = this.text.toString()
        if (text.isBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            return "0"
        }
        isBlank = false
        return text
    }
}
