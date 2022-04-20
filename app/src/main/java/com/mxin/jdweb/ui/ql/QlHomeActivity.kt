package com.mxin.jdweb.ui.ql

import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mxin.jdweb.R

class QlHomeActivity: AppCompatActivity() {

    private lateinit var viewPager:ViewPager2
    private lateinit var navigationView: BottomNavigationView
    private val mAdapter by lazy { HomeAdapter(this) }
    private val mHomeModel by lazy { ViewModelProvider(this).get(HomeModel::class.java) }
    private var addEnvMenuState:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ql_home)
        initView()
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "青龙面板"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = mAdapter
        viewPager.isUserInputEnabled = false

        navigationView = findViewById(R.id.navigationView)
        navigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item_envs-> {
                    viewPager.setCurrentItem(0,false)
                    addEnvMenuState = true
                }
                R.id.item_me-> {
                    viewPager.setCurrentItem(1,false)
                    addEnvMenuState = false
                }
            }
            invalidateOptionsMenu()
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ql_home, menu)
        val itemAddEnvMenu = menu?.findItem(R.id.item_addEnv)
        itemAddEnvMenu?.isVisible = addEnvMenuState
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_addEnv ->{
                mHomeModel.clickAddEnv()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

class HomeAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> EnvsFragment()
            1-> MeFragment()
            else-> Fragment()
        }

    }


}


class HomeModel : ViewModel(){

    private val _addEnvLiveData = MutableLiveData(0)

    val addEnvLiveData: LiveData<Int>
    get() = _addEnvLiveData

    fun clickAddEnv(){
        _addEnvLiveData.postValue((_addEnvLiveData.value?:0)+1)
    }

}
