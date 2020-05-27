package com.example.jerry.bmi_chart

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chart.*
import java.util.*


class ChartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val BMI_values = mutableListOf<Any?>()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // 從Firebase獲取資料並傳入參數
        FirebaseDatabase.getInstance().getReference("users").child(uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    BMI_values.clear()
                    for (ds in dataSnapshot.children) {
                        Log.d("FirebaseTest", "$ds size - ${dataSnapshot.childrenCount}")
                        BMI_values += ds.value
                    }

                    Log.d("BMI_values", "$BMI_values")
                    if (BMI_values.isNotEmpty()) initChart(bmi_LineChart, BMI_values)
                }
                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    // 初始化圖表屬性
    private fun initChart(chart: LineChart, BMI_values: MutableList<Any?>) {
        chart.onTouchListener = null
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setExtraOffsets(10f, 10f, 10f, 0f)
        chart.animateX(1000)

        //圖示
        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)

        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.description.text = "紅線: 體重過重(> 23) 藍線: 體重過輕(< 18.5)"
        chart.description.textSize = 12f
        chart.setDrawBorders(true)
        chart.setBorderColor(Color.parseColor("#b3b3b3"))  // 黑色

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        var xAxis: XAxis
        run {
            // X-Axis Style
            xAxis = chart.xAxis

            xAxis.isEnabled = false

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f)
        }

        var yAxis: YAxis
        run {
            // Y-Axis Style
            yAxis = chart.axisLeft
            yAxis.textSize = 14f
//            yAxis.axisMinimum = 12f
//            yAxis.axisMaximum = 38f
            yAxis.isEnabled = true
            yAxis.setLabelCount(10, true)
            yAxis.granularity = 1f
//            yAxis.isGranularityEnabled = true
            yAxis.spaceBottom = 3f
            yAxis.spaceTop = 3f

            val yLimitLine = LimitLine(18.5f, "過輕(< 18.5)")
            yLimitLine.textSize = 12f
            yLimitLine.lineWidth = .5f
            yLimitLine.lineColor = Color.BLUE
            yLimitLine.textColor = Color.BLUE

            val yLimitLine2 = LimitLine(23f, "過重(> 23)")
            yLimitLine2.textSize = 12f
            yLimitLine2.lineWidth = .5f
            yLimitLine2.lineColor = Color.RED
            yLimitLine2.textColor = Color.RED

            yAxis.addLimitLine(yLimitLine)
            yAxis.addLimitLine(yLimitLine2)

            // disable dual axis (only use LEFT axis)
            bmi_LineChart?.axisRight!!.isEnabled = false

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f)
        }

        // 設定圖表X軸為 BMI可變集合MutableList 的長度，Y軸為 50
        setLineData(BMI_values.size, BMI_values)

        Log.d("initChart", "values- $BMI_values, size- ${BMI_values.size}")
    }

    // 隨機產生顏色
    private fun randomColor(): Int {
        val random = Random()
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)
        return Color.rgb(r, g, b)
    }

    // 更改部分圖表屬性，將 BMI_values 資料繪製為圖表
    private fun setLineData(count: Int, BMI_values: MutableList<Any?>) {
        val dataSets = ArrayList<ILineDataSet>()
        val lists_BMI = ArrayList<Entry>()

        for (i in 0 until count) {
            val value = BMI_values[i]
            lists_BMI.add(Entry(i.toFloat(), value.toString().toFloat()))
        }

        val dataSet1 = LineDataSet(lists_BMI, "BMI")

        dataSet1.lineWidth = 3f
        dataSet1.circleSize = 4f
        dataSet1.valueTextSize = 11f
        dataSet1.setDrawCircleHole(false)
        dataSet1.color = randomColor()
        dataSets.add(dataSet1)

        val d = LineData(dataSets)

        bmi_LineChart?.data = d
        bmi_LineChart?.invalidate()
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
            R.id.action_chart -> true
            R.id.action_bmi -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
