package com.example.shiftme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.example.shiftme.dataTypes.User;
import com.example.shiftme.R;

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder>{

    private Context context;
    private ArrayList<User> listUsers;

    public UserAdapter(Context context, ArrayList<User> listUsers) {
        this.context = context;
        this.listUsers = listUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        final User user = listUsers.get(position);
        holder.firstname.setText(user.getFirstName());
        holder.lastname.setText(user.getLastName());
        holder.shift.setText(user.getGroup());
    }


    @Override
    public int getItemCount() {
        return listUsers.size();
    }
}