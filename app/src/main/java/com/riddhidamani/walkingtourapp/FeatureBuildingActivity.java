package com.riddhidamani.walkingtourapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.riddhidamani.walkingtourapp.databinding.ActivityFeatureBuildingBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FeatureBuildingActivity extends AppCompatActivity {

    private static final String TAG = "FeatureBuildingActivity";
    private ActivityFeatureBuildingBinding binding;
    private Typeface textFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_feature_building);

        binding = ActivityFeatureBuildingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("");

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.home_image);
        }

        textFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        binding.buildingName.setTypeface(textFont);
        binding.buildingAddress.setTypeface(textFont);
        binding.buildingDescription.setTypeface(textFont);

        GeoFenceData fenceData = (GeoFenceData) getIntent().getSerializableExtra("DATA");

        if (fenceData != null) {
            binding.buildingName.setText(fenceData.getId());
            binding.buildingAddress.setText(fenceData.getAddress());
            String description = fenceData.getDescription();
            binding.buildingDescription.setText(fenceData.getDescription());
            loadImagePicasso(binding.imageView, fenceData.getImageURL());
        }

        binding.buildingDescription.setMovementMethod(new ScrollingMovementMethod());

    }

    private void loadImagePicasso(ImageView imageView, String imageURL){
        Picasso.get().load(imageURL).into(imageView,
                new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: Size:" + ((BitmapDrawable) imageView.getDrawable()).getBitmap().getByteCount());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }
                });
    }
}