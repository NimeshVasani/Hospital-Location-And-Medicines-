package com.example.runningapp.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.R
import com.example.runningapp.ui.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_diet_plan.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class DietPlanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_plan)
        var diet_item = arrayListOf<String>("Diet1", "Diet2", "Diet3", "Diet4")
        diet_list.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, diet_item)

        diet_list.onItemClickListener =
            AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val popupView = LayoutInflater.from(this).inflate(R.layout.diet_inflater, null)
                var x: TextView = popupView.findViewById(R.id.diet_detail)
                var inputStream = resources.openRawResource(R.raw.diet1)

                if (position == 0) {
                    inputStream = resources.openRawResource(R.raw.diet1)
                }
                if (position == 1) {
                    inputStream = resources.openRawResource(R.raw.diet2)

                }
                if (position == 2) {
                    inputStream = resources.openRawResource(R.raw.diet3)

                }
                if (position == 3) {
                    inputStream = resources.openRawResource(R.raw.diet4)

                }
                val byteArrayOutputStream = ByteArrayOutputStream()
                var i: Int
                try {
                    i = inputStream.read()
                    while (i != -1) {
                        byteArrayOutputStream.write(i)
                        i = inputStream.read()
                    }
                    inputStream.close()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
                x.setText(Html.fromHtml(byteArrayOutputStream.toString()))

                val popupWindow = PopupWindow(
                    popupView,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                popupWindow.isFocusable = true
                popupWindow.showAsDropDown(popupView, 0, 0)

            }

    }
}