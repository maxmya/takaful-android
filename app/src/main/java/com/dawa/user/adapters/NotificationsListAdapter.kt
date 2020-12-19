package com.dawa.user.adapters

import android.view.View
import android.widget.TextView
import com.dawa.user.R
import com.dawa.user.network.data.NotificationDTO
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class NotificationsListAdapter(dataSet: List<NotificationDTO> = emptyList()) :
    DragDropSwipeAdapter<NotificationDTO, NotificationsListAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val notificationTitle: TextView = itemView.findViewById(R.id.title)
        val notificationBody: TextView = itemView.findViewById(R.id.body)
        val dragger: TextView = itemView.findViewById(R.id.dragger)

    }

    override fun getViewHolder(itemView: View) = ViewHolder(itemView)

    override fun onBindViewHolder(item: NotificationDTO, viewHolder: ViewHolder, position: Int) {

        viewHolder.notificationTitle.text = item.title
        viewHolder.notificationBody.text = item.body

    }


    override fun getViewToTouchToStartDraggingItem(item: NotificationDTO,
                                                   viewHolder: ViewHolder,
                                                   position: Int): View? {
        return viewHolder.dragger;
    }
}
