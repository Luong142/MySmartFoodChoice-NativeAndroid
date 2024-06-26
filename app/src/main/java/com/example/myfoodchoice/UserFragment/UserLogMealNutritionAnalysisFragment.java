package com.example.myfoodchoice.UserFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfoodchoice.AdapterInterfaceListener.OnDishClickListener;
import com.example.myfoodchoice.AdapterRecyclerView.DishGuestUserAdapter;
import com.example.myfoodchoice.ModelCaloriesNinja.FoodItem;
import com.example.myfoodchoice.ModelFreeFoodAPI.Dish;
import com.example.myfoodchoice.ModelNutrition.NutritionMeal;
import com.example.myfoodchoice.ModelSignUp.UserProfile;
import com.example.myfoodchoice.R;
import com.example.myfoodchoice.RetrofitProvider.CaloriesNinjaAPI;
import com.example.myfoodchoice.RetrofitProvider.FreeFoodDetailAPI;
import com.example.myfoodchoice.RetrofitProvider.RetrofitFreeFoodClient;
import com.example.myfoodchoice.RetrofitProvider.RetrofitNinjaCaloriesClient;
import com.example.myfoodchoice.UserActivity.UserMainMenuActivity;
import com.example.myfoodchoice.ml.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Contract;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLogMealNutritionAnalysisFragment extends Fragment implements OnDishClickListener
{
    final static String TAG = "UserLogMealNutritionFragment";
    private static final String PATH_ACCOUNT_TYPE = "Registered Accounts";
    int imageSize;

    // TODO: declare UI components
    DatabaseReference databaseReferenceUserProfile,
            databaseReferenceDailyFoodIntake,
            databaseReferenceDailyFoodIntakeChild;

    StorageReference storageReferenceFoodImage;

    StorageTask<UploadTask.TaskSnapshot> storageTask;

    UserProfile userProfile;
    ImageView foodImage;

    TextView cholesterolTextView, sugarTextView, saltTextView, caloriesTextView;

    TextView foodNameTextView, dietTypeTextView;

    ProgressBar loadingLogMeal, loadingAddDish;

    // TODO: add in one more button for taking photo I think.
    FloatingActionButton takePhotoBtn, uploadPhotoBtn;

    Button logMealBtn, addDishBtn;

    ActivityResultLauncher<Intent> uploadPhotoactivityResultLauncher, takePhotoActivityResultLauncher;

    ActivityResultLauncher<String[]> requestPermissionLauncher;

    // for image file

    Bitmap image;

    int dimension;

    // calling calories ninja API
    private CaloriesNinjaAPI caloriesNinjaAPI;

    private FreeFoodDetailAPI freeFoodDetailAPI;

    private FoodItem foodItem;
    FoodItem.Item itemDisplay;

    List<FoodItem.Item> foodItemsDisplay;

    // todo: init firebase
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;

    FirebaseStorage firebaseStorage;

    FirebaseUser firebaseUser;

    String userID, foodName, dietType;

    boolean isSeafoodAllergic, isPeanutAllergic, isEggAllergic;

    final static String PATH_USERPROFILE = "Android User Profile"; // FIXME: the path need to access the account.

    final static String PATH_DAILY_FOOD_INTAKE = "Android Meals"; // fixme:  the path need to access daily meal.

    NutritionMeal nutritionMeal;

    double totalCalories, totalCholesterol, totalSalt, totalSugar;

    RecyclerView dishRecyclerView;

    DishGuestUserAdapter dishGuestUserAdapter;
    private Uri selectedImageUri;

    Bundle bundle;

    StringBuilder caloriesMessage, cholesterolMessage, saltMessage, sugarMessage;

    double displayCalories, displaySalt, displaySugar, displayCholesterol;

    // todo: this dialog will appear
    private Dish dishInfo;

    private List<String> strIngredients; // todo: our algo is to add in every single ingredient

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // TODO: init Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance
                ("https://myfoodchoice-dc7bd-default-rtdb.asia-southeast1.firebasedatabase.app/");

        firebaseStorage = FirebaseStorage.getInstance();

        // TODO: init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // TODO: init user id
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            userID = firebaseUser.getUid();

            // TODO: init database reference for user profile
            databaseReferenceUserProfile =
                    firebaseDatabase.getReference(PATH_USERPROFILE).child(userID);

            databaseReferenceDailyFoodIntake =
                    firebaseDatabase.getReference(PATH_DAILY_FOOD_INTAKE).child(userID);

            databaseReferenceUserProfile.addValueEventListener(onHealthUserProfileListener());

            storageReferenceFoodImage = firebaseStorage.getReference()
                    .child("Food Images")
                    .child(userID);
        }
        // set the food item in the Meal object
        // todo: test the meal object
        bundle = getArguments();
        if (bundle != null)
        {
            nutritionMeal = bundle.getParcelable("meal");
        }

        foodItem = new FoodItem();
        if (nutritionMeal != null)
        {
            nutritionMeal.setDishes(foodItem);
        }

        itemDisplay = new FoodItem.Item();

        // init nutrition value to 0
        totalCalories = 0;
        totalCholesterol = 0;
        totalSalt = 0;
        totalSugar = 0;

        // init message
        caloriesMessage = new StringBuilder();
        cholesterolMessage = new StringBuilder();
        saltMessage = new StringBuilder();
        sugarMessage = new StringBuilder();

        // TODO: init UI components
        foodNameTextView = view.findViewById(R.id.foodName);

        // fixme: should be matched with the ID.


        // todo: init text view for nutrition value
        caloriesTextView = view.findViewById(R.id.caloriesTextView);
        cholesterolTextView = view.findViewById(R.id.cholesterolTextView);
        saltTextView = view.findViewById(R.id.sodiumTextView);
        sugarTextView = view.findViewById(R.id.sugarTextView);
        dietTypeTextView = view.findViewById(R.id.dietType);

        takePhotoBtn = view.findViewById(R.id.takePhotoBtn);
        uploadPhotoBtn = view.findViewById(R.id.uploadPhotoBtn);

        // todo: this is actually a card view.
        logMealBtn = view.findViewById(R.id.logMealBtn);
        loadingLogMeal = view.findViewById(R.id.loadingLogMeal);
        loadingAddDish = view.findViewById(R.id.loadingAddDish);

        // set visibility
        loadingAddDish.setVisibility(ProgressBar.GONE);
        loadingLogMeal.setVisibility(ProgressBar.GONE);

        // set on click
        logMealBtn.setOnClickListener(onNavToLogMealListener());

        // init add dish btn card view
        addDishBtn = view.findViewById(R.id.addDishBtn);
        addDishBtn.setOnClickListener(onAddDishListener());

        // init and set recycler view
        foodItemsDisplay = new ArrayList<>();
        dishRecyclerView = view.findViewById(R.id.dishRecyclerView);
        dishGuestUserAdapter = new DishGuestUserAdapter(foodItemsDisplay, this);
        setAdapter();
        dishRecyclerView.setVerticalScrollBarEnabled(true);

        foodImage = view.findViewById(R.id.foodPhoto);

        // todo: set onclick here
        uploadPhotoBtn.setOnClickListener(onUploadPhotoListener());
        takePhotoBtn.setOnClickListener(onTakePhotoListener());

        // todo: init API for two of them
        freeFoodDetailAPI = RetrofitFreeFoodClient.getRetrofitFreeInstance().create(FreeFoodDetailAPI.class);
        caloriesNinjaAPI = RetrofitNinjaCaloriesClient.getRetrofitNinjaInstance().create(CaloriesNinjaAPI.class);

        imageSize = 224; // important?

        // for camera activity
        takePhotoActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), onTakePhotoActivityLauncher());

        // for upload photo
        uploadPhotoactivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), onUploadPhotoActivityLauncher());

        // for permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), onPermissionLauncher());

    }

    @NonNull
    @Contract(" -> new")
    private ValueEventListener onHealthUserProfileListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // todo: our plan is to find a correct API to warn if the food contains what allergies to user,
                //  and diet type
                userProfile = snapshot.getValue(UserProfile.class);

                if (userProfile == null)
                {
                    // Log.d(TAG, "onDataChange: userProfile is null");
                    return;
                }

                dietType = userProfile.getDietType();

                isSeafoodAllergic = userProfile.isAllergySeafood();

                isPeanutAllergic = userProfile.isAllergyPeanut();

                isEggAllergic = userProfile.isAllergyEgg();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        };
    }

    @NonNull
    @Contract(" -> new")
    private Callback<Dish> callBackDishInfoResponseFromAPI()
    {
        return new Callback<Dish>()
        {
            @Override
            public void onResponse(@NonNull Call<Dish> call, @NonNull Response<Dish> response)
            {
                if (response.isSuccessful())
                {
                    // todo: use dishInfo to alert user if they have health problems.
                    dishInfo = response.body();
                    // Log.d(TAG, "onResponse: " + dishInfo);

                    if (dishInfo == null)
                    {
                        Log.d(TAG, "onResponse: dishInfo is null");
                        return;
                    }

                    if (dishInfo.getMeals().isEmpty())
                    {
                        Log.d(TAG, "onResponse: dishInfo.getMeals() is empty");
                        return;
                    }

                    strIngredients = dishInfo.getMeals().stream()
                            .flatMap(meal -> Stream.of
                                    (
                                    meal.getStrIngredient1(), meal.getStrIngredient2(), meal.getStrIngredient3(),
                                    meal.getStrIngredient4(), meal.getStrIngredient5(), meal.getStrIngredient6(),
                                    meal.getStrIngredient7(), meal.getStrIngredient8(), meal.getStrIngredient9(),
                                    meal.getStrIngredient10(), meal.getStrIngredient11(), meal.getStrIngredient12(),
                                    meal.getStrIngredient13(), meal.getStrIngredient14(), meal.getStrIngredient15(),
                                    meal.getStrIngredient16(), meal.getStrIngredient17(), meal.getStrIngredient18(),
                                    meal.getStrIngredient19(), meal.getStrIngredient20()
                                    ))
                            .filter(Objects::nonNull) // if null attribute then filter out.
                            .filter(strIngredient -> !strIngredient.isEmpty()) // if empty then filter out.
                            .distinct() // if duplicate then filter out.
                            .collect(Collectors.toList());

                    // todo: testing done
                    // Log.d(TAG, "onResponse: " + strIngredients);

                    /*
                    allergiesArrayList.add(new Allergies("Gluten", R.drawable.gluten_free));
                    allergiesArrayList.add(new Allergies("Dairy", R.drawable.dairy));
                    allergiesArrayList.add(new Allergies("Egg", R.drawable.egg));
                    allergiesArrayList.add(new Allergies("Seafood", R.drawable.fish));
                    allergiesArrayList.add(new Allergies("Peanut", R.drawable.peanut));

                    // FIXME: edamam can't be used.
                    dietTypeArrayList.add(new UserProfile("Vegetarian", R.drawable.vege));
                    dietTypeArrayList.add(new UserProfile("Non-Vegetarian", R.drawable.non_vege));
                    */

                    // here is the check
                    /*
                    for (String ingredient : strIngredients)
                    {
                        Log.d(TAG, "onResponse: ingredient: " + ingredient);
                    }
                     */
                    checkAllergiesAndDietType(strIngredients);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Dish> call, @NonNull Throwable t)
            {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        };
    }

    private void checkAllergiesAndDietType(@NonNull List<String> strIngredients)
    {
        // todo: update and add in more ingredient that identify these two info.
        // todo: retrain the model based on whether both website accept the input or name of food
        // fixme: https://www.themealdb.com/api/json/v1/1/search.php?s=Fish%20Soup
        // fixme: https://calorieninjas.com/

        // todo: remember to update those lists below as new model
        // fixme: all checking ingredients must be in lower case.

        List<String> allergyEggList = Arrays.asList("eggs", "egg",
                "egg white", "egg yolks", "omelette", "scrambled eggs", "fried eggs",
                "egg salad", "egg yolk");

        List<String> allergyPeanutList = Arrays.asList("peanuts",
                "peanut", "peanut butter", "peanut oil", "peanut flour",
                "peanut sauce", "peanut butter cookies", "peanut brittle");

        List<String> allergySeafoodList = Arrays.asList("lobster",
                "fish", "crustacean", "shellfish", "anchovy fillet",
                "fish stock", "king prawns", "fish sauce", "shrimp", "crab", "oysters",
                "mussels", "clams", "scallop", "octopus", "squid", "sea bass",
                "salmon", "tuna", "trout", "halibut", "cod", "snapper", "tilapia",
                "flounder", "mackerel", "sardines", "anchovies", "caviar", "truffles",
                "game meat");

        List<String> nonVegeList = Arrays.asList(
                "meat", "chicken", "beef", "lamb", "turkey", "pork", "ham", "sausage", "duck", "mutton", "venison",
                "anchovy fillet", "fish stock", "king prawns", "fish sauce", "lobster", "fish", "crustacean", "shellfish",
                "egg", "bacon", "steak", "ribs", "brisket", "spareribs", "hot dogs",
                "sausages", "burgers", "patties",
                "salmon", "tuna", "mussels", "clams", "oysters",
                "scallops", "shrimp", "prawns", "crab", "lobster",
                "snapper", "tilapia", "cod", "halibut", "trout", "catfish",
                "flounder", "mackerel", "sardines", "anchovies",
                "caviar", "truffles", "game meat", "beef fillet"
        );

        String allergen;
        // todo:
        /*
        Log.d(TAG, "allergic egg here: " + isEggAllergic);

        Log.d(TAG, "allergic seafood here: " + isSeafoodAllergic);

        Log.d(TAG, "allergic peanut here: " + isPeanutAllergic);
         */
        // fixme: not show
        if (isEggAllergic)
        {
            allergen = checkForAllergyMatch(strIngredients, allergyEggList);
            if (!allergen.equals("No allergens found in the ingredients."))
            {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Cautious")
                        .setMessage(String.format("This dish contains %s ingredient " +
                                        "which is allergic to your health.",
                                allergen))
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
        if (isSeafoodAllergic)
        {
            allergen = checkForAllergyMatch(strIngredients, allergySeafoodList);
            if (!allergen.equals("No allergens found in the ingredients."))
            {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Cautious")
                        .setMessage(String.format("This dish contains %s ingredient " +
                                        "which is allergic to your health.",
                                allergen))
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
        if (isPeanutAllergic)
        {
            allergen = checkForAllergyMatch(strIngredients, allergyPeanutList);
            if (!allergen.equals("No allergens found in the ingredients."))
            {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Cautious")
                        .setMessage(String.format("This dish contains %s ingredient " +
                                        "which is allergic to your health.",
                                allergen))
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }

        // todo: done.
        boolean isVegetarian = strIngredients.stream()
                .map(String::toLowerCase)
                .noneMatch(nonVegeList::contains);

        dietTypeTextView.setText(isVegetarian ? "Vegetarian Food" : "Non-Vegetarian Food");
    }

    private String checkForAllergyMatch(@NonNull List<String> strIngredients,
                                        @NonNull List<String> allergyList)
    {
        List<String> lowerCaseIngredients = strIngredients.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for (String allergy : allergyList)
        {
            if (lowerCaseIngredients.contains(allergy))
            {
                return allergy;
            }
        }
        return "No allergens found in the ingredients.";
    }


    @NonNull
    @Contract(" -> new")
    private Callback<FoodItem> callBackNutritionValueResponseFromAPI()
    {
        return new Callback<FoodItem>()
        {
            @Override
            public void onResponse(@NonNull Call<FoodItem> call, @NonNull Response<FoodItem> response)
            {
                if (response.isSuccessful())
                {
                    foodItem = response.body();
                    if (foodItem != null)
                    {
                        // assign the variable to the is foodItems array list.

                        // FIXME: sometime the API doesn't give response.
                        // Log.d(TAG, "onResponse: " + foodItem);
                        // todo: set progress bar here
                        // get all total calculations
                        for (FoodItem.Item itemLoop : foodItem.getItems())
                        {
                            totalCalories += itemLoop.getCalories();
                            totalCholesterol += itemLoop.getCholesterol_mg();
                            totalSalt += itemLoop.getSodium_mg();
                            totalSugar += itemLoop.getSugar_g();

                            // todo: set the item.
                            itemDisplay = itemLoop;
                            itemDisplay.setFoodImage(selectedImageUri.toString());


                            // fixme: remember must be unique name if not will be override
                            String uniqueImageName = firebaseUser.getDisplayName() + "_" +
                                    UUID.randomUUID() + ".jpg";

                            // upload the image to Firebase Storage
                            final StorageReference storageReference = storageReferenceFoodImage.child
                                    (uniqueImageName); // FIXME: potential bug.

                            // Log.d(TAG, "onResponse: " + selectedImageUri);

                            // todo: set storage task here, there is a bug here.
                            // fixme: not stable to upload the file, if fail then the image is content instead of
                            // URL format
                            storageTask = storageReference.putFile(selectedImageUri).
                                    addOnFailureListener(onFailureUploadFoodImage());

                            // set image here
                            storageTask.continueWithTask(task ->
                            {
                                if (!task.isSuccessful())
                                {
                                    throw Objects.requireNonNull(task.getException());
                                }
                                return storageReference.getDownloadUrl();
                            }).addOnCompleteListener(onCompleteUploadUriListener());
                        }

                        // todo: set the total calories first.
                        nutritionMeal.setTotalCalories(totalCalories);
                        nutritionMeal.setTotalCholesterol(totalCholesterol);
                        nutritionMeal.setTotalSodium(totalSalt);
                        nutritionMeal.setTotalSugar(totalSugar);

                        // Log.d(TAG, String.valueOf(nutritionMeal.getTotalCalories()));

                        // update the individual nutrition value
                        updateDishNutritionUI();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodItem> call, @NonNull Throwable t)
            {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        };
    }

    @NonNull
    @Contract(" -> new")
    private OnCompleteListener<Uri> onCompleteUploadUriListener()
    {
        return task ->
        {
            if (task.isSuccessful())
            {
                Uri downloadUri = task.getResult();
                itemDisplay.setFoodImage(downloadUri.toString());
            }
            else
            {
                Log.d(TAG, "onCompleteUploadListener: " + task.getException());
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private OnFailureListener onFailureUploadFoodImage()
    {
        return e ->
                Log.d(TAG, "onFailureUploadFoodImage: " + e.getMessage());
    }

    private void updateDishNutritionUI()
    {
        // to display individually
        displayCalories = totalCalories;
        displayCholesterol = totalCholesterol;
        displaySalt = totalSalt;
        displaySugar = totalSugar;
        // foodItems.add(itemLoop);

        // todo: set text
        caloriesMessage
                .append(displayCalories)
                .append(" kcal");
        caloriesTextView.setText(caloriesMessage.toString());

        cholesterolMessage
                .append(displayCholesterol)
                .append(" mg");
        cholesterolTextView.setText(cholesterolMessage.toString());

        saltMessage
                .append(displaySalt)
                .append(" mg");
        saltTextView.setText(saltMessage.toString());

        sugarMessage
                .append(displaySugar)
                .append(" g");
        sugarTextView.setText(sugarMessage.toString());

        // reset string builder
        caloriesMessage.setLength(0);
        cholesterolMessage.setLength(0);
        saltMessage.setLength(0);
        sugarMessage.setLength(0);

        // reset the value for one dish only
        displayCalories = 0;
        displaySalt = 0;
        displaySugar = 0;
        displayCholesterol = 0;
        totalCalories = 0;
        totalSalt = 0;
        totalSugar = 0;
        totalCholesterol = 0;
    }

    public void classifyImage(@NonNull Bitmap image)// todo: algo using tensorflow lite to label image.
    {
        // todo: Tensorflow lite is an API.
        try
        {
            // model here
            Model model = Model.newInstance(requireActivity().getApplicationContext());

            // holding memory of that new image.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize
                    (new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            // fixme, @NonNull Bitmap image since the I have converted it into image.
            // fixme, Bitmap supports JPEG, PNG, BMP, GIF, WebP except uncommon image format
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * imageSize  * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // what is this code about, using double for loop to put the float value inside of that byteBuffer.
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++)
            {
                for (int j = 0; j < imageSize; j++)
                {
                    int val = intValues[pixel++];
                    // todo: predefined value to normalize the extraction of RGB.

                    // todo: RED
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));

                    // todo: GREEN
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));

                    // todo: BLUE
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++)
            {
                if(confidences[i] > maxConfidence)
                {
                    // todo: pls note that confidence is currently not being used, only maxPos.
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            // todo: add more dishes here, based on the model, we train more food on another model.
            String[] classes = {"Beef wellington",
                    "Nasi Lemak", "Omelette",
                    "Fish Soup", "Tart", "Laksa"};

            // result.setText(classes[maxPos]);
            // todo: need to test image recognition algo.
            foodName = classes[maxPos];
            // Log.d(TAG, "The dish name is classified as: " + foodName);
            foodNameTextView.setText(foodName);

            // call API, and get result with that model class.
            Call<FoodItem> call = caloriesNinjaAPI.getFoodItem(foodName);
            // todo: uncomment this part below to do get calories info and more from this API.
            call.enqueue(callBackNutritionValueResponseFromAPI());

            // todo: call free food API for allergies and diet type.
            String modifiedName; // todo: remember to add in case for this.
            switch (foodName)
            {
                case ("Nasi Lemak"):
                    modifiedName = "Nasi lemak";
                    break;
                case ("Fish Soup"):
                    modifiedName = "Fish Soup";
                    break;
                case ("Tart"):
                    modifiedName = "Tart";
                    break;
                case ("Laksa"):
                    modifiedName = "Laksa";
                    break;
                case ("Omelette"):
                    modifiedName = "Omelette";
                    break;
                case ("Beef Wellington"):
                    modifiedName = "Beef Wellington";
                    break;
                default:
                    modifiedName = foodName;
            }

            Call<Dish> dishCall = freeFoodDetailAPI.searchMealByName(modifiedName);
            dishCall.enqueue(callBackDishInfoResponseFromAPI());

            // todo: input from user when search for recipe,
            // todo: if the "ingredients" contains the "allergies", we can show warning contains "nuts" to user, best option.
            // todo: 3 options

            StringBuilder s = new StringBuilder();
            for(int i = 0; i < classes.length; i++)
            {
                s.append(String.format(Locale.ROOT, "%s: %.1f%%\n",
                        classes[i], confidences[i] * 100));
            }
            // confidence.setText(s);

            // free the model to release memory.
            model.close();
        }
        catch (IOException e)
        {
            // TODO Handle the exception
            Log.d(TAG, "ClassifyImage: " + e.getMessage());
        }
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onAddDishListener()
    {
        return v ->
        {
            if (itemDisplay.getName() == null || itemDisplay.getFoodImage() == null)
            {
                Toast.makeText(requireContext(), "Please select a dish", Toast.LENGTH_SHORT).show();
                return;
            }

            addDishBtn.setVisibility(Button.GONE);
            loadingAddDish.setVisibility(ProgressBar.VISIBLE);

            // fixme: there might be a problem with the dish
            Toast.makeText(requireContext(), "Dish is added.", Toast.LENGTH_SHORT).show();

            // ensure the list is initialized before adding an item
            if (nutritionMeal.getDishes().getItems() == null)
            {
                nutritionMeal.getDishes().setItems(new ArrayList<>());
            }

            // this object for the next activity to record.
            if (nutritionMeal.getDishes().getItems().add(itemDisplay))
            {
                addDishBtn.setVisibility(Button.VISIBLE);
                loadingAddDish.setVisibility(ProgressBar.GONE);
            }

            // this one is for adapter which means for UI to show.
            foodItemsDisplay.add(itemDisplay);

            // fixme: there is another problem that the food item can be duplicated.
            // Log.d(TAG, "onAddDishListener: " + foodItems);
            dishGuestUserAdapter.notifyItemInserted(foodItemsDisplay.size() - 1);
        };
    }

    @Override
    public void onRemoveDish(int position) // todo: remove the element
    {
        // todo: to remove the dish from the list.
        if (nutritionMeal.getDishes().getItems() == null)
        {
            nutritionMeal.getDishes().setItems(new ArrayList<>());
        }

        // this object for the next activity to record.
        nutritionMeal.getDishes().getItems().remove(position);

        // this one is for adapter which means for UI to show.
        foodItemsDisplay.remove(position);

        dishGuestUserAdapter.notifyItemRemoved(position);
    }

    @NonNull
    @Contract(" -> new")
    private View.OnClickListener onTakePhotoListener()
    {
        return v ->
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            }
            else
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // todo: test this later.
                FragmentActivity fragmentActivity = getActivity();

                if (fragmentActivity != null)
                {
                    if (cameraIntent.resolveActivity(fragmentActivity.getPackageManager()) != null)
                    {
                        takePhotoActivityResultLauncher.launch(cameraIntent);
                    }
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true) // for log meal button
    private View.OnClickListener onNavToLogMealListener()
    {
        return v ->
        {
            StringBuilder message = new StringBuilder();

            if (nutritionMeal.getDishes() == null || nutritionMeal.getDishes().getItems() == null ||
                    nutritionMeal.getDishes().getItems().isEmpty())
            {
                message.append("Dish is required to be added before logging your meal.");
                Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            if (nutritionMeal.getDishes().getItems().size() > 3)
            {
                message.append("You can only add up to 3 dishes.");
                Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            if (foodItemsDisplay.isEmpty())
            {
                message.append("Dish is required to be added before logging your meal.");
                Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            logMealBtn.setVisibility(Button.GONE);
            loadingLogMeal.setVisibility(ProgressBar.VISIBLE);

            // todo: set the time stamp for meal
            nutritionMeal.startDate();

            // todo: push the data in firebase
            databaseReferenceDailyFoodIntakeChild = databaseReferenceDailyFoodIntake.push();

            nutritionMeal.setKey(databaseReferenceDailyFoodIntakeChild.getKey());
            // fixme: testing
            //Log.d(TAG, "onNavToLogMealListener: " + meal);
            //Log.d(TAG, "onNavToLogMealListener: " + formatTime(meal.getDate()));

            // todo: set total nutrition value
            databaseReferenceDailyFoodIntakeChild.setValue(nutritionMeal).addOnCompleteListener(onCompleteLogMealListener());
        };
    }

    @NonNull
    @Contract(pure = true)
    private OnCompleteListener<Void> onCompleteLogMealListener()
    {
        return task ->
        {
            if (task.isSuccessful())
            {
                Toast.makeText(requireContext(), "Logged your meal.", Toast.LENGTH_LONG).show();
                bundle.putParcelable("meal", nutritionMeal);

                logMealBtn.setVisibility(Button.VISIBLE);
                loadingLogMeal.setVisibility(ProgressBar.GONE);

                // todo: go to the home page.
                Intent intent = new Intent(requireContext(), UserMainMenuActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(requireContext(), "Error logging your meal.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "onCompleteLogMealListener: " + task.getException());
            }
        };
    }

    private void setAdapter()
    {
        // set the adapter
        RecyclerView.LayoutManager layoutManager = new
                LinearLayoutManager(requireActivity().getApplicationContext());
        dishRecyclerView.setLayoutManager(layoutManager);
        dishRecyclerView.setItemAnimator(new DefaultItemAnimator());
        dishRecyclerView.setAdapter(dishGuestUserAdapter);
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<Map<String, Boolean>> onPermissionLauncher()
    {
        return permissions ->
        {
            if (Boolean.TRUE.equals(permissions.get(Manifest.permission.CAMERA)))
            {
                // Permission granted, can now start the camera intent
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // todo: test this later
                FragmentActivity fragmentActivity = getActivity();

                if (fragmentActivity != null)
                {
                    if (cameraIntent.resolveActivity(fragmentActivity.getPackageManager()) != null)
                    {
                        takePhotoActivityResultLauncher.launch(cameraIntent);
                    }
                }
            }
            else
            {
                // denied, show toast
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<ActivityResult> onTakePhotoActivityLauncher()
    {
        return result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                Intent data = result.getData();
                if (data != null && data.getData() != null)
                {
                    selectedImageUri = data.getData();
                    Bundle extras = data.getExtras();
                    if (extras != null)
                    {
                        image = (Bitmap) extras.get("data");
                        if (image != null)
                        {
                            dimension = Math.min(image.getWidth(), image.getHeight());
                            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                            // Set the Bitmap to the ImageView
                            foodImage.setImageBitmap(image);

                            // I don't know what is this?
                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                            classifyImage(image);
                        }
                    }
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<ActivityResult> onUploadPhotoActivityLauncher()
    {
        return result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                Intent data = result.getData();
                if (data != null && data.getData() != null)
                {
                    selectedImageUri = data.getData();
                    try {
                        // Decode the URI to a Bitmap
                        image = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),
                                selectedImageUri);
                        if (image != null)
                        {
                            // Set dimension
                            dimension = Math.min(image.getWidth(), image.getHeight());
                            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                            // Set the Bitmap to the ImageView
                            foodImage.setImageBitmap(image);

                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                            classifyImage(image);
                        }
                    }
                    catch (IOException e)
                    {
                        Log.d(TAG, "error here: " + e);
                    }
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onUploadPhotoListener()
    {
        return v ->
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            uploadPhotoactivityResultLauncher.launch(Intent.createChooser(intent, "Select File"));
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_log_meal_nutrition_analysis, container, false);
    }
}
