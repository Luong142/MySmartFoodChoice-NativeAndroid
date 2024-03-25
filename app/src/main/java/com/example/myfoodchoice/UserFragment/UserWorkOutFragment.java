package com.example.myfoodchoice.UserFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfoodchoice.Adapter.WorkoutPlanAdapter;
import com.example.myfoodchoice.AdapterInterfaceListener.OnWorkoutPlanClickListener;
import com.example.myfoodchoice.ModelBusiness.WorkoutPlan;
import com.example.myfoodchoice.R;

import java.util.ArrayList;


public class UserWorkOutFragment extends Fragment implements OnWorkoutPlanClickListener
{

    // TODO: declare components
    RecyclerView workoutRecyclerView;

    WorkoutPlanAdapter workoutPlanAdapter;

    private ArrayList<WorkoutPlan> workoutPlans;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // TODO: init firebase components.



        // TODO: init UI components


        // Initialize the recipeList
        workoutPlans = new ArrayList<>();
        populateWorkoutList();

        // for init recycle view component
        workoutRecyclerView = view.findViewById(R.id.workoutRecyclerView);
        workoutPlanAdapter = new WorkoutPlanAdapter(workoutPlans, this);
        setAdapter();
        workoutRecyclerView.setVerticalScrollBarEnabled(true);

        // Set the adapter to the RecyclerView
        // recipeRecyclerView.setAdapter(adapter);

        // TODO: Populate the recipeList with your Recipe data


        // Notify the adapter that the data has changed
        // recipeItemAdapter.notifyDataSetChanged();

    }

    @Override
    public void onWorkoutPlanClick(int position)
    {
        // TODO: implement onClick
        Toast.makeText(getContext(), "pls update this next", Toast.LENGTH_SHORT).show();
    }

    private void setAdapter()
    {
        // set the adapter
        RecyclerView.LayoutManager layoutManager = new
                LinearLayoutManager(requireActivity().getApplicationContext());
        workoutRecyclerView.setLayoutManager(layoutManager);
        workoutRecyclerView.setItemAnimator(new DefaultItemAnimator());
        workoutRecyclerView.setAdapter(workoutPlanAdapter);
    }

    private void populateWorkoutList()
    {
        workoutPlans.add(new WorkoutPlan("Plan 1", "Push up", 10));
        workoutPlans.add(new WorkoutPlan("Plan 1", "Push up", 10));
        workoutPlans.add(new WorkoutPlan("Plan 1", "Push up", 10));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_work_out, container, false);
    }
}