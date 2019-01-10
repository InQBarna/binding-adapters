package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.viewpager.widget.PagerAdapter
import androidx.appcompat.app.AppCompatActivity
import com.inqbarna.adapters.TypeMarker
import com.inqbarna.libsamples.databinding.TestpagerMainBinding

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 04/05/2018
 */
class TestPagerAdapter : AppCompatActivity() {

    lateinit var binding: TestpagerMainBinding
    lateinit var model: PagingVM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.testpager_main)
        model = PagingVM(List(20) { "Item at pos $it" } )
        binding.model = model
    }

    class PagingVM(private val contents: List<String>) {
        val adapter: PagerAdapter by lazy { MyAdapter(contents) }
    }


    class PageVM(val displayText: String) : TypeMarker {
        override fun getItemType(): Int = R.layout.testpager_page_item
    }

    companion object {
        @JvmStatic
        fun getCallingIntent(context: Context): Intent {
            return Intent(context, TestPagerAdapter::class.java)
        }
    }
}