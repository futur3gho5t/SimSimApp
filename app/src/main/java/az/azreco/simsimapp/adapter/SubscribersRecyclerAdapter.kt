package az.azreco.simsimapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.azreco.simsimapp.R
import az.azreco.simsimapp.model.SubscribeModel
import kotlinx.android.synthetic.main.subscribe_item_list.view.*
import android.view.animation.AnimationUtils


class SubscribersRecyclerAdapter :
    RecyclerView.Adapter<SubscribersRecyclerAdapter.SubscribersViewHolder>() {

    private val lastPosition = -1
    private lateinit var mSubscribersList: List<SubscribeModel>
    private lateinit var mContext: Context

    inner class SubscribersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun setData(list: List<SubscribeModel>) {
        mSubscribersList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribersViewHolder {
        mContext = parent.context
        return SubscribersViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.subscribe_item_list,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: SubscribersViewHolder, position: Int) {
        val model: SubscribeModel = mSubscribersList[position]
        holder.itemView.apply {
            ivIconRecycler.setImageResource(mSubscribersList[position].icon)
            tvTitleRecycler.text = mSubscribersList[position].appName
            tvDescRecycler.text = mSubscribersList[position].desc
            if (holder.adapterPosition > lastPosition) {
                this.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.from_x_to_x))
            }
            setOnClickListener {
                onItemClickListener?.let { it(model) }
            }
        }
    }

    override fun getItemCount(): Int {
        return mSubscribersList.size
    }

    private var onItemClickListener: ((SubscribeModel) -> Unit)? = null

    // обьявляем эту функцию в фрагменте или активити где используется наш адаптер
    fun setOnItemClickListener(listener: (SubscribeModel) -> Unit) {
        onItemClickListener = listener
    }
}