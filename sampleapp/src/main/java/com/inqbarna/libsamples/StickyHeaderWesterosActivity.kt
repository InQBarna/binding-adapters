package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inqbarna.adapters.*

/**
 * Created by Ricard Aparicio on 2019-08-02.
 * ricard.aparicio@inqbarna.com
 */

class StickyHeaderWesterosActivity : ListBaseActivity<House>() {

    private val houses = generatePopulation()

    private val stickyHeaderProvider = StickyHeaderProvider(houses, BR.model) {
        return@StickyHeaderProvider this is House.Name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Westeros"
    }

    override fun setupRecycler(recycler: RecyclerView) {
        recycler.run {
            addStickyHeader(stickyHeaderProvider)
            layoutManager = LinearLayoutManager(recycler.context)
        }
    }

    override fun createAdapter(): BasicBindingAdapter<House> {
        return BasicBindingAdapter<House>(ItemBinder { variableBinding, pos, dataAtPos ->
            variableBinding.bindValue(BR.model, dataAtPos)
        }).apply {
            setItems(generatePopulation())
        }
    }

    private fun generatePopulation(): ArrayList<House> {
        val houses = ArrayList<House>()

        with(houses) {
            add(House.Name("Stark"))
            add(House.Member("Sansa"))
            add(House.Member("Bran"))
            add(House.Member("Arya"))
            add(House.Member("Eddard"))

            add(House.Name("Lannister"))
            add(House.Member("Tyrion"))
            add(House.Member("Cersei"))
            add(House.Member("Tywin"))
            add(House.Member("Jaime"))

            add(House.Name("Targaryen"))
            add(House.Member("Daenerys"))
            add(House.Member("Rhaegar"))
            add(House.Member("Viserys"))
            add(House.Member("Aerys"))

            add(House.Name("Baratheon"))
            add(House.Member("Joffrey"))
            add(House.Member("Myrcella"))
            add(House.Member("Tommen"))

            add(House.Name("Tyrell"))
            add(House.Member("Margaery"))
            add(House.Member("Loras"))
            add(House.Member("Mace"))
            add(House.Member("Garlan"))
            add(House.Member("Willas"))
        }

        return houses
    }

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(
                context,
                StickyHeaderWesterosActivity::class.java
            )
        }
    }
}

sealed class House(val label: String) : TypeMarker {
    data class Name(val txt: String) : House(txt)
    data class Member(val txt: String) : House(txt)

    override fun getItemType() = R.layout.sticky_header_item
}