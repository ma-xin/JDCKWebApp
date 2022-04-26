package com.mxin.jdweb.ui.ql

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.network.api.SystemApi
import com.mxin.jdweb.ui.ql.me.MeFragment
import com.mxin.jdweb.utils.SpannableUtil
import kotlinx.coroutines.launch

class QlHomeActivity: AppCompatActivity() {

    private lateinit var viewPager:ViewPager2
    private lateinit var navigationView: BottomNavigationView
    private val mAdapter by lazy { HomeAdapter(this) }
    private val mHomeModel by lazy { ViewModelProvider(this).get(HomeModel::class.java) }
    private var addEnvMenuState:Boolean = true
    private val spUtil by lazy { App.getInstance().spUtil }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ql_home)
        initView()
        initData()
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "青龙面板"
        val qlVersion = spUtil.getString(SPConstants.QL_version)
        if(!TextUtils.isEmpty(qlVersion)){
            toolbar.subtitle = "青龙版本：$qlVersion"
        }
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        mHomeModel.qlVersionLiveData.observe(this){
            if(!TextUtils.isEmpty(it)){
                toolbar.subtitle = it
            }
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

    private fun initData() {
        getQLVersion()
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

    private fun getQLVersion(){
        lifecycleScope.launch {
            try{
                val resp = ServiceGenerator.createService(SystemApi::class.java).version()
                if(resp.code == 200){
                    resp.data?.version?.let {
                        val cacheQlVersion = spUtil.getString(SPConstants.QL_version)
                        if(TextUtils.isEmpty(cacheQlVersion) || cacheQlVersion == "0"){
                            spUtil.put(SPConstants.QL_version, it)
                            mHomeModel.setQLVersion(it)
                        }
                        else if(!TextUtils.isEmpty(it) && cacheQlVersion != it){
                            AlertDialog.Builder(this@QlHomeActivity)
                                .setTitle("检测到青龙版本号")
                                .setMessage("配置文件中的版本号：$cacheQlVersion, 检测系统的版本号：$it\n是否覆盖配置文件的版本号？")
                                .setPositiveButton(SpannableUtil.formatForeground("覆盖", Color.RED)){ dialog, _ ->
                                    spUtil.put(SPConstants.QL_version, it)
                                    mHomeModel.setQLVersion(it)
                                    dialog.dismiss()
                                }
                                .setNegativeButton(SpannableUtil.formatForeground("取消", Color.GRAY)){dialog, _ ->
                                    dialog.dismiss()
                                }
                        }
                        return@launch
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
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
    private val _versionLiveData = MutableLiveData("")

    val addEnvLiveData: LiveData<Int>
    get() = _addEnvLiveData

    val qlVersionLiveData:LiveData<String>
    get() = _versionLiveData

    fun clickAddEnv(){
        _addEnvLiveData.postValue((_addEnvLiveData.value?:0)+1)
    }

    fun setQLVersion(version:String){
        _versionLiveData.postValue(version)
    }

}
