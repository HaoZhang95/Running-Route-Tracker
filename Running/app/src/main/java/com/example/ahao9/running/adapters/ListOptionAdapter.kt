package com.example.ahao9.running.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.ahao9.running.R
import com.example.ahao9.running.activities.OptionBean

/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 0:09 2018/9/26
 * @ Description：Build for Metropolia project
 */
class ListOptionAdapter(var context:Context, var data: List<OptionBean>): BaseAdapter() {

    companion object {
        class ViewHolder {
            lateinit var title: TextView
            lateinit var content: TextView
            lateinit var icon: ImageView
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        lateinit var viewHolder: ViewHolder
        lateinit var retView: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            retView = LayoutInflater.from(context).inflate(R.layout.option_item, null)
            viewHolder.title = retView.findViewById(R.id.tv_item_title) as TextView
            viewHolder.content = retView.findViewById(R.id.tv_item_content) as TextView
            viewHolder.icon = retView.findViewById(R.id.iv_item_icon) as ImageView
            retView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            retView = convertView
        }
        val bean = getItem(position) as OptionBean
        viewHolder.title.text = bean.title
        viewHolder.title.setTextColor(bean.title_color)
        viewHolder.content.text = bean.content
        viewHolder.icon.setImageResource(bean.icon_url)
        return retView
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }
}