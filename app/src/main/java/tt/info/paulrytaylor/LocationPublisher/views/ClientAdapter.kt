package tt.info.paulrytaylor.LocationPublisher.views

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tt.info.paulrytaylor.LocationPublisher.R
import tt.info.paulrytaylor.LocationPublisher.models.ClientModel

class ClientAdapter(private val clientAdapterInterface: ClientAdapterInterface) : RecyclerView.Adapter<ClientAdapter.ViewHolder>() {
    private val clients: MutableList<ClientModel> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clientTextView: TextView = itemView.findViewById(R.id.clientTextView)
        val clientButton: Button = itemView.findViewById(R.id.clientButton)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.client_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val client = clients[position]
        holder.clientTextView.text = "${client.student_id}\nmin: ${client.min}\nmax: ${client.max}"
        holder.clientButton.setOnClickListener {
            clientAdapterInterface.selectStudentID(client.student_id)
        }
    }
    override fun getItemCount(): Int {
        return clients.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateClients(map: HashMap<String,ClientModel>) {
        Log.v("Subscriber","Updating Recycler, ${map.size}")
        clients.clear()
        clients.addAll( map.values.toMutableList() )
        notifyItemRangeInserted(0, clients.size)
    }
}
