package com.example.yash.vba;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MechanicSettingsActivity extends AppCompatActivity {

    private EditText mNsmeField,mPhoneField,mGarrage;
    private Button mBack,mConfirm;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mTravallerDatabase;
    private String userID,mName,mPhone,mProfileImageUrl,mgarr,mService;
    private Uri resultUri;

    private RadioGroup mRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_settings);

        mNsmeField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mProfileImage = (ImageView)findViewById(R.id.profileImage);
        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        mConfirm = (Button) findViewById(R.id.confirm);
        mGarrage=(EditText) findViewById(R.id.Garrage);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mTravallerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);
        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }

        });
    }



    private void getUserInfo()
    {
        mTravallerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map=(Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNsmeField.setText(mName);
                    }
                    if(map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("garrage")!=null){
                        mGarrage.setText(map.get("garrage").toString());
                    }
                    if(map.get("service")!=null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case "2 wheel":mRadioGroup.check(R.id.wheel2);break;
                            case "3 wheel":mRadioGroup.check(R.id.wheel3);break;
                            case "4 wheel":mRadioGroup.check(R.id.wheel4);break;
                            case "4+ wheel":mRadioGroup.check(R.id.wheel4p);break;

                        }
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl= map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void saveUserInformation() {

        mName=mNsmeField.getText().toString();
        mPhone=mPhoneField.getText().toString();
        mgarr=mGarrage.getText().toString();

        int  selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton= (RadioButton)findViewById(selectId);

        if(radioButton.getText() == null)
        {
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("garrage",mgarr);
        userInfo.put("service", mService);
        mTravallerDatabase.updateChildren(userInfo);

        if(resultUri != null)
        {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl",uri.toString());
                            mTravallerDatabase.updateChildren(newImage);
                            finish();
                            return;
                        }
                    });

                }
            });
        }else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data!=null && data.getData()!=null){
            final Uri imageUri = data.getData();
            resultUri= imageUri;
            mProfileImage.setImageURI(resultUri);
        }


    }

}