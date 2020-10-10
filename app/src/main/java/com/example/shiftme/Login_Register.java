package com.example.shiftme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;

import com.example.shiftme.dataTypes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login_Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private EditText email;
    private EditText password;

    private CheckBox LoginCheckBox;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        LoginCheckBox = (CheckBox) findViewById(R.id.rememberMeCheckbox);
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        CardView login = (CardView) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginDialog();
            }
        });

        TextView singUp = (TextView) findViewById(R.id.singUp);
        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singUpDialog();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        databaseRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(currentUser != null && snapshot.child(currentUser.getUid()).getValue() != null){
                    Intent intent = new Intent(Login_Register.this, Calendar.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        loadPreferences();
    }

    private void loadPreferences() {
        if (prefs != null) {
            String email_pref = prefs.getString("EMAIL", "");
            String pass_pref = prefs.getString("PASS", "");
            boolean checkbox_state = prefs.getBoolean("CHECKBOX_STATE", false);
            email.setText(email_pref);
            password.setText(pass_pref);
            LoginCheckBox.setChecked(checkbox_state);
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EMAIL", email.getText().toString());
        editor.putString("PASS", password.getText().toString());
        editor.putBoolean("CHECKBOX_STATE", true);
        editor.apply();
    }

    private void clearPreferences() {
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("EMAIL");
            editor.remove("PASS");
            editor.remove("CHECKBOX_STATE");
            editor.apply();
        }
    }

    //Check fields with exceptions
    private boolean checkFields(String name, String lastname, String email, String password,
                                String confirmPass, String managerPass, boolean isChecked) {

        //Check if one or both fields are empty.
        if (name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty() || (isChecked && managerPass.isEmpty())) {
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
                throw new Exception("Error: Please enter a valid name.");
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

        //Check email field.
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            try {
                throw new Exception("Error: Invalid mail format.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check password field.
        if (!password.matches("^(?!.*\\s).{6,14}$")) {
            try {
                throw new Exception("Error: Invalid Password (No spaces allowed. Minimum length: 6)");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check password match.
        if (!confirmPass.matches(password)) {
            try {
                throw new Exception("Error: Password does not match.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //Check manager password.
        if (isChecked && !managerPass.matches("123121")) {
            try {
                throw new Exception("Error: Wrong manager password.");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void loginDialog(){
        if (TextUtils.isEmpty(email.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(Login_Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                password.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(Login_Register.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (LoginCheckBox.isChecked()) {
                                savePreferences();
                            } else {
                                clearPreferences();
                            }
                            Intent intent = new Intent(Login_Register.this, Calendar.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    };

    private void pushUserIntoDatabase(String firstname, String lastname, String email, String group) {
        currentUser = mAuth.getCurrentUser();
        User user = new User(firstname, lastname, email, group);
        DatabaseReference usersRef = databaseRef.child("Users").child(currentUser.getUid());
        usersRef.setValue(user).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.err.println(e.getMessage());
            }
        });
    }

    private void singUpDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") final View subView = inflater.inflate(R.layout.register_layout, null);

        final EditText nameField = (EditText)subView.findViewById(R.id.name);
        final EditText lastNameField = (EditText)subView.findViewById(R.id.lastName);
        final EditText emailField = (EditText)subView.findViewById(R.id.email);
        final EditText passField = (EditText)subView.findViewById(R.id.pass);
        final EditText confirmPassField = (EditText)subView.findViewById(R.id.confirmPass);
        final EditText managerPassField = (EditText) subView.findViewById(R.id.managerPass);

        final CheckBox managerCheckBox = (CheckBox) subView.findViewById(R.id.managerCheckbox);;
        managerCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(managerCheckBox.isChecked()){
                    managerPassField.setVisibility(View.VISIBLE);
                }
                else {
                    managerPassField.setVisibility(View.GONE);
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Form");
        builder.setView(subView);
        builder.create();

        builder.setPositiveButton("Sing Up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String firstname = nameField.getText().toString();
                final String lastname = lastNameField.getText().toString();
                final String mail = emailField.getText().toString();
                final String psw = passField.getText().toString();
                final String confirmPass = confirmPassField.getText().toString();
                final String managerPass = managerPassField.getText().toString();
                final boolean isChecked = managerCheckBox.isChecked();
                final String[] group = new String[1];

                if (checkFields(firstname, lastname, mail, psw, confirmPass, managerPass, isChecked)) {
                    mAuth.createUserWithEmailAndPassword(mail, psw)
                            .addOnCompleteListener(Login_Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if(isChecked){
                                            group[0] = "Manager";
                                        }
                                        else {
                                            group[0] = "Worker";
                                        }
                                        pushUserIntoDatabase(firstname, lastname, mail, group[0]);
                                        finish();
                                        startActivity(new Intent(Login_Register.this, Calendar.class));
                                    }
                                }
                            });
                } else {
                    ((ViewGroup) subView.getParent()).removeView(subView);
                    nameField.setText(firstname);
                    lastNameField.setText(lastname);
                    emailField.setText(mail);
                    passField.setText("");
                    confirmPassField.setText("");
                    managerPassField.setText("");
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

}
