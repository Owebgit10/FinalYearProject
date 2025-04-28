package com.example.questapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.*;

public class CommunityCreationActivity extends AppCompatActivity {

    private EditText communityNameInput, communityDescriptionInput, communityTagInput, communityRuleInput;
    private Button btnCreateCommunity, btnCancelCommunityCreation, uploadCommunityImageButton, removeCommunityImageButton;
    private ImageView communityImagePreview;
    private FlexboxLayout communityTagInputContainer, communityRuleInputContainer;
    private Uri imageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_creation);

        // View bindings
        communityNameInput = findViewById(R.id.communityNameInput);
        communityDescriptionInput = findViewById(R.id.communityDescriptionInput);
        communityTagInput = findViewById(R.id.communityTagInput);
        communityRuleInput = findViewById(R.id.communityRuleInput);
        communityTagInputContainer = findViewById(R.id.communityTagInputContainer);
        communityRuleInputContainer = findViewById(R.id.communityRuleInputContainer);
        communityImagePreview = findViewById(R.id.communityImagePreview);
        uploadCommunityImageButton = findViewById(R.id.uploadCommunityImageButton);
        removeCommunityImageButton = findViewById(R.id.removeCommunityImageButton);
        btnCreateCommunity = findViewById(R.id.btnCreateCommunity);
        btnCancelCommunityCreation = findViewById(R.id.btnCancelCommunityCreation);

        setupImagePicker();
        setupTagInput(communityTagInput, communityTagInputContainer);
        setupTagInput(communityRuleInput, communityRuleInputContainer);

        uploadCommunityImageButton.setOnClickListener(v -> openImagePicker());
        removeCommunityImageButton.setOnClickListener(v -> {
            imageUri = null;
            communityImagePreview.setImageResource(R.drawable.ic_upload_placeholder);
        });

        btnCancelCommunityCreation.setOnClickListener(v -> showCancelConfirmationDialog());
        btnCreateCommunity.setOnClickListener(v -> createCommunity());
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Community Creation")
                .setMessage("Are you sure you want to stop creating this community? Details added will not be saved for creating the same community in future.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            communityImagePreview.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            Log.e("CommunityCreation", "Image loading failed", e);
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void setupTagInput(EditText inputField, FlexboxLayout container) {
        inputField.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                String text = inputField.getText().toString().trim();

                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_COMMA || keyCode == KeyEvent.KEYCODE_SPACE)
                        && !text.isEmpty()) {

                    if (!isDuplicateChip(text, container)) {
                        Chip chip = new Chip(this);
                        chip.setText(text);
                        chip.setChipIconVisible(true);
                        chip.setChipIconResource(R.drawable.ic_tag);
                        chip.setCloseIconVisible(true);
                        chip.setCloseIconResource(R.drawable.ic_closetag);
                        chip.setChipIconTintResource(R.color.colorPrimary);
                        chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                        chip.setCloseIconTint(ContextCompat.getColorStateList(this, R.color.white));
                        chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.colorPrimary));
                        chip.setOnCloseIconClickListener(view -> container.removeView(chip));
                        container.addView(chip, container.getChildCount() - 1);
                    }

                    inputField.setText("");
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_DEL && text.isEmpty() && container.getChildCount() > 1) {
                    container.removeViewAt(container.getChildCount() - 2);
                    return true;
                }
            }
            return false;
        });
    }

    private boolean isDuplicateChip(String text, FlexboxLayout container) {
        for (int i = 0; i < container.getChildCount() - 1; i++) {
            View child = container.getChildAt(i);
            if (child instanceof Chip) {
                if (((Chip) child).getText().toString().equalsIgnoreCase(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createCommunity() {
        String name = communityNameInput.getText().toString().trim();
        String description = communityDescriptionInput.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = extractChipTexts(communityTagInputContainer);
        List<String> rules = extractChipTexts(communityRuleInputContainer);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String communityId = FirebaseDatabase.getInstance().getReference("Communities").push().getKey();
        if (communityId == null) {
            Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("community_images").child(communityId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveCommunityToDatabase(communityId, name, description, tags, rules, user.getUid(), imageUrl);
                    })
            ).addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveCommunityToDatabase(communityId, name, description, tags, rules, user.getUid(), null);
        }
    }

    private List<String> extractChipTexts(FlexboxLayout container) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < container.getChildCount() - 1; i++) {
            View child = container.getChildAt(i);
            if (child instanceof Chip) {
                list.add(((Chip) child).getText().toString().toLowerCase());
            }
        }
        return list;
    }

    private void saveCommunityToDatabase(String id, String name, String description, List<String> tags,
                                         List<String> rules, String userId, String imageUrl) {
        Map<String, Object> community = new HashMap<>();
        community.put("id", id);
        community.put("name", name);
        community.put("description", description);
        community.put("tags", tags);
        community.put("rules", rules);
        community.put("userId", userId);
        community.put("imageUrl", imageUrl);
        community.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("Communities")
                .child(id)
                .setValue(community)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Community created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create community", Toast.LENGTH_SHORT).show());
    }
}




