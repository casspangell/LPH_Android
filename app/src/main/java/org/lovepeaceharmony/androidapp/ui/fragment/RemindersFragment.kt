package org.lovepeaceharmony.androidapp.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.ReminderAdapter
import org.lovepeaceharmony.androidapp.model.AlarmModel
import org.lovepeaceharmony.androidapp.ui.activity.AddReminderActivity
import org.lovepeaceharmony.androidapp.utility.Constants

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [RemindersFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 09/11/17.
 */
class RemindersFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, ReminderAdapter.OnRemindersCallBack {

    private var listContainer: View? = null
    private var remindersAdapter: ReminderAdapter? = null
    private var tvNoDataFound: TextView? = null

    private val mReminderBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            restartLoader()

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reminders, container, false)
        initView(view)
        return view
    }


    private fun initView(view: View) {
        val fabButton = view.findViewById<FloatingActionButton>(R.id.fab)
        fabButton.visibility = View.VISIBLE
        fabButton.setOnClickListener {
            val intent = Intent(context, AddReminderActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_EDIT, false)
            startActivityForResult(intent, Constants.REQUEST_CODE_REMINDERS)
        }

        listContainer = view.findViewById(R.id.ll_list_container)
        tvNoDataFound = view.findViewById(R.id.tv_no_data_found)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        remindersAdapter = ReminderAdapter(context!!, null, this)
        recyclerView.adapter = remindersAdapter

        recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fabButton.isShown)
                    fabButton.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fabButton.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        loaderManager.initLoader(Constants.URL_REMINDERS_LOADER, arguments, this)
    }

    fun restartLoader() {
        loaderManager.restartLoader(Constants.URL_REMINDERS_LOADER, null, this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null)
            activity!!.registerReceiver(mReminderBroadCast, IntentFilter(Constants.BROADCAST_REMINDERS))
    }

    override fun onDetach() {
        super.onDetach()
        if (activity != null)
            activity!!.unregisterReceiver(mReminderBroadCast)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return AlarmModel.getCursorLoader(context!!)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_REMINDERS_LOADER) {
            if (cursor != null) {
                Log.d("Cursor Count : ", cursor.count.toString() + " Test")
                if (cursor.count > 0) {
                    listContainer!!.visibility = View.VISIBLE
                    tvNoDataFound!!.visibility = View.GONE
                } else {
                    listContainer!!.visibility = View.GONE
                    tvNoDataFound!!.visibility = View.VISIBLE
                }
                remindersAdapter!!.swapCursor(cursor)
                remindersAdapter!!.notifyDataSetChanged()
            }
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        remindersAdapter!!.swapCursor(null)
    }

    override fun onRefresh() {
        restartLoader()
    }

    override fun onItemClick(_id: Int) {
        val intent = Intent(context, AddReminderActivity::class.java)
        intent.putExtra(Constants.BUNDLE_IS_FROM_EDIT, true)
        intent.putExtra(Constants.BUNDLE_ALARM_ID, _id)
        startActivityForResult(intent, Constants.REQUEST_CODE_REMINDERS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult: ", "Coming to onActivityResult 1")
        if (resultCode == RESULT_OK && requestCode == Constants.REQUEST_CODE_REMINDERS) {
            Log.d("onActivityResult: ", "Coming to onActivityResult 2")
            restartLoader()
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * //     * @param param1 Parameter 1.
         * //     * @param param2 Parameter 2.
         * @return A new instance of fragment RemindersFragment.
         */
        fun newInstance(/*String param1, String param2*/): RemindersFragment {
            /*RemindersFragment fragment = new RemindersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
            return RemindersFragment()
        }
    }
}// Required empty public constructor
