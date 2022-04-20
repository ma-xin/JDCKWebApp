package com.mxin.jdweb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.mxin.jdweb.ui.ql.QLLoginActivity

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val btnOne = findViewById<Button>(R.id.btn_one)

        btnOne.setOnClickListener {
            startActivity(Intent(this, WebActivity::class.java))
        }

        findViewById<Button>(R.id.btn_ql).setOnClickListener {
            startActivity(Intent(this, QLLoginActivity::class.java))
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.item_acion){
           startActivity(Intent(this, WebActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


}