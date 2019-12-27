package com.abduqodirov.invitex.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.abduqodirov.invitex.MembersManager
import com.abduqodirov.invitex.R
import com.abduqodirov.invitex.adapters.AytilganClickListener
import com.abduqodirov.invitex.adapters.CollapseClickListener
import com.abduqodirov.invitex.adapters.HoldMenuClickListener
import com.abduqodirov.invitex.adapters.SingleListRecycleViewAdapter
import com.abduqodirov.invitex.database.MehmonDatabase
import com.abduqodirov.invitex.databinding.FragmentSingleListBinding
import com.abduqodirov.invitex.firestore.CloudFirestoreRepo
import com.abduqodirov.invitex.models.Mehmon
import com.abduqodirov.invitex.viewmodel.ListViewModelFactory
import com.abduqodirov.invitex.viewmodel.SingleListViewModel
import kotlinx.android.synthetic.main.fragment_single_list.*


private const val ARG_OBJECT = "object"

private const val DIALOG_TASK_EDIT = 0
private const val DIALOG_TASK_DELETE = 1
private const val DIALOG_TASK_MOVE = 2

class SingleListFragment : Fragment() {


    private var toifa: String = ""
    private lateinit var binding: FragmentSingleListBinding

    private lateinit var viewModel: SingleListViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_single_list,
            container, false
        )

        val application = requireNotNull(activity!!.application)

        val dataSource = MehmonDatabase.getInstance(application).mehmonDatabaseDao

        val viewModelFactory = ListViewModelFactory(
            dataSource = dataSource,
            application = application
        )

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(
                SingleListViewModel::class.java
            )



        binding.viewModel = viewModel

        val mAdapter =
            SingleListRecycleViewAdapter(
                AytilganClickListener { mehmon ->
                    viewModel.onMehmonChecked(mehmon)
                },
                HoldMenuClickListener {

                    showMainTasksDialog(it)

                },
                CollapseClickListener { member, isCollapsed ->

                    //TODO Avvalgi yozilgan collapsed va yangi kelgan eventdagi boolean bir xil bo'lmasa keyin bajaradi. Aks xolda shunchaki icon almashadi
                    if (!MembersManager.membersCollapsed.containsKey(member.ism)) {
                        viewModel.onCollapseMember(member = member, isCollapsed = isCollapsed)
                    } else if (MembersManager.membersCollapsed.getValue(member.ism) != isCollapsed) {
                        viewModel.onCollapseMember(member = member, isCollapsed = isCollapsed)
                    }

                    if (!MembersManager.membersCollapsed.containsKey(member.ism)) {
                        Log.i("naza", "Oldin yozilmagan ekan yozyabman")
                        MembersManager.membersCollapsed.put(member.ism, isCollapsed)
                        Log.i("naza", "${MembersManager.membersCollapsed[member.ism]}")
                    } else {
                        Log.i("naza", "Bor ekan yangilayabman")
                        MembersManager.membersCollapsed[member.ism] = isCollapsed
                        Log.i("naza", "${MembersManager.membersCollapsed[member.ism]}")
                    }


                }
            )

        binding.mainList.apply {
            adapter = mAdapter
            setHasFixedSize(true)
        }

        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            toifa = getString(ARG_OBJECT)
        }


//        viewModel.observeOfItsGuests(toifa)

        //TODO shuni bitta viewmodel bilan hal qilish kerak yoki undanam yaxshisi Har bir tab uchun fragment ishlatmasdan performance jihatdan yaxshiroq usul topish. Little Panda akadan so'rab ko'rish
        viewModel.setToifa(toifa = toifa)


        if (CloudFirestoreRepo.isFirestoreConnected()) {
            viewModel.loadMembers()

            viewModel.localFirstMembers.observe(this, Observer {

                for (member in it) {
                    viewModel.loadFirestoreMehmons(toifa = toifa, username = member)
                }

            })

            viewModel.toifaniBarchaMehmonlari
                .observe(this@SingleListFragment, Observer { remoteGuests ->

                    //TODO har bir list ichiga element yo'q paytda element yo'qligi haqida object. Uni recyclerViewda bitta TextViewda ko'rsatib element yo'qligini bildirish

                    if (remoteGuests[0].isNotEmpty()) {
                        list_empty_text.visibility = View.GONE
                        main_list.visibility = View.VISIBLE
                    }

                    val firestoreMehmonlar = ArrayList<Mehmon>()

                    for (guests in remoteGuests) {
                        firestoreMehmonlar.addAll(guests)
                    }


                    mAdapter.submitList(firestoreMehmonlar)
                })

        } else {
            viewModel.loadSpecificMehmons(toifa)

            viewModel.localGuests
                .observe(this@SingleListFragment, Observer { localGuests ->

                    if (localGuests.isNotEmpty()) {
                        list_empty_text.visibility = View.GONE
                        main_list.visibility = View.VISIBLE
                    }

                    mAdapter.submitList(localGuests)
                })
        }



//                main_list.smoothScrollToPosition(0)




//        viewModel.getLocalSearchResults("idam").observe(this@SingleListFragment, Observer {
//            Log.i("searchm", "${it.distinct()}")
//        })

        binding.lifecycleOwner = this

        return binding.root
    }

    private fun showMainTasksDialog(mehmon: Mehmon) {
        val builder = AlertDialog.Builder(activity!!)

        builder.setTitle(getString(R.string.select_task_with_mehmon))
        builder.setItems(R.array.tasks_with_mehmon,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DIALOG_TASK_DELETE -> viewModel.deleteMehmon(mehmon)
                    DIALOG_TASK_EDIT -> showRenameDialog(mehmon)
                }
            })

        builder.show()

    }

    private fun showRenameDialog(mehmon: Mehmon) {
        val builder = AlertDialog.Builder(activity!!)

        val editText = EditText(activity)
        editText.setText(mehmon.ism)
        editText.requestFocus()

        builder.setView(editText)
        builder.setTitle(getString(R.string.enter_new_name))

        builder.setPositiveButton(
            getString(R.string.done),
            DialogInterface.OnClickListener { dialog, which ->
                viewModel.renameMehmon(mehmon, editText.text.toString())
            })

        builder.setNegativeButton(
            getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, which ->
                //            dialog.dismiss()
            })

        builder.show()

    }

    override fun onResume() {
        super.onResume()
//        Hides keyboard if previously opened page had keyboard
        hideKeyboard(view)
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

