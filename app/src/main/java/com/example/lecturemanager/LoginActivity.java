package com.example.lecturemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnEnter;
    Button btnSignUp;
    EditText etName,etPassword;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this);

         mAuth = FirebaseAuth.getInstance();

         btnSignUp = findViewById(R.id.btnSignUp);
         btnSignUp.setOnClickListener(this);

         etName = findViewById(R.id.etName);
         etPassword = findViewById(R.id.etPassword);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEnter){

            String email = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "נא למלא שם משתמש וסיסמה", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "כניסה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent(LoginActivity.this, GroupsActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this,
                                    "שגיאה בכניסה: שם משתמש או סיסמה שגויים",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
        if (v.getId() == R.id.btnSignUp){

                String email = etName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(this, "הסיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "הרשמה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();


                                 startActivity(new Intent(this, GroupsActivity.class));
                                 finish();

                            } else {
                                Toast.makeText(this,
                                        "שגיאה בהרשמה: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }
    }
