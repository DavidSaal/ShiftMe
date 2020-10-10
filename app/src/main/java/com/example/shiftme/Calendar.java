package com.example.shiftme;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import com.example.shiftme.Adapter.WorkerAdapter;
import com.example.shiftme.dataTypes.User;
import com.google.firebase.database.ValueEventListener;
import com.venmo.view.TooltipView;

public class Calendar extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    CompactCalendarView calendar;
    private ArrayList<User> allUsers = new ArrayList<>();
    private RecyclerView userView;

    private String dateclicked;
    private Long dateclickedTime;
    private Date dateNow = new Date();
    private String userId;

    private int check = 0;

    @Override
    public void onStart() {
        super.onStart();
        if (currentUser == null) {
            startActivity(new Intent(this, Login_Register.class));
            finish();
        }

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                calendar.removeAllEvents();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String timeInMillis = ds.getKey();
                    assert timeInMillis != null;
                    for (int i = 0;  i < ds.getValue(Integer.class); i++) {
                        Event event = new Event(Color.GREEN, Long.parseLong(timeInMillis));
                        calendar.addEvent(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        DatabaseReference events = databaseRef.child("Events");
        events.addValueEventListener(postListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("ShiftMe");
        calendar = findViewById(R.id.calendarView);
        calendar.setUseThreeLetterAbbreviation(true);

        final FloatingActionButton btnAdd = (FloatingActionButton) findViewById(R.id.btnAdd);
        final TooltipView addTooltip = (TooltipView) findViewById(R.id.addTooltip);

        databaseRef.child("Users").child(currentUser.getUid()).child("group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(Objects.equals(snapshot.getValue(), "Worker")){
                    btnAdd.setVisibility(View.GONE);
                    addTooltip.setVisibility(View.GONE);
                }
                else{
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addWorkerDialog();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        FloatingActionButton btnUserList = (FloatingActionButton) findViewById(R.id.btnUserList);
        btnUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Calendar.this, UserList.class));
            }
        });

        userView = (RecyclerView)findViewById(R.id.user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        userView.setLayoutManager(linearLayoutManager);
        userView.setHasFixedSize(true);
        userView.setVisibility(View.VISIBLE);

        calendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                allUsers.clear();
                dateclickedTime = dateClicked.getTime();
                dateclicked = dateForamt(dateClicked);

                if(calendar.getEvents(dateClicked).isEmpty()){
                    setAdapter();
                }else {
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                String id = ds.getKey();
                                String firstname = ds.child("firstName").getValue(String.class);
                                String lastname = ds.child("lastName").getValue(String.class);
                                String shift = ds.child("shift").getValue(String.class);
                                User user = new User(id, firstname, lastname, shift, "Worker");
                                allUsers.add(user);
                            }
                            setAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };
                    DatabaseReference shifts = databaseRef.child("Shifts").child(String.valueOf(dateclicked));
                    shifts.addValueEventListener(postListener);
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editAccount:
                editAccount();
                break;
            case R.id.changePass:
                changePass();
                break;
            case R.id.deleteUser:
                deleteUser();
                break;
            case R.id.singout:
                singOut();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void editAccount(){
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View subView = inflater.inflate(R.layout.edit_account_layout, null);

        final EditText nameField = (EditText)subView.findViewById(R.id.name);
        final EditText lastNameField = (EditText)subView.findViewById(R.id.lastName);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Account");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String firstname = nameField.getText().toString();
                final String lastname = lastNameField.getText().toString();

                if(checkEditUserFields(firstname, lastname)){
                    DatabaseReference usersRef = databaseRef.child("Users").child(currentUser.getUid());
                    usersRef.child("firstName").setValue(firstname);
                    usersRef.child("lastName").setValue(lastname);
                    Toast.makeText(Calendar.this, "Account Successfully Updated!", Toast.LENGTH_LONG).show();
                }
                else{
                    ((ViewGroup)subView.getParent()).removeView(subView);
                    nameField.setText(firstname);
                    lastNameField.setText(lastname);
                    builder.show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    protected void changePass(){
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View subView = inflater.inflate(R.layout.change_pass_layout, null);

        final EditText passField = (EditText)subView.findViewById(R.id.pass);
        final EditText confirmPassField = (EditText)subView.findViewById(R.id.confirmPass);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter A New Password");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String pass = passField.getText().toString();
                final String confirmPass = confirmPassField.getText().toString();

                if(checkChangePassFields(pass, confirmPass)){
                    currentUser.updatePassword(pass);
                    Toast.makeText(Calendar.this, "Password Successfully Changed!", Toast.LENGTH_LONG).show();
                }
                else{
                    ((ViewGroup)subView.getParent()).removeView(subView);
                    passField.setText("");
                    confirmPassField.setText("");
                    builder.show();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    protected void deleteUser(){
        AlertDialog.Builder deleteUserDialogBuilder = new AlertDialog.Builder(this);
        deleteUserDialogBuilder.setMessage("Are you sure you want to delete your account?");
        deleteUserDialogBuilder.setCancelable(true);
        deleteUserDialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        // Remove user from authentication
                        assert user != null;
                        user.delete();
                        // Remove user from database
                        usersRef.child(currentUser.getUid()).removeValue();
                        // Go back to the login activity
                        startActivity(new Intent(Calendar.this, Login_Register.class));
                    }
                });
        deleteUserDialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog deleteUserDialog = deleteUserDialogBuilder.create();
        deleteUserDialog.show();
    }

    protected void singOut(){
        AlertDialog.Builder signOutDialogBuilder = new AlertDialog.Builder(this);
        signOutDialogBuilder.setMessage("Are you sure you want to sign out?");
        signOutDialogBuilder.setCancelable(true);
        signOutDialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mAuth.signOut();
                        final Intent login = new Intent(Calendar.this, Login_Register.class);
                        startActivity(login);
                        finish();
                    }
                });
        signOutDialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog signoutDialog = signOutDialogBuilder.create();
        signoutDialog.show();
    }

    private boolean checkFields(String name, String lastname, String shift) {

        //Check if one or both fields are empty.
        if (name.isEmpty() || lastname.isEmpty() || shift.isEmpty()) {
            try {
                throw new Exception("Error: All fields must be filled.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check name field.
        if (!name.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a vaild name.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check last name field.
        if (!lastname.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a valid lastname.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check last name field.
        if (!shift.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Shift must contain only characters.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private boolean checkEditUserFields(String name, String lastname) {

        //Check if one or both fields are empty.
        if (name.isEmpty() || lastname.isEmpty()) {
            try {
                throw new Exception("Error: All fields must be filled.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check name field.
        if (!name.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a vaild name.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check last name field.
        if (!lastname.matches("[a-zA-Z]+[[\\s][a-zA-Z]+]*")) {
            try {
                throw new Exception("Error: Please enter a valid lastname.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private boolean checkChangePassFields(String pass, String confirmPass) {

        //Check if one or both fields are empty.
        if (pass.isEmpty() || confirmPass.isEmpty()) {
            try {
                throw new Exception("Error: All fields must be filled.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check password field.
        if (!pass.matches("^(?!.*\\s).{6,14}$")) {
            try {
                throw new Exception("Error: Invalid Password (No spaces allowed. Minimum length: 6)");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check password match.
        if (!confirmPass.matches(pass)) {
            try {
                throw new Exception("Error: Password does not match.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private String dateForamt(Date dateClicked){
        String day = (String) DateFormat.format("dd", dateClicked);
        String month = (String) DateFormat.format("MM", dateClicked);
        String year = (String) DateFormat.format("yyyy", dateClicked);
        return day + "-" + month + "-" + year;
    }

    public void setAdapter(){
        WorkerAdapter workerAdapter = new WorkerAdapter(Calendar.this, allUsers, dateclicked, dateclickedTime);
        userView.setAdapter(workerAdapter);
    }

    private void addWorkerDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View subView = inflater.inflate(R.layout.add_shift_layout, null);

        final EditText firstNameField = (EditText)subView.findViewById(R.id.firstname);
        final EditText lastNameField = (EditText)subView.findViewById(R.id.lastname);
        final EditText shiftField = (EditText)subView.findViewById(R.id.shift);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Shift Worker");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("Add Worker", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                check = 0;
                final String name = firstNameField.getText().toString();
                final String lastname = lastNameField.getText().toString();
                final String shift = shiftField.getText().toString();

                if(checkFields(name, lastname, shift)){
                    if(dateclicked == null){
                        dateclicked = dateForamt(dateNow);
                        dateclickedTime = dateNow.getTime();
                    }
                    userId = databaseRef.push().getKey();
                    User newUser = new User(userId, name, lastname, shift, "Worker");
                    databaseRef.child("Shifts").child(String.valueOf(dateclicked)).child(userId).setValue(newUser);
                    allUsers.add(newUser);

                    final DatabaseReference eventQuery = databaseRef.child("Events").child(Long.toString(dateclickedTime));
                        eventQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Integer getValue = snapshot.getValue(Integer.class);
                                if(getValue == null){
                                    eventQuery.setValue(1);
                                    check = 1;
                                }
                                else{
                                    if(check == 0) {
                                        eventQuery.setValue(getValue + 1);
                                        check = 1;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    Event event = new Event(Color.GREEN, dateclickedTime);
                    calendar.addEvent(event);

                    setAdapter();

                    //startActivity(getIntent());
                    //calendar.addEvent(event);
                    //finish();

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

    @Override
    public void onClick(View v) {

    }

}