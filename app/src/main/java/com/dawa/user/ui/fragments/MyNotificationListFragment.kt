package com.dawa.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dawa.user.R
import com.dawa.user.adapters.NotificationsListAdapter
import com.dawa.user.handlers.AppExecutorsService
import com.dawa.user.handlers.PreferenceManagerService
import com.dawa.user.network.data.NotificationDTO
import com.dawa.user.network.retrofit.RetrofitClient
import com.dawa.user.ui.dialogs.MessageProgressDialog
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my_notifications.*

class MyNotificationListFragment : Fragment() {

    lateinit var progressDialog: MessageProgressDialog

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_notifications, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = MessageProgressDialog(requireActivity())

        val userData = PreferenceManagerService.retrieveUserData()
        RetrofitClient.INSTANCE.getAllNotifications(userData.id).onErrorReturn {
            it.printStackTrace()
            null
        }.doOnRequest {
            AppExecutorsService.mainThread().execute {
                progressDialog.loading()
            }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe {
            AppExecutorsService.mainThread().execute {
                progressDialog.dismiss()
                if (it.success && it.data != null) {
                    val mAdapter = NotificationsListAdapter(it.data)
                    my_notification_list.layoutManager = LinearLayoutManager(requireActivity())
                    my_notification_list.adapter = mAdapter
                    my_notification_list.swipeListener = onItemSwipeListener
                } else {
                    progressDialog.generalError()
                    AppExecutorsService.handlerDelayed({ progressDialog.dismiss() }, 1000)
                }

            }
        }


    }


    private val onItemSwipeListener = object : OnItemSwipeListener<NotificationDTO> {
        override fun onItemSwiped(position: Int,
                                  direction: OnItemSwipeListener.SwipeDirection,
                                  item: NotificationDTO): Boolean {

            Toast.makeText(requireContext(), "we deleted ${item.id}", Toast.LENGTH_SHORT).show()
            return false
        }
    }


}