package com.example.myfoodchoice.AuthenticationActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myfoodchoice.BusinessDietitianActivity.DietitianMainMenuActivity;
import com.example.myfoodchoice.GuestActivity.GuestMainMenuActivity;
import com.example.myfoodchoice.ModelSignUp.Account;
import com.example.myfoodchoice.Prevalent.Prevalent;
import com.example.myfoodchoice.R;
import com.example.myfoodchoice.UserActivity.UserMainMenuActivity;
import com.example.myfoodchoice.UserActivity.UserPremiumMainMenuActivity;
import com.example.myfoodchoice.WelcomeActivity.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.Contract;

import java.util.UUID;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity
{
    // todo: we should make or incentive the guest
    // todo: guest account doesn't need to register
    // todo: Guest can upgrade to User account.
    // todo: make guest page more interesting to them and interest them into official user.
    // todo: need to put the video

    // TODO: declare UI component
    // button
    Button loginBtn;

    FloatingActionButton backBtn;

    // Edit text
    EditText loginEmailEditText, loginPasswordEditText;

    // check box
    CheckBox rememberMe;

    // clickable text
    TextView clickableForgotPassword, clickableSignUpNav, clickableLoginAsGuestNav;

    // for clickable text
    SpannableString spannableStringSignUpNav, spannableStringForgotPassword;

    // for progress bar
    ProgressBar progressBar;

    // firebase login
    FirebaseAuth mAuth;

    FirebaseUser firebaseUser;

    String guestUserID;

    static final int INDEX_START = 0;

    static final String PATH_DATABASE = "Registered Accounts";

    static final String TAG = "LoginActivity";

    FirebaseDatabase firebaseDatabase;

    String email, password, userID, accountType;

    String emailRememberMe, passwordRememberMe, accountTypeRememberMe;

    Intent intent;

    Account account;

    DatabaseReference databaseReferenceAccountType;

    boolean isTrialOver;

    AlertDialog alertGuestTrialOverDialog; // guest doesn't need to register

    android.app.AlertDialog.Builder builder;

    android.app.AlertDialog alertDialog;
    SpannableString spannableStringLoginAsGuestNav;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Smart Food Choice");

        // TODO: init paper
        Paper.init(LoginActivity.this);

        // TODO: init Firebase auth
        mAuth = FirebaseAuth.getInstance();

        // TODO: init Firebase database, paste the correct link as reference.
        firebaseDatabase = FirebaseDatabase.getInstance(
                "https://myfoodchoice-dc7bd-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // for testing
        // firebaseDatabase.getReference().child("Test").child("new child").setValue("new value");
        // by default is Guest.
        accountType = "Default";

        // TODO: init UI components
        loginEmailEditText = findViewById(R.id.login_email);
        loginPasswordEditText = findViewById(R.id.login_password);

        // init user profile
        // TODO: do we need userProfile here to pass to main-menu?

        // check box
        rememberMe = findViewById(R.id.rememberMe_checkBox);

        // clickable text
        clickableForgotPassword = findViewById(R.id.clickableForgotPassword);
        clickableSignUpNav = findViewById(R.id.clickableSignUpNavText);
        clickableLoginAsGuestNav = findViewById(R.id.clickableLoginAsGuestNavText);

        // button
        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setVisibility(View.VISIBLE);
        loginBtn.setOnClickListener(onLoginListener());
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(onBackBtnListener());

        // nav to sign up page based on text click
        spannableStringSignUpNav = new SpannableString(clickableSignUpNav.getText());
        spannableStringSignUpNav.setSpan(clickableSignUpNavSpan(), 0, clickableSignUpNav.length(), 0);
        clickableSignUpNav.setText(spannableStringSignUpNav);
        clickableSignUpNav.setMovementMethod(LinkMovementMethod.getInstance());

        // nav to forgot password page based on text click
        spannableStringForgotPassword = new SpannableString(clickableForgotPassword.getText());
        spannableStringForgotPassword.setSpan(clickableForgotPasswordNavSpan(), INDEX_START, clickableForgotPassword.length(), 0);
        clickableForgotPassword.setText(spannableStringForgotPassword);
        clickableForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());

        // nav to UserHomePage
        spannableStringLoginAsGuestNav = new SpannableString(clickableLoginAsGuestNav.getText());
        spannableStringLoginAsGuestNav.setSpan(clickableLoginAsGuestNavSpan(), INDEX_START,
                clickableLoginAsGuestNav.length(), 0);
        clickableLoginAsGuestNav.setText(spannableStringLoginAsGuestNav);
        clickableLoginAsGuestNav.setMovementMethod(LinkMovementMethod.getInstance());

        // progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // check box listener
        rememberMe.setOnCheckedChangeListener(onCheckedListener());

        // assign to email and password
        emailRememberMe = Paper.book().read(Prevalent.EmailKey);
        passwordRememberMe = Paper.book().read(Prevalent.PasswordKey);
        accountTypeRememberMe = Paper.book().read(Prevalent.AccountType);

        if (emailRememberMe != null && passwordRememberMe != null
                && accountTypeRememberMe != null)
        {
            if (!TextUtils.isEmpty(emailRememberMe) && !TextUtils.isEmpty(passwordRememberMe)
                    && !TextUtils.isEmpty(accountTypeRememberMe))
            {
                /*
                Log.d(TAG, "onCreate: " +
                        emailRememberMe + " " + passwordRememberMe + " " + accountTypeRememberMe);
                 */
                switch (accountTypeRememberMe)
                {
                    case "User":
                        allowUserLogin(emailRememberMe, passwordRememberMe);
                        break;
                    case "Dietitian":
                        allowDietitianLogin(emailRememberMe, passwordRememberMe);
                        break;
                    case "Premium User":
                        allowPremiumUserLogin(emailRememberMe, passwordRememberMe);
                        break;
                    default:
                        Toast.makeText(LoginActivity.this,
                                "Account type is not recognized, please try again.",
                                Toast.LENGTH_SHORT).show();
                        break;
                }

                if (!isFinishing())
                {
                    builder = new android.app.AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Already Logged in");
                    builder.setMessage("Please wait...");
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onBackBtnListener()
    {
        return v ->
        {
            Intent intent1 = new Intent(LoginActivity.this, WelcomeActivity.class);
            startActivity(intent1);
            finish();
        };
    }

    private void allowPremiumUserLogin(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                // Toast.makeText(LoginActivity.this, "Welcome to Smart Food Choice!", Toast.LENGTH_SHORT).show();
                Intent intent = new
                        Intent(LoginActivity.this, UserPremiumMainMenuActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(LoginActivity.this, "Error, " +
                        "there is something wrong with database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Contract(" -> new") // the purpose to is to recognise the account type
    // and auto choose the correct layout for normal user, dietitian, trainer.
    private ValueEventListener valueAccountTypeEvent()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                account = snapshot.getValue(Account.class);
                if (account != null)
                {
                    accountType = account.getAccountType();
                    isTrialOver = account.isGuestTrialActive();
                    // Log.d(TAG, "Account here: " + account);
                    if (rememberMe.isChecked())
                    {
                        Paper.book().write(Prevalent.AccountType, accountType);
                    }
                    else
                    {
                        Paper.book().delete(Prevalent.AccountType);
                        Paper.book().delete(Prevalent.EmailKey);
                        Paper.book().delete(Prevalent.PasswordKey);
                    }

                    switch (accountType) // FIXME: there is a bug when login, it might inform us.
                    {
                        case "Premium User":
                            intent = new Intent(LoginActivity.this, UserPremiumMainMenuActivity.class);
                            break;
                        case "Dietitian":
                            intent = new Intent(LoginActivity.this, DietitianMainMenuActivity.class);
                            break;
                        case "User":
                            intent = new Intent(LoginActivity.this, UserMainMenuActivity.class);
                            break;

                        default:
                            Toast.makeText(LoginActivity.this,
                                    "Account type is not recognized, please try again.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (intent != null)
                    {
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    Log.d(TAG, "onDataChange: this is else " + accountType);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.d("LoginActivity", "loadUserProfile:onCancelled ", error.toException());
            }
        };
    }

    // TODO: the purpose is to save data via Paper.
    public CompoundButton.OnCheckedChangeListener onCheckedListener()
    {
        return (buttonView, isChecked) ->
        {
            email = loginEmailEditText.getText().toString().trim();
            password = loginPasswordEditText.getText().toString().trim();
            if (isChecked)
            {
                Paper.book().write(Prevalent.EmailKey, email);
                Paper.book().write(Prevalent.PasswordKey, password);
            }
        };
    }
    // TODO: to implement the login functionalities for this activity.
    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onLoginListener()
    {
        return v ->
        {
            email = loginEmailEditText.getText().toString().trim();
            password = loginPasswordEditText.getText().toString().trim();
            allowLogin(email, password);
        };
    }

    public void allowLogin(String email, String password)
    {
        // TODO: login function

        // validation
        if (TextUtils.isEmpty(email))
        {
            loginEmailEditText.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password))
        {
            loginPasswordEditText.setError("Password is required.");
            return;
        }

        // loading
        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.GONE);

        // authentication login
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            // check the condition based on the account type.
            // if the account type is not recognized, then show a toast message.
            // if the account type is recognized, then navigate to the correct main menu page.
            // this one is to monitor the auth state changes

            if (task.isSuccessful())
            {
                firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null)
                {
                    userID = firebaseUser.getUid();
                    // Log.d("LoginActivity", "task is ok: " + firebaseUser.getUid());
                    // fixme: there is a bug with login where databaseReferenceAccountType is null.
                    //Log.d("LoginActivity", "user ID here: " + userID);
                    databaseReferenceAccountType = firebaseDatabase.getReference
                            (PATH_DATABASE).child(userID); // FIXME: careful with the name path
                    //Log.d("LoginActivity", "account type URL here: " + databaseReferenceAccountType);
                    databaseReferenceAccountType.addListenerForSingleValueEvent(valueAccountTypeEvent());
                }
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
                Toast.makeText(LoginActivity.this,
                        "Email or Password incorrect, please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void allowUserLogin(String email, String password)
    {
        // TODO: login function
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                // Toast.makeText(LoginActivity.this, "Welcome to Smart Food Choice!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, UserMainMenuActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void allowDietitianLogin(String email, String password)
    {
        // authentication login
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                // Toast.makeText(LoginActivity.this, "Welcome to Smart Food Choice!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DietitianMainMenuActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing())
        {
            alertDialog.dismiss();
        }
    }

    public ClickableSpan clickableForgotPasswordNavSpan()
    {
        {
            return new ClickableSpan()
            {
                @Override
                public void onClick(View widget)
                {
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            };
        }
    }

    public ClickableSpan clickableSignUpNavSpan()
    {
        {
            return new ClickableSpan()
            {
                @Override
                public void onClick(View widget)
                {
                    Intent intent = new Intent(LoginActivity.this, RegisterUserActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
        }
    }

    @NonNull
    @Contract(" -> new")
    private Object clickableLoginAsGuestNavSpan()
    {
        return new ClickableSpan()
        {
            @Override
            public void onClick(@NonNull View widget)
            {
                // fixme: the problem is that getCurrentUser(), should generate a new user
                guestUserID = UUID.randomUUID().toString();
                mAuth.signInAnonymously().addOnCompleteListener(onCompleteSignInAsGuestListener());
            }
        };
    }
    @NonNull
    @Contract(pure = true)
    private OnCompleteListener<AuthResult> onCompleteSignInAsGuestListener()
    {
        return task ->
        {
            if (task.isSuccessful())
            {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null)
                {
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(guestUserID)
                            .build());

                }

                Intent intent = new Intent(LoginActivity.this, GuestMainMenuActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "signInAnonymously:failure", task.getException());
            }
        };
    }
}