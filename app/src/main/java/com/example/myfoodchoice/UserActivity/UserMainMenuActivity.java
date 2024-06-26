package com.example.myfoodchoice.UserActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myfoodchoice.ModelSignUp.Account;
import com.example.myfoodchoice.ModelSignUp.UserProfile;
import com.example.myfoodchoice.R;
import com.example.myfoodchoice.SharedReviewAllActorsFragment.ReviewFragment;
import com.example.myfoodchoice.UserFragment.UserLogMealFragment;
import com.example.myfoodchoice.UserFragment.UserProfileViewFragment;
import com.example.myfoodchoice.UserFragment.UserUpgradeAccountToPremiumFragment;
import com.example.myfoodchoice.UserFragment.UserViewHealthTipsFragment;
import com.example.myfoodchoice.UserFragment.UserViewMealHistoryFragment;
import com.example.myfoodchoice.UserFragment.UserViewRecipeFragment;
import com.example.myfoodchoice.WelcomeActivity.WelcomeActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;

import io.paperdb.Paper;

public class UserMainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String PATH_USERPROFILE = "Android User Profile";
    DrawerLayout drawerLayout;

    // todo: init firebase components
    DatabaseReference databaseReferenceRegisteredUser,
            databaseReferenceUserProfile;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;

    FirebaseUser firebaseUser;

    String userID;

    // TODO: declare UI components on the
    NavigationView navigationView;

    View headerView;

    TextView headerFullName, headerEmail;

    ImageView headerProfilePicture;

    final static String TAG = "UserMainMenuActivity";

    final static String PATH_ACCOUNT = "Registered Accounts"; // FIXME: the path need to access the account.
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_menu);

        // TODO: init Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance
                ("https://myfoodchoice-dc7bd-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // TODO: init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // TODO: init user id
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            userID = firebaseUser.getUid();
            // TODO: init database reference for user account
            databaseReferenceRegisteredUser = firebaseDatabase.getReference(PATH_ACCOUNT).child(userID);
            databaseReferenceRegisteredUser.addListenerForSingleValueEvent(valueRegisteredUserEventListener());

            // TODO: init database reference for user profile
            databaseReferenceUserProfile = firebaseDatabase.getReference(PATH_USERPROFILE).child(userID);
            databaseReferenceUserProfile.addListenerForSingleValueEvent(valueUserProfileEventListener());
        }

        // TODO: init UI component in nav_header and activity_main_menu
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        headerFullName = headerView.findViewById(R.id.fullNameText);
        headerEmail = headerView.findViewById(R.id.emailText);
        headerProfilePicture = headerView.findViewById(R.id.profilePicture);
        drawerLayout = findViewById(R.id.drawer_layout);


        // TODO: set UI components in nav_header

        // TODO: for navigation drawer
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Smart Food Choice");
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        // TODO: the animation is here in navigation drawer should record this in report file.
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new UserLogMealFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_view);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */)
        {
            @Override
            public void handleOnBackPressed()
            {
                // handle the back button press
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @NonNull
    @Contract(" -> new")
    private ValueEventListener valueUserProfileEventListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // FIXME: the problem is that userProfile is null.
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                // Log.d(TAG, "onDataChange: " + userProfile);

                // set the first name and last name in the UI
                if (userProfile != null)
                {
                    // set full name here
                    String fullName = userProfile.getFirstName() + " " + userProfile.getLastName();
                    headerFullName.setText(fullName);

                    // set profile picture here
                    String profileImageUrl = userProfile.getProfileImageUrl();
                    Uri profileImageUri = Uri.parse(profileImageUrl);
                    // FIXME: the image doesn't show because the image source is from Gallery within android device.
                    // Log.d(TAG, "onDataChange: " + profileImageUri.toString());
                    Picasso.get()
                            .load(profileImageUri)
                            .resize(150, 150)
                            .error(R.drawable.error)
                            .into(headerProfilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText
                        (UserMainMenuActivity.this, "Error database connection", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "loadUserProfile:onCancelled ", error.toException());
            }
        };
    }

    @NonNull
    @Contract(" -> new")
    private ValueEventListener valueRegisteredUserEventListener()
    {
        {
            return new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    // check if the dataSnapshot contains the user profile data
                    if (snapshot.exists())
                    {
                        // extract data from firebase database
                        Account account = snapshot.getValue(Account.class);
                        // Log.d(TAG, "onDataChange: " + userAccount);
                        // Log.d(TAG, "onDataChange: " + userProfile);
                        if (account != null)
                        {
                            // set email
                            String email = account.getEmail();
                            headerEmail.setText(email);
                        }

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Toast.makeText
                            (UserMainMenuActivity.this, "Error database connection", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "loadUserProfile:onCancelled ", error.toException());
                }
            };
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int itemId = item.getItemId();

        Fragment fragment = null;
        String fragmentTag = null;
        // TODO: implement more tab here
        // TODO: our task is to follow the wireframe diagram from web and complete more UI for prototype, that is it.
        if (itemId == R.id.nav_log_my_meal)
        {
            fragment = new UserLogMealFragment();
            fragmentTag = "UserLogMealFragment";
        }

        else if (itemId == R.id.nav_meal_history)
        {
            fragment  = new UserViewMealHistoryFragment();
            fragmentTag = "UserViewMealHistoryFragment";
        }

        else if (itemId == R.id.nav_food_recipe)
        {
            fragment = new UserViewRecipeFragment();
            fragmentTag = "UserRecipeFragment";
        }

        else if (itemId == R.id.nav_health_tips)
        {
            fragment = new UserViewHealthTipsFragment();
            fragmentTag = "UserHealthTipsFragment";
        }

        else if (itemId == R.id.nav_upgrade)
        {
            fragment = new UserUpgradeAccountToPremiumFragment();
            fragmentTag = "UserUpgradeAccountToPremiumFragment";
        }

        else if (itemId == R.id.nav_review)
        {
            fragment = new ReviewFragment();
            fragmentTag = "ReviewFragment";
        }

        // TODO: update and view user profile
        // our plan is to make 3 in 1 manage profile part.
        else if (itemId == R.id.nav_manage_userProfile)
        {
            fragment = new UserProfileViewFragment();
            fragmentTag = "UserProfileViewFragment";
        }

        else if (itemId == R.id.nav_log_off)
        {
            // TODO: destroy this temporary db.
            Paper.book().destroy();
            // log out
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        if (fragment != null)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // fixme: this will help to increase the performance by reusing the old fragment, not init new one.
            Fragment existingFragment = fragmentManager.findFragmentByTag(fragmentTag);
            if (existingFragment != null)
            {
                // If it exists, show the existing fragment
                transaction.show(existingFragment);
            }
            else
            {
                // If not, add the fragment to the back stack
                transaction.replace(R.id.fragment_container, fragment, fragmentTag);
            }
            transaction.commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}