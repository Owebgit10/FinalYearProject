package com.example.questapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    private TextView toolBaruserName;
    private TextView pageTitle;
    private ImageView profileIcon, notificationBell;
    private TextView notificationBadgeCounter;

    private BottomNavigationView bottomNavigationView;

    private int notificationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolBaruserName = findViewById(R.id.toolBaruserName);
        pageTitle = findViewById(R.id.pageTitle);
        profileIcon = findViewById(R.id.profileIcon);
        notificationBell = findViewById(R.id.notificationBell);
        notificationBadgeCounter = findViewById(R.id.notificationBadgeCounter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            toolBaruserName.setText(displayName != null ? displayName : getString(R.string.default_user_name));
        }

        profileIcon.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, profileIcon, Gravity.END, 0, R.style.CustomPopupMenu);
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

            for (int i = 0; i < popup.getMenu().size(); i++) {
                MenuItem item = popup.getMenu().getItem(i);
                SpannableString spanString = new SpannableString(item.getTitle());
                spanString.setSpan(new ForegroundColorSpan(Color.parseColor("#DCA900")), 0, spanString.length(), 0);
                item.setTitle(spanString);
            }

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_sign_out) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.action_edit_profile) {
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        notificationBell.setOnClickListener(v -> clearNotificationCount());

        bottomNavigationView = findViewById(R.id.navigation);

        // Check if we're coming from ApplyToEventActivity to go to ManageEventsFragment directly
        String navigateTo = getIntent().getStringExtra("navigateTo");
        if ("joinedEvents".equals(navigateTo)) {
            ManageEventsFragment manageFragment = new ManageEventsFragment();
            Bundle args = new Bundle();
            args.putString("navigateTo", "joinedEvents");
            manageFragment.setArguments(args);
            loadFragment(manageFragment);
            pageTitle.setText(getString(R.string.title_events));
            bottomNavigationView.setSelectedItemId(R.id.navigation_events);
        } else if ("events".equals(navigateTo)) {
            loadFragment(new EventsFragment());
            pageTitle.setText(getString(R.string.title_events));
            bottomNavigationView.setSelectedItemId(R.id.navigation_events);
        } else {
            loadFragment(new QuestGoalPilotFragment());
            pageTitle.setText(getString(R.string.title_quest_goal_pilot));
        }


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int id = item.getItemId();
            if (id == R.id.navigation_mindset) {
                selectedFragment = new MindsetFragment();
                title = getString(R.string.title_mindset);
            } else if (id == R.id.navigation_events) {
                selectedFragment = new EventsFragment();
                title = getString(R.string.title_events);
            } else if (id == R.id.navigation_quest_goal_pilot) {
                selectedFragment = new QuestGoalPilotFragment();
                title = getString(R.string.title_quest_goal_pilot);
            } else if (id == R.id.navigation_communities) {
                selectedFragment = new CommunitiesFragment();
                title = getString(R.string.title_communities);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                pageTitle.setText(title);
            }
            return true;
        });

        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Enable Notifications")
                        .setMessage("Notifications are disabled. Would you like to open Settings and enable them?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public void incrementNotificationCount() {
        notificationCount++;
        updateBadge();
    }

    public void clearNotificationCount() {
        notificationCount = 0;
        updateBadge();
    }

    private void updateBadge() {
        if (notificationBadgeCounter != null) {
            if (notificationCount > 0) {
                notificationBadgeCounter.setText(String.valueOf(notificationCount));
                notificationBadgeCounter.setVisibility(View.VISIBLE);
            } else {
                notificationBadgeCounter.setVisibility(View.GONE);
            }
        }
    }
}












