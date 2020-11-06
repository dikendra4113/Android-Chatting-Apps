package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chattingapp.model.MessageAdapter;
import com.example.chattingapp.model.MessageModel;
import com.example.chattingapp.model.PaperDb;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import io.paperdb.Paper;

public class FriendListActivity extends AppCompatActivity {
    private  String username, email,image,uid;
    private DatabaseReference RootRef,seen;
    private RecyclerView recyclerView;
    private ImageView sendBtn;
    private EditText messageBox;
    private MessageAdapter adapter;
    private UUID uuid;
    private String saveCurrentDate, saveCurrentTime,timestamp;
    private  String last;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }else{

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        messageBox = findViewById(R.id.msgEditView);
        sendBtn = findViewById(R.id.sendBtn);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));
        RootRef = FirebaseDatabase.getInstance().getReference();
        seen = FirebaseDatabase.getInstance().getReference();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("AppStone Messanger");


        Paper.init(this);
        seen.child("lastseen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                last = snapshot.child("seen").getValue().toString();
                Paper.book().write(PaperDb.lastSeen,last);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//       User data retrive
        Intent intent = getIntent();
        username = intent.getStringExtra("name");
        uid = intent.getStringExtra("uid");
        image = intent.getStringExtra("image");
        email = intent.getStringExtra("email");
        Log.i("msg",username +":"+ email);

        FirebaseRecyclerOptions<MessageModel> options =
                new FirebaseRecyclerOptions.Builder<MessageModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("chats").orderByChild("uid"), MessageModel.class)
                        .build();
        adapter = new MessageAdapter(email,options);
        recyclerView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());
        timestamp = saveCurrentDate+"T"+saveCurrentTime+"xxxZ";
        Log.i("time++++++",timestamp);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uuid = UUID.randomUUID();
                MessageModel model = new MessageModel();
                model.setViewCode(1);
                Log.i("tpe",""+model.getViewCode());
                sendMsg();

            }
        });

    }

    private void sendMsg() {
        String msg = messageBox.getText().toString();
        HashMap<String,Object> data = new HashMap<>();
        data.put("message",msg);
        data.put("sender",email);
        data.put("time",timestamp);
        data.put("uid",""+last);

        RootRef.child("chats").child(uuid.toString()).updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FriendListActivity.this, "sent", Toast.LENGTH_SHORT).show();
                messageBox.setText("");
//                Log.i("last",last);
                int i = Integer.parseInt(last) + 1;
                last = ""+i;
                Paper.book().write(PaperDb.lastSeen,last);
                seen.child("lastseen").child("seen").setValue(last);
            }
        });


        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(Integer.parseInt(last));

    }

    @Override
    protected void onStart() {
        super.onStart();
       // last = Paper.book().read(PaperDb.lastSeen);
        adapter.startListening();
      //  recyclerView.smoothScrollToPosition(Integer.parseInt(last));

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
      //  Paper.book().write(PaperDb.lastSeen,last);
    }


}