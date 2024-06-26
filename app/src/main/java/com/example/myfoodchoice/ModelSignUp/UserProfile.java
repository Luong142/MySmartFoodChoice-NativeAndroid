package com.example.myfoodchoice.ModelSignUp;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.myfoodchoice.ModelUtilities.Reward;
import com.example.myfoodchoice.R;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class UserProfile extends CommonProfile implements Parcelable // way more efficient to use Parcelable.
{
    private String key;
    private String height;

    private String weight;

    private String gender;
    private int age;

    private int points;

    private String dietType; // for diet to recommend

    private int dietTypeImage;

    private ArrayList<Reward> alrRedeemedRewardList;

    private boolean isHighBloodPressure;

    private boolean isDiabetes;

    private boolean isHighCholesterol;

    private boolean isAllergySeafood;

    private boolean isAllergyPeanut;

    private boolean isAllergyEgg;

    public UserProfile()
    {
        // this constructor is required for Firebase to be able to deserialize the object
        super();

        // default value
        height = String.valueOf(50);
        weight = String.valueOf(50);
        gender = "Male";
        age = 20;
        dietType = "Vegetarian";
        dietTypeImage = R.drawable.vege;
        isAllergyEgg = false;
        isAllergyPeanut = false;
        isAllergySeafood = false;
        isDiabetes = false;
        isHighBloodPressure = false;
        isHighCholesterol = false;
        points = 100; // put the default point here
        alrRedeemedRewardList = new ArrayList<>();
    }

    protected UserProfile(@NonNull Parcel in)
    {
        super();
        key = in.readString();
        setFirstName(in.readString());
        setLastName(in.readString());
        height = in.readString();
        weight = in.readString();
        setProfileImageUrl(in.readString());
        gender = in.readString();
        age = in.readInt();
        dietType = in.readString();
        points = in.readInt();
        // activityLevel = (ActivityLevel) in.readSerializable();
        isHighBloodPressure = in.readByte() != 0;
        isDiabetes = in.readByte() != 0;
        isHighCholesterol = in.readByte() != 0;
        isAllergySeafood = in.readByte() != 0;
        isAllergyPeanut = in.readByte() != 0;
        isAllergyEgg = in.readByte() != 0;
        alrRedeemedRewardList = new ArrayList<>();
    }

    public UserProfile(String dietType, int dietTypeImage)
    {
        this.dietType = dietType;
        this.dietTypeImage = dietTypeImage;
    }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>()
    {
        @NonNull
        @Contract("_ -> new")
        @Override
        public UserProfile createFromParcel(Parcel in) {
            return new UserProfile(in);
        }

        @NonNull
        @Contract(value = "_ -> new", pure = true)
        @Override
        public UserProfile[] newArray(int size) {
            return new UserProfile[size];
        }
    };

    @Override
    public int describeContents()
    {
        return  0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags)
    {
        dest.writeString(key);
        dest.writeString(getFirstName());
        dest.writeString(getLastName());
        dest.writeString(height);
        dest.writeString(weight);
        dest.writeString(getProfileImageUrl());
        dest.writeString(gender);
        dest.writeInt(age);
        dest.writeString(dietType);
        dest.writeInt(points);
        // dest.writeSerializable(activityLevel);
        dest.writeByte((byte) (isHighBloodPressure ? 1 : 0));
        dest.writeByte((byte) (isDiabetes ? 1 : 0));
        dest.writeByte((byte) (isHighCholesterol ? 1 : 0));
        dest.writeByte((byte) (isAllergySeafood ? 1 : 0));
        dest.writeByte((byte) (isAllergyPeanut ? 1 : 0));
        dest.writeByte((byte) (isAllergyEgg ? 1 : 0));
        dest.writeList(alrRedeemedRewardList);
    }

    public String getFullUserDetail()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Height: ").append(this.height).append("\n");
        sb.append("Weight: ").append(this.weight).append("\n");
        sb.append("Gender: ").append(this.gender).append("\n");
        sb.append("Age: ").append(this.age).append("\n");
        sb.append("Diet Type: ").append(this.dietType).append("\n\n");
        sb.append("Health Conditions Summary:\n");
        sb.append("---------------------------\n");
        sb.append("High Blood Pressure: ").append(isHighBloodPressure ? "Yes" : "No").append("\n");
        sb.append("Diabetes: ").append(isDiabetes ? "Yes" : "No").append("\n");
        sb.append("High Cholesterol: ").append(isHighCholesterol ? "Yes" : "No").append("\n");
        sb.append(" Seafood: ").append(isAllergySeafood ? "Yes" : "No").append("\n");
        sb.append(" Peanut: ").append(isAllergyPeanut ? "Yes" : "No").append("\n");
        sb.append(" Egg: ").append(isAllergyEgg ? "Yes" : "No").append("\n");
        sb.append("---------------------------\n");
        return sb.toString();
    }

    public String getUserProfileDetail()
    {
        String sb = "Full Name\n" +
                this.getFirstName() + " " + this.getLastName() + "\n" +
                "Gender: " + this.gender + "\n" +
                "Age: " + this.age + "\n"
                + "Weight: " + this.weight;
        return sb;
    }

    public String getUserHealthDetail()
    {
        String sb = "Health Conditions Summary:\n" +
                "---------------------------\n" +
                "Height: " + this.height + "\n" +
                "Weight: " + this.weight + "\n" +
                "Diet Type: " + this.dietType + "\n" +
                "High Blood Pressure: " + (isHighBloodPressure ? "Yes" : "No") + "\n" +
                "Diabetes: " + (isDiabetes ? "Yes" : "No") + "\n" +
                "High Cholesterol: " + (isHighCholesterol ? "Yes" : "No") + "\n" +
                " Seafood: " + (isAllergySeafood ? "Yes" : "No") + "\n" +
                " Peanut: " + (isAllergyPeanut ? "Yes" : "No") + "\n" +
                " Egg: " + (isAllergyEgg ? "Yes" : "No") + "\n" +
                "---------------------------";
        return sb;
    }

    @NonNull
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("User Profile\n");
        sb.append("-------------------\n");
        sb.append("First Name: ").append(getFirstName()).append("\n");
        sb.append("Last Name: ").append(getLastName()).append("\n");
        sb.append("Height: ").append(height).append(" cm\n");
        sb.append("Weight: ").append(weight).append(" kg\n");
        sb.append("Gender: ").append(gender).append("\n");
        sb.append("Age: ").append(age).append("\n");
        sb.append("Diet Type: ").append(dietType).append("\n");
        sb.append("High Blood Pressure: ").append(isHighBloodPressure).append("\n");
        sb.append("Diabetes: ").append(isDiabetes).append("\n");
        sb.append("High Cholesterol: ").append(isHighCholesterol).append("\n");
        // sb.append("Activity Level: ").append(activityLevel).append("\n");
        sb.append("Allergy Seafood: ").append(isAllergySeafood).append("\n");
        sb.append("Allergy Peanut: ").append(isAllergyPeanut).append("\n");
        sb.append("Allergy Egg: ").append(isAllergyEgg).append("\n");
        sb.append("Points: ").append(points).append("\n");
        for (Reward reward : alrRedeemedRewardList)
        {
            sb.append("Ok should be here: ").append(reward.toString()).append("\n");
        }
        sb.append("-------------------");
        return sb.toString();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<Reward> getAlrRedeemedRewardList() {
        return alrRedeemedRewardList;
    }

    public void setAlrRedeemedRewardList(ArrayList<Reward> alrRedeemedRewardList) {
        this.alrRedeemedRewardList = alrRedeemedRewardList;
    }

    public boolean isAllergySeafood() {
        return isAllergySeafood;
    }

    public void setAllergySeafood(boolean allergySeafood) {
        isAllergySeafood = allergySeafood;
    }

    public boolean isAllergyPeanut() {
        return isAllergyPeanut;
    }

    public void setAllergyPeanut(boolean allergyPeanut) {
        isAllergyPeanut = allergyPeanut;
    }

    public boolean isAllergyEgg() {
        return isAllergyEgg;
    }

    public void setAllergyEgg(boolean allergyEgg) {
        isAllergyEgg = allergyEgg;
    }

    public boolean isHighBloodPressure() {
        return isHighBloodPressure;
    }

    public void setHighBloodPressure(boolean highBloodPressure) {
        this.isHighBloodPressure = highBloodPressure;
    }

    public boolean isDiabetes() {
        return isDiabetes;
    }

    public void setDiabetes(boolean diabetes) {
        this.isDiabetes = diabetes;
    }

    public boolean isHighCholesterol() {
        return isHighCholesterol;
    }

    public void setHighCholesterol(boolean highCholesterol) {
        this.isHighCholesterol = highCholesterol;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getDietTypeImage() {
        return dietTypeImage;
    }

    public void setDietTypeImage(int dietTypeImage) {
        this.dietTypeImage = dietTypeImage;
    }


    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }
}
