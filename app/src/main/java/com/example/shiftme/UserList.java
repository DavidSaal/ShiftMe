package com.example.shiftme;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shiftme.Adapter.UserAdapter;
import com.example.shiftme.dataTypes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserList extends AppCompatActivity {
    private RecyclerView userView;
    private UserAdapter userAdapter;

    private DatabaseReference databaseRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_layout);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        userView = (RecyclerView)findViewById(R.id.user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        userView.setLayoutManager(linearLayoutManager);
        userView.setHasFixedSize(true);

        final ArrayList<User> allUsers = new ArrayList<>();

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String firstname = ds.child("firstName").getValue(String.class);
                    String lastname = ds.child("lastName").getValue(String.class);
                    String email = ds.child("email").getValue(String.class);
                    String group = ds.child("group").getValue(String.class);
                    User user = new User(firstname, lastname, email, group);
                    allUsers.add(user);
                }
                if(allUsers.size() > 0){
                    userView.setVisibility(View.VISIBLE);
                    userAdapter = new UserAdapter(UserList.this, allUsers);
                    userView.setAdapter(userAdapter);

                }else {
                    userView.setVisibility(View.GONE);
                    Toast.makeText(UserList.this, "There is no users in the database.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        DatabaseReference users = databaseRef.child("Users");
        users.addValueEventListener(postListener);
    }
}
