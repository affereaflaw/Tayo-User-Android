package com.example.affereaflaw.tayouser;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private String userKey;
    private EditText inputName, inputPass, inputEmail, inputPhone, etDate, inputPassConf;
    private Button btnDaftar;
    private RadioButton radGender, radGender2;
    private FirebaseAuth auth;
    private DatabaseReference userProfil;
    private Calendar tanggal;
    private RadioGroup groupGender;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //meninstance firebase auth
        auth = FirebaseAuth.getInstance();

        btnDaftar       = (Button) findViewById(R.id.btn_Daftar);
        inputName   = (EditText) findViewById(R.id.txtNama);
        inputPhone   = (EditText) findViewById(R.id.txtPhone);
        inputPassConf = (EditText) findViewById(R.id.txtConfirm_pass);
        inputPass       = (EditText) findViewById(R.id.txtPassword_daftar);
        inputEmail      = (EditText) findViewById(R.id.txtEmail_daftar);
        radGender       = (RadioButton) findViewById(R.id.btn_Lk);
        radGender2      = (RadioButton) findViewById(R.id.btn_Pr);
        etDate          = (EditText) findViewById(R.id.txtDate);
        groupGender     = (RadioGroup) findViewById(R.id.groupGender);

        progressDialog = new ProgressDialog(this);

        if(etDate.hasSelection()){
            etDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    tanggal = Calendar.getInstance();
                    int tahun = tanggal.get(Calendar.YEAR);
                    int bulan = tanggal.get(Calendar.MONTH);
                    int hari = tanggal.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            int bulan = i1+1;
                            etDate.setText(i2+"/"+bulan+"/"+i);
                        }
                    }, tahun, bulan, hari);
                    datePickerDialog.show();
                }
            });
        }



        //ketika button daftar diklik
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name  = inputName.getText().toString().trim();
                final String noHp = inputPhone.getText().toString().trim();
                final String ttl    = etDate.getText().toString().trim();
                final String email    = inputEmail.getText().toString().trim();
                final String password = inputPass.getText().toString().trim();
                final String passwordConf = inputPassConf.getText().toString().trim();
                final int tumpang = 0;

                Integer genderLP= groupGender.getCheckedRadioButtonId();
                radGender = (RadioButton) findViewById(genderLP);
                radGender2 = (RadioButton) findViewById(genderLP);


//                progressDialog.setMessage("Register...");
//                progressDialog.show();
                //membuat user

                auth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                final String genderStr;

                                if (!radGender.isChecked() && !radGender2.isChecked()){
                                    Toast.makeText(getApplicationContext(), "Please choose your Gender", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if(radGender.isChecked()){
                                    RadioButton rb = (RadioButton) findViewById(R.id.btn_Lk);
                                    genderStr = rb.getText().toString().trim();
                                } else {
                                    RadioButton rb = (RadioButton) findViewById(R.id.btn_Pr);
                                    genderStr = rb.getText().toString().trim();
                                }

                                if (TextUtils.isEmpty(name)){
                                    Toast.makeText(getApplicationContext(),"Please input Username", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (TextUtils.isEmpty(email)){
                                    Toast.makeText(getApplicationContext(),"Please input your Email", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (TextUtils.isEmpty(password)){
                                    Toast.makeText(getApplicationContext(),"Please enter Password", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (password.length()<6){
                                    Toast.makeText(getApplicationContext(), "Password minimum 6 characters",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (!passwordConf.equals((password))){
                                    Toast.makeText(getApplicationContext(), "Password Password Didnt Match",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Toast.makeText(RegisterActivity.this, "createUserWithEmail:OnComplete" + task.isSuccessful()
                                        ,Toast.LENGTH_SHORT).show();
                                //Mengambil key user yang unik
                                userKey = auth.getCurrentUser().getUid();
                                //Simpan ke database
                                userProfil = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("nama", name);
                                userMap.put("email", email);
                                userMap.put("password", password);
                                userMap.put("tanggal lahir", ttl);
                                userMap.put("gender", genderStr);
                                userMap.put("phone", noHp);
                                userProfil.setValue(userMap);
                                userProfil.child("tumpang").setValue(tumpang);
                                //jika pendaftaran gagal, akan muncul pesan
                                //jika berhasil akan ada notif sukses



                                if(!task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Authentication failed" + task.getException()
                                            ,Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    startActivity(new Intent(RegisterActivity.this, MainMenu.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }
}