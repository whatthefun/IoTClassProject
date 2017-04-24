package com.example.user.iotclassproject.data;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.user.iotclassproject.R;
import java.util.List;

/**
 * Created by USER on 2017/04/24.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.myViewHolder> {

    private List<BluetoothDevice> mDataList;

    public MyAdapter(List<BluetoothDevice> dataList){
        this.mDataList = dataList;
    }

    @Override public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent,false);
        myViewHolder viewHolder = new myViewHolder(view);
        return viewHolder;
    }

    @Override public void onBindViewHolder(myViewHolder holder, int position) {
        holder.txtDevice.setText(mDataList.get(position).getName());
    }

    @Override public int getItemCount() {
        return mDataList.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder{

        public TextView txtDevice;

        public myViewHolder(View itemView) {
            super(itemView);
            txtDevice = (TextView) itemView.findViewById(R.id.txtDevice);
        }
    }
}
