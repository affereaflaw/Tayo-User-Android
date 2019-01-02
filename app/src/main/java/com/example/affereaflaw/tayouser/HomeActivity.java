package com.example.affereaflaw.tayouser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private Button btnKeluar;
    private TextView txtNama, txtUsername, txtEmail, txtGender, txtPhone, txtDate, txtTumpang;
    private String userKey;
    private FirebaseAuth auth;
    private DatabaseReference dbProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnKeluar = (Button) findViewById(R.id.btn_logout);
        txtNama = (TextView) findViewById(R.id.profilNama);
        txtTumpang = (TextView) findViewById(R.id.profilTotalCommute_value);
        txtEmail = (TextView) findViewById(R.id.profilEmail);
        txtDate = (TextView) findViewById(R.id.profilTTL);
        txtGender = (TextView) findViewById(R.id.profilGender);
        txtPhone = (TextView) findViewById(R.id.profilNoHp);

        auth = FirebaseAuth.getInstance();

        userKey = auth.getCurrentUser().getUid();
        dbProfil = FirebaseDatabase.getInstance().getReference().child("Users");
        dbProfil.child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nameView = (String) dataSnapshot.child("nama").getValue();
                //String usernameView = (String) dataSnapshot.child("username").getValue();
                String emailView = (String) dataSnapshot.child("email").getValue();
                String genderView = (String) dataSnapshot.child("gender").getValue();
                String PhoneView = (String) dataSnapshot.child("phone").getValue();
                String ttlView = (String) dataSnapshot.child("tanggal lahir").getValue();
                int tumpang = dataSnapshot.child("tumpang").getValue(Integer.class);
                String tumpangView = tumpang + "";

                txtNama.setText(nameView);
                txtDate.setText(ttlView);
                //txtUsername.setText(usernameView);
                txtEmail.setText(emailView);
                txtGender.setText(genderView);
                txtPhone.setText(PhoneView);
                txtTumpang.setText(tumpangView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
