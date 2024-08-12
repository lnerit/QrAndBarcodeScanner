package com.example.barcodescanner.feature.tabs.session

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodescanner.R

class ButtonAdapter(private val buttonTexts: List<String>, private val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_session, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val buttonText = buttonTexts[position]
        holder.button.text = buttonText
        holder.button.setOnClickListener {
            onClickListener(buttonText)
        }
    }

    override fun getItemCount(): Int = buttonTexts.size

    class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.buttonOk)
    }
}