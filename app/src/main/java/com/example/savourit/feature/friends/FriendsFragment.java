package com.example.savourit.feature.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savourit.R;
import com.example.savourit.adapters.FriendsAdapter;
import com.example.savourit.feature.friends.viewmodel.FriendsViewModel;
import com.example.savourit.models.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerFriends;
    private FriendsAdapter friendsAdapter;
    private final List<Friend> friendsList = new ArrayList<>();
    private FriendsViewModel friendsViewModel;
    private EditText edtFriendUsername;
    private Button btnAddFriend, btnLogout;

    public FriendsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.title_friends);

        recyclerFriends = view.findViewById(R.id.recyclerFriends);
        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        btnLogout = view.findViewById(R.id.btnLogout);
        edtFriendUsername = view.findViewById(R.id.edtFriendUsername);

        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        friendsAdapter = new FriendsAdapter(friendsList, this::navigateToChatFragment, this::onFriendLongClick);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFriends.setAdapter(friendsAdapter);

        // Observe Friends List from ViewModel
        friendsViewModel.getFriendsList().observe(getViewLifecycleOwner(), updatedList -> {
            friendsList.clear();
            friendsList.addAll(updatedList);
            friendsAdapter.notifyDataSetChanged();
        });

        // Load Friends List
        friendsViewModel.loadFriends();

        btnLogout.setOnClickListener(v -> friendsViewModel.logoutUser(requireActivity()));
        btnAddFriend.setOnClickListener(v -> {
            String username = edtFriendUsername.getText().toString().trim();
            if (!username.isEmpty()) {
                friendsViewModel.addFriend(username);
            } else {
                Toast.makeText(requireContext(), "Enter a username!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToChatFragment(Friend friend) {
        String chatRoomId = friendsViewModel.getChatRoomId(friend);
        if (chatRoomId != null) {
            Bundle bundle = new Bundle();
            bundle.putString("chatRoomId", chatRoomId);
            bundle.putString("friendId", friend.getUserId());
            Navigation.findNavController(requireView()).navigate(R.id.friends_to_chat, bundle);
        }
    }

    private void onFriendLongClick(Friend friend) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Friend")
                .setMessage("Do you want to remove " + friend.getUsername() + "?")
                .setPositiveButton("Yes", (dialog, which) -> friendsViewModel.deleteFriend(friend))
                .setNegativeButton("No", null)
                .show();
    }
}
