package com.abduqodirov.invitex.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.abduqodirov.invitex.R
import com.abduqodirov.invitex.adapters.MezbonRecyclerAdapter
import com.abduqodirov.invitex.database.MehmonDatabase
import com.abduqodirov.invitex.databinding.FragmentDashboardBinding
import com.abduqodirov.invitex.firestore.CloudFirestoreRepo
import com.abduqodirov.invitex.viewmodel.ListViewModelFactory
import com.abduqodirov.invitex.viewmodel.SingleListViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CloudFirestoreRepo.isFirestoreConnected()) {
            this.findNavController()
                .navigate(DashboardFragmentDirections.actionDashboardFragmentToAuthFragment())
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireNotNull(activity!!.application)

        val dataSource = MehmonDatabase.getInstance(application).mehmonDatabaseDao

        val viewModelFactory =
            ListViewModelFactory(dataSource = dataSource, application = application)

        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(SingleListViewModel::class.java)


        val mAdapter = MezbonRecyclerAdapter()

        mezbonsRecycler.apply {
            setHasFixedSize(true)
            adapter = mAdapter
        }


        viewModel.loadMembers()

        viewModel.localFirstMembers.observe(this, Observer {
            mAdapter.submitList(it)
        })

        addNewMemberButton.setOnClickListener {
            this.findNavController().navigate(DashboardFragmentDirections.actionDashboardFragmentToShowCodeFragment())
        }

        //TODO stream filter count bilan nechta mehmon aytilgani va nechta qolganini hisoblash.

    }

}
