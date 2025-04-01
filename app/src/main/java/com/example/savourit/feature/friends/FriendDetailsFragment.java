package com.example.savourit.feature.friends;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savourit.R;
import com.example.savourit.adapters.ReviewAdapter;
import com.example.savourit.feature.friends.viewmodel.FriendDetailsViewModel;
import com.example.savourit.models.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


public class FriendDetailsFragment extends Fragment {

    private TextView txtFriendUsername, txtFriendAccountType, txtFriendEmail;
    private Button btnBack;

    private FriendDetailsViewModel viewModel;
    private RecyclerView recyclerFriendReviews;
    private List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;


    public FriendDetailsFragment() {
        // Required empty constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        txtFriendUsername = view.findViewById(R.id.txtFriendUsername);
        txtFriendAccountType = view.findViewById(R.id.txtFriendAccountType);
        txtFriendEmail = view.findViewById(R.id.txtFriendEmail);
        btnBack = view.findViewById(R.id.btnBack);
        recyclerFriendReviews = view.findViewById(R.id.recyclerFriendReviews);
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerFriendReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFriendReviews.setAdapter(reviewAdapter);


        viewModel = new ViewModelProvider(this).get(FriendDetailsViewModel.class);

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        if (getArguments() != null && getArguments().containsKey("friendId")) {
            String friendId = getArguments().getString("friendId");
            viewModel.loadFriendDetails(friendId);
        }

        viewModel.getFriendDetails().observe(getViewLifecycleOwner(), friend -> {
            if (friend != null) {
                txtFriendUsername.setText(friend.getUsername());
                txtFriendAccountType.setText(friend.getAccountType());
                txtFriendEmail.setText(friend.getEmail());

                Log.d("DEBUG", "Fetching reviews for userID: " + friend.getUserId());
                // Load reviews based on friend.getUserId()
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("reviews")
                        .whereEqualTo("userID", friend.getUserId())
                        .get()
                        .addOnSuccessListener(query -> {
                            reviewList.clear();

                            if (query.isEmpty()) {
                                reviewAdapter.notifyDataSetChanged();
                                return;
                            }

                            List<Review> tempList = new ArrayList<>();
                            int totalReviews = query.size();
                            final int[] completed = {0};

                            for (QueryDocumentSnapshot doc : query) {
                                Log.d("DEBUG", "Review doc: " + doc.getData().toString());

                                Double ratingObj = doc.getDouble("rating");
                                double rating = ratingObj != null ? ratingObj : 0.0;

                                String resId = doc.getString("resID");

                                if (resId != null) {
                                    final String[] commentFinal = {doc.getString("comment")};
                                    final Double rawRating = doc.getDouble("rating");
                                    final double[] ratingFinal = {rawRating != null ? rawRating : 0.0};

                                    // Determine liked status manually
                                    final boolean isLiked = doc.contains("liked")
                                            ? Boolean.TRUE.equals(doc.getBoolean("liked"))
                                            : ratingFinal[0] >= 4.0;



                                    FirebaseFirestore.getInstance().collection("restaurants")
                                            .document(resId)
                                            .get()
                                            .addOnSuccessListener(restaurantDoc -> {
                                                String restaurantName = restaurantDoc.getString("name");
                                                if (restaurantName == null) restaurantName = "Unknown Restaurant";
                                                Log.d("DEBUG", "Adding review: " + restaurantName + " | rating: " + ratingFinal[0] + " | liked: " + isLiked);

                                                tempList.add(new Review(restaurantName, commentFinal[0], ratingFinal[0], isLiked));

                                                completed[0]++;
                                                if (completed[0] == totalReviews) {
                                                    reviewList.addAll(tempList);
                                                    reviewAdapter.notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("DEBUG", "Failed to fetch restaurant", e);
                                                completed[0]++;
                                                if (completed[0] == totalReviews) {
                                                    reviewList.addAll(tempList);
                                                    reviewAdapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                                else {
                                    completed[0]++;
                                    if (completed[0] == totalReviews) {
                                        reviewList.addAll(tempList);
                                        reviewAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FriendDetailsFragment", "Error loading reviews", e);
                        });
            }
        });
    }

}