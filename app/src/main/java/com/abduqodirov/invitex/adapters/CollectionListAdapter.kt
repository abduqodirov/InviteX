package com.abduqodirov.invitex.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abduqodirov.invitex.fragments.SingleListFragment

private const val ARG_OBJECT = "object"

class CollectionListAdapter(fm: FragmentManager, private val toifalar: Array<String>) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val fragment = SingleListFragment()

        fragment.arguments = Bundle().apply {
            putString(ARG_OBJECT, toifalar[position])
        }

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return toifalar[position]
    }

    override fun getCount(): Int = toifalar.size
}
