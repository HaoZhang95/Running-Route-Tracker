package com.example.ahao9.running.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.example.ahao9.running.R
import com.example.ahao9.running.database.entity.TestUpload
import com.squareup.picasso.Picasso


/**
 * @ Author     ：Hao Zhang.
 * @ Date       ：Created in 18:12 2018/10/6
 * @ Description：Build for Metropolia project
 */
class TestActivityAdapter(val mContext: Context, val uploads:  List<TestUpload>): RecyclerView.Adapter<TestActivityAdapter.ImageViewHolder>() {

    private var myListener: MyOnItemClickListener? = null

    interface MyOnItemClickListener {
        fun onItemClick(position: Int)

        fun onWhatEverClick(position: Int)

        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: MyOnItemClickListener) {
        myListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder{
        val v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return uploads.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uploadCurrent = uploads.get(position)
        holder.textViewName?.text = uploadCurrent.mName
        Picasso.with(mContext)
                .load(uploadCurrent.mImageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView)
    }

    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{
        override fun onClick(v: View?) {
            if (myListener != null) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    myListener!!.onItemClick(position)
                }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {

            menu.setHeaderTitle("Select Action")
            val doWhatever = menu.add(Menu.NONE, 1, 1, "Do whatever")
            val delete = menu.add(Menu.NONE, 2, 2, "Delete")

            doWhatever.setOnMenuItemClickListener(this)
            delete.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            if (myListener != null) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    when (item.getItemId()) {
                        1 -> {
                            myListener!!.onWhatEverClick(position)
                            return true
                        }
                        2 -> {
                            myListener!!.onDeleteClick(position)
                            return true
                        }
                    }
                }
            }
            return false
        }

        var textViewName: TextView? = null
        var imageView: ImageView? = null

        init {
            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }
    }
}