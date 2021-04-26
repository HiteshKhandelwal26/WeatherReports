package com.hitesh.weatherreport.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hitesh.weatherreport.R;
import com.hitesh.weatherreport.roomDB.entity.WeatherPOJO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

/*Class created to load the list of favorite cities marked by the user on Dashboard screen*/
public class FavCitiesAdapter extends RecyclerView.Adapter<FavCitiesAdapter.ViewHolder> {

    private final List<WeatherPOJO> citiesList;
    private final FavCitiesAdapter.ClickListener clickListener;

    public FavCitiesAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
        citiesList = new ArrayList<>();
    }

    @NonNull
    @Override
    public FavCitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavCitiesAdapter.ViewHolder holder, int position) {
        WeatherPOJO todo = citiesList.get(position);

        if (position == 0) {
            requireNonNull(holder).removeItem.setVisibility(View.GONE);
            holder.txtName.setText(String.format("%s (Current Locality)", todo.cityName.toUpperCase(Locale.US)));
        } else {
            holder.removeItem.setVisibility(View.VISIBLE);
            holder.txtName.setText(todo.cityName.toUpperCase(Locale.US));
        }
        holder.txtName.setOnClickListener(v -> clickListener.launchIntent(todo.cityName, position));
        holder.removeItem.setOnClickListener(view -> clickListener.removeItem(todo));
    }

    @Override
    public int getItemCount() {
        return citiesList.size();
    }

    /*Updated the list and notify*/
    public void updateTodoList(List<WeatherPOJO> data) {
        citiesList.clear();
        citiesList.addAll(data);
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void launchIntent(String cityName, int mPosition);

        void removeItem(WeatherPOJO ID);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName;
        public ImageView removeItem;

        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtNo);
            removeItem = view.findViewById(R.id.delItem);
        }
    }
}
