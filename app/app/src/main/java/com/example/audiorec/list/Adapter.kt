package com.example.audiorec.list

import android.accessibilityservice.AccessibilityService.MagnificationController.OnMagnificationChangedListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.audiorec.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter(var records : ArrayList<AudioRecord>, var listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>()  {
    var prevIndex = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var tViewName:TextView = itemView.findViewById(R.id.tViewName)
        var tViewDate:TextView = itemView.findViewById(R.id.tViewDate)
        var tViewAudioDuration: TextView = itemView.findViewById(R.id.audioDuration)
        var btnPlayPause: ImageButton = itemView.findViewById(R.id.btnPlayPause)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){
            var audioRecord = records[position]

//            var form = SimpleDateFormat("dd.MM.yyyy в hh:mm")
//            var Date = form.format(Date(audioRecord.date))
            var Date = audioRecord.date
            holder.tViewName.text = audioRecord.name
            holder.tViewDate.text = "$Date"
            holder.tViewAudioDuration.text =  "${audioRecord.duration}".substring(0, "${audioRecord.duration}".length - 3)

        }
    }
}
/*

class Adapter(var records : ArrayList<AudioRecord>, var listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var editMode = false

    fun isEditMode(): Boolean { return editMode}
    fun setEditMode(mode: Boolean) {
        if(editMode != mode){
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var tvFilename : TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta : TextView = itemView.findViewById(R.id.tvMeta)
        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){
            var record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)

            holder.tvFilename.text = record.filename
            holder.tvMeta.text = "${record.duration } $strDate"

            if(editMode){
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = record.isChecked
            }else{
                holder.checkbox.visibility = View.GONE
                holder.checkbox.isChecked = false
            }
        }
    }
}





<androidx.recyclerview.widget.RecyclerView
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:id="@+id/RecyclerView"
            app:layout_constraintBottom_toBottomOf="@+id/linearLayout"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />
* */