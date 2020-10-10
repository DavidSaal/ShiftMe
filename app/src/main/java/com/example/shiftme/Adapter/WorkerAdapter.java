package com.example.shiftme.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.example.shiftme.Calendar;
import com.example.shiftme.dataTypes.User;
import com.example.shiftme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class WorkerAdapter extends RecyclerView.Adapter<UserViewHolder>{

    private Context context;
    private ArrayList<User> listUsers;
    private Query shiftQuery;
    private Query eventQuery;
    private Calendar calendar;

    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private String dateclicked;
    private Long dateclickedTime;

    public WorkerAdapter(Context context, ArrayList<User> listUsers , String dateclicked, Long dateclickedTime) {
        this.context = context;
        this.listUsers = listUsers;
        this.dateclicked = dateclicked;
        this.dateclickedTime = dateclickedTime;
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
        shiftQuery = databaseRef.child("Shifts").child(dateclicked).child(user.getId());
        eventQuery = databaseRef.child("Events").child(Long.toString(dateclickedTime));

        holder.firstname.setText(user.getFirstName());
        holder.lastname.setText(user.getLastName());
        holder.shift.setText(user.getShift());

        holder.editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskDialog(user);
            }
        });

        holder.deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shiftQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot idSnapshot: dataSnapshot.getChildren()) {
                            idSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });

                eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot idSnapshot: dataSnapshot.getChildren()) {
                            int value = idSnapshot.getValue(Integer.class);
                            Log.i("TAG", "ASDASDASDASDASDSADASDSADAS: " + 1);
                            if(value > 1){
                                idSnapshot.getRef().setValue(value - 1);
                            }
                            else{
                                idSnapshot.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });

                //refresh the activity page.
               // ((Activity)context).finish();
                //context.startActivity(((Activity) context).getIntent());
            }
        });
    }

    private void editTaskDialog(final User user){
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View subView = inflater.inflate(R.layout.add_shift_layout, null);

        final EditText firstNameField = (EditText)subView.findViewById(R.id.firstname);
        final EditText lastNameField = (EditText)subView.findViewById(R.id.lastname);
        final EditText shiftField = (EditText)subView.findViewById(R.id.shift);

        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        shiftField.setText(user.getShift());

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Worker Shift");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String name = firstNameField.getText().toString();
                final String lastname = lastNameField.getText().toString();
                final String shift = shiftField.getText().toString();

                if(checkFields(name, lastname, shift)){
                    DatabaseReference shiftsRef = databaseRef.child("Shifts").child(dateclicked).child(user.getId());
                    shiftsRef.child("firstName").setValue(name);
                    shiftsRef.child("lastName").setValue(lastname);
                    shiftsRef.child("shift").setValue(shift);
                    user.setFirstName(name);
                    user.setLastName(lastname);
                    user.setShift(shift);
                }
                else {
                    ((ViewGroup) subView.getParent()).removeView(subView);
                    firstNameField.setText(name);
                    lastNameField.setText(lastname);
                    shiftField.setText(shift);
                    builder.show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private boolean checkFields(String name, String lastname, String shift) {

        //Check if one or both fields are empty.
        if (name.isEmpty() || lastname.isEmpty() || shift.isEmpty()) {
            try {
                throw new Exception("Error: All fields must be filled.");
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check name field.
        if (!name.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a vaild name");
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check last name field.
        if (!lastname.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a valid lastname");
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check last name field.
        if (!shift.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Shift must contain only characters");
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }
}