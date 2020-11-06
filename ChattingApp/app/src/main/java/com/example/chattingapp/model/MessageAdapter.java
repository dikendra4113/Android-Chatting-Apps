package com.example.chattingapp.model;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends FirebaseRecyclerAdapter<MessageModel,MessageAdapter.messageHolder> {


    private final String email;
    String temp ;
    List<String> arrayList = new ArrayList<>();

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     * @param email
     * @param options
     */
    public MessageAdapter(String email, @NonNull FirebaseRecyclerOptions<MessageModel> options) {

        super(options);
        this.email = email;
    }

    @Override
    protected void onBindViewHolder(@NonNull messageHolder holder, int position, @NonNull MessageModel model) {


        holder.time.setText(model.getTime());
        holder.message.setText(model.getMessage());

        temp = String.valueOf(arrayList.add(model.getSender()));
    }

    @NonNull
    @Override
    public messageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view =view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recieve_msg,parent,false);
        for (String s : arrayList){
            if(s.equals(email)){
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_msg,parent,false);

            }else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recieve_msg,parent,false);
            }

        }
        return  new messageHolder(view);

//        MessageModel model = new MessageModel();
//        String s = model.getSender();
//        Log.i("viewTyop",""+viewType);
//
//        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_msg,parent,false);
//        Log.i("user" , email +":"+mAuth.getCurrentUser().getEmail());

    }

    class messageHolder extends RecyclerView.ViewHolder
    {
        TextView time, message,you;


        public messageHolder(@NonNull View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.time);
            message = (TextView)itemView.findViewById(R.id.message);
            you = (TextView)itemView.findViewById(R.id.sendertxt);
        }
    }
}
