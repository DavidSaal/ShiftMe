package com.example.shiftme.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shiftme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

class UserViewHolder extends RecyclerView.ViewHolder {
    TextView firstname, lastname, shift;
    ImageView deleteUser;
    ImageView editUser;

    UserViewHolder(final View itemView) {
        super(itemView);
        firstname = (TextView)itemView.findViewById(R.id.user_firstname);
        lastname = (TextView)itemView.findViewById(R.id.user_lastname);
        shift = (TextView)itemView.findViewById(R.id.user_shift);
        deleteUser = (ImageView)itemView.findViewById(R.id.delete_user);
        editUser = (ImageView)itemView.findViewById(R.id.edit_user);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        assert currentUser != null;
        databaseRef.child("Users").child(currentUser.getUid()).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(Objects.equals(snapshot.getValue(), "Worker")){
                    deleteUser.setVisibility(View.GONE);
                    editUser.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
