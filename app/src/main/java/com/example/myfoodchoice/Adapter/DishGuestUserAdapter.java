package com.example.myfoodchoice.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfoodchoice.AdapterInterfaceListener.OnDishClickListener;
import com.example.myfoodchoice.ModelCaloriesNinja.FoodItem;
import com.example.myfoodchoice.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DishGuestUserAdapter extends RecyclerView.Adapter<DishGuestUserAdapter.myViewHolder>
{
    private final List<FoodItem.Item> itemList;

    private final OnDishClickListener onDishClickListener;

    public DishGuestUserAdapter(List<FoodItem.Item> itemList, OnDishClickListener onDishClickListener)
    {
        this.itemList = itemList;
        this.onDishClickListener = onDishClickListener;
    }

    public static class myViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView foodImage;

        public TextView foodName;

        public Button removeBtn;

        public myViewHolder(final View itemView, OnDishClickListener onDishClickListener)
        {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            removeBtn = itemView.findViewById(R.id.removeBtn);

            removeBtn.setOnClickListener(v ->
            {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION)
                {
                    onDishClickListener.onClickDish(position);
                }
            });
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.dish_item_layout,
                parent, false);
        return new DishGuestUserAdapter.myViewHolder(itemView, onDishClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position)
    {
        FoodItem.Item item = itemList.get(position);
        if (item != null)
        {
            holder.foodName.setText(item.getName());
            Picasso.get()
                    .load(Uri.parse(item.getFoodImage()))
                    .resize(150, 150)
                    .error(R.drawable.error)
                    .into(holder.foodImage);
        }
        else
        {
            holder.foodName.setText("No name here");
            holder.foodImage.setImageResource(R.drawable.error);
            holder.removeBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount()
    {
        return itemList.size();
    }
}
