package com.example.myfoodchoice.AuthenticationActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myfoodchoice.ModelSignUp.UserProfile;
import com.example.myfoodchoice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class UserProfileCreateFirstActivity extends AppCompatActivity
{
    // TODO: declare UI components
    EditText age, editIntHeight, editIntWeight;

    ImageView profilePicture, maleImage, femaleImage;
    ProgressBar progressBar;

    Button nextBtn;

    String myUri, gender;

    int ageInt;

    FirebaseDatabase firebaseDatabase;

    UserProfile userProfile;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    DatabaseReference databaseReferenceUserProfile;

    StorageReference storageReferenceProfilePics;

    StorageTask<UploadTask.TaskSnapshot> storageTask;

    final static String TAG = "UserProfileCreateFirstActivity";

    ActivityResultLauncher<Intent> activityResultLauncher;

    static final String LABEL = "Android User Profile";

    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_create_first);

        // TODO: init Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance
                ("https://myfoodchoice-dc7bd-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // TODO: init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            databaseReferenceUserProfile = firebaseDatabase.getReference(LABEL).child(firebaseUser.getUid());
            storageReferenceProfilePics =
                    FirebaseStorage.getInstance().getReference()
                            .child("Profile Pics")
                            .child(firebaseUser.getUid());
        }

        // TODO: init UI components
        age = findViewById(R.id.ageProfile);
        profilePicture = findViewById(R.id.profilePicture);
        progressBar = findViewById(R.id.progressBar);
        nextBtn = findViewById(R.id.nextBtn);
        maleImage = findViewById(R.id.maleImage);
        femaleImage = findViewById(R.id.femaleImage);
        editIntHeight = findViewById(R.id.heightProfile);
        editIntWeight = findViewById(R.id.weightProfile);

        // TODO: init UI component
        /*
        spinnerActivity = findViewById(R.id.activitySpinner);
        activityArrayList = new ArrayList<>();
        initListActivityLevel();
        ActivityLevelAdapter activityLevelAdapter = new ActivityLevelAdapter(this, activityArrayList);
        spinnerActivity.setAdapter(activityLevelAdapter);
        spinnerActivity.setOnItemSelectedListener(onItemSelectedActivityListener);
         */

        // set gender to male by default
        gender = "Default";
        maleImage.setOnClickListener(onMaleClickListener());
        femaleImage.setOnClickListener(onFemaleClickListener());

        // set progress bar to gone
        progressBar.setVisibility(ProgressBar.GONE);

        // set onClickListener button
        nextBtn.setOnClickListener(onCreateProfileListener());

        // init userProfile
        userProfile = getIntent().getParcelableExtra("userProfile");
        // log debug
        // Log.d(TAG, "onCreate: " + userProfile);

        profilePicture.setOnClickListener(onImageClickListener());

        // init activity for gallery part.
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->
                {
                    if (result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null)
                        {
                            // get the URI of the selected image
                            selectedImageUri = data.getData();
                            // Log.d(TAG, "Selected image URI: " + selectedImageUri);
                            // display the selected image in an ImageView.
                            profilePicture.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        // TODO: this is to display the profile picture in here
        // we can reference this function to UserMainMenuActivity and use it to display the profile picture.
        getUserInfo();
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onCreateProfileListener()
    {
        return v ->
        {
            ageInt = Integer.parseInt(age.getText().toString().trim());
            //Log.d(TAG, "onCreateProfileListener: " + gender);

            // TODO: the user can click on image view and select their profile picture or take new picture to upload
            // TODO: to Firebase database.

            // check if selectedImageUri is null
            if (selectedImageUri == null)
            {
                Toast.makeText(UserProfileCreateFirstActivity.this,
                        "Please select a profile picture.", Toast.LENGTH_SHORT).show();
                return; // exit the method if selectedImageUri is null
            }

            if (Objects.equals(gender, "Default"))
            {
                Toast.makeText(UserProfileCreateFirstActivity.this,
                        "Please select a gender.", Toast.LENGTH_SHORT).show();
                return; // exit the method if gender is not selected
            }

            if (Integer.parseInt(age.getText().toString().trim()) > 100
                    || Integer.parseInt(age.getText().toString().trim()) < 0)
            {
                age.setError("Please enter a valid age.");
                age.requestFocus();
                return; // exit the method if age is not entered
            }

            if (TextUtils.isEmpty(age.getText().toString().trim()))
            {
                age.setError("Please enter your age.");
                age.requestFocus();
                return; // exit the method if age is not entered
            }

            // for height, centimeter unit.
            if (TextUtils.isEmpty(editIntHeight.getText().toString()))
            {
                editIntHeight.setError("Please enter your height");
                editIntHeight.requestFocus();
                return;
            }

            int height = Integer.parseInt(editIntHeight.getText().toString());

            if (height < 50 || height > 250)
            {
                editIntHeight.setError("Invalid height value meter");
                editIntHeight.requestFocus();
                return;
            }

            // for weight, kg unit
            if (TextUtils.isEmpty(editIntWeight.getText().toString()))
            {
                editIntWeight.setError("Please enter your weight");
                editIntWeight.requestFocus();
                return;
            }

            int weight = Integer.parseInt(editIntWeight.getText().toString());

            if (weight < 20 || weight > 120)
            {
                editIntWeight.setError("Invalid weight value kg");
                editIntWeight.requestFocus();
                return;
            }

            weight = Integer.parseInt(editIntWeight.getText().toString());
            height = Integer.parseInt(editIntHeight.getText().toString());

            // make the progress bar appear
            progressBar.setVisibility(ProgressBar.VISIBLE);
            nextBtn.setVisibility(Button.GONE);

            // upload the image to Firebase Storage
            final StorageReference storageReference = storageReferenceProfilePics.child
                    (firebaseUser.getDisplayName() + ".jpg"); // FIXME: potential bug.

            // FIXME: the selected image Uri haven't converted to Uri path.
            storageTask = storageReference.putFile(selectedImageUri)
                    .addOnFailureListener(onFailurePart());

            // set the download URL to the user profile
            userProfile.setGender(gender); // FIXME: need to test this field.
            userProfile.setAge(ageInt);
            userProfile.setWeight(String.valueOf(weight));
            userProfile.setHeight(String.valueOf(height));

            // set image here
            storageTask.continueWithTask(task ->
            {
                if (!task.isSuccessful())
                {
                    throw Objects.requireNonNull(task.getException());
                }
                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(onCompleteUploadListener());
        };
    }


    @NonNull
    @Contract(pure = true)
    private OnFailureListener onFailurePart() // FIXME: for debug purpose
    {
        return v ->
        {
            Log.d(TAG, "onFailurePart: " + v);
        };
    }

    @NonNull
    @Contract(pure = true)
    private OnCompleteListener<Uri> onCompleteUploadListener()
    {
        return task ->
        {
            // FIXME: profile picture task is unsuccessful.
            if (task.isSuccessful())
            {
                Uri downloadUri = task.getResult();
                myUri = downloadUri.toString();

                // FIXME:
                userProfile.setProfileImageUrl(myUri);
                // Log.d(TAG, "onCreateProfileListener: " + selectedImageUri);
                // Log.d(TAG, "onNextListener: " + userProfile);
                // TODO: set the value based on UserProfile class.
                // databaseReferenceUserProfile.setValue(userProfile).addOnCompleteListener(onCompleteListener());

                firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(myUri)).build()).addOnCompleteListener(onCompleteNextListener());
                // Log.d(TAG,"onCompeteUploadListener: " + firebaseUser.getPhotoUrl());
            }
            else
            {
                Log.d(TAG, "onCompleteUploadListener: " + myUri);
                progressBar.setVisibility(ProgressBar.GONE);
                //Log.e(TAG, "onCompleteUploadListener: " + task.getException());
                Toast.makeText(UserProfileCreateFirstActivity.this,
                        "Image not selected.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void getUserInfo()
    {
        databaseReferenceUserProfile.child(firebaseUser.getUid()).addValueEventListener(valueEventListener());
    }

    @NonNull
    @Contract(" -> new")
    private ValueEventListener valueEventListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0)
                {
                    if (snapshot.hasChild("image"))
                    {
                        String image = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                        Picasso.get().load(image).into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(UserProfileCreateFirstActivity.this,
                        "Error getting user info.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onImageClickListener()
    {
        return v ->
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activityResultLauncher.launch(Intent.createChooser(intent, "Select File"));
        };
    }

    // TODO: update this new field for the user to select to get the gender
    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onFemaleClickListener()
    {
        return v ->
        {
            maleImage.setBackground(ContextCompat.getDrawable(this, R.color.peach_puff));
            femaleImage.setBackground(ContextCompat.getDrawable(this, R.color.peach_puff_new));
            gender = "Female";
        };
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onMaleClickListener()
    {
        return v ->
        {
            maleImage.setBackground(ContextCompat.getDrawable(this, R.color.peach_puff_new));
            femaleImage.setBackground(ContextCompat.getDrawable(this, R.color.peach_puff));
            gender = "Male";
        };
    }

    @NonNull
    @Contract(pure = true)
    private OnCompleteListener<Void> onCompleteNextListener()
    {
        {
            return task ->
            {
                Intent intent = new Intent(UserProfileCreateFirstActivity.this,
                        UserProfileHealthDeclarationActivity.class);
                intent.putExtra("userProfile", userProfile);
                startActivity(intent);
                finish(); // to close this page.
            };
        }
    }
}

