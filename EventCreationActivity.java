package com.example.questapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.util.Log;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.*;

public class EventCreationActivity extends AppCompatActivity {

    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private EditText eventNameInput, eventLocationInput, eventDateInput, eventTimeInput,
            eventDescriptionInput, eventSpacesInput, tagInput, otherCategoryInput;
    private Button btnCreateEvent, btnCancel, uploadImageButton;
    private ImageView eventImagePreview;
    private Spinner categorySpinner;
    private ChipGroup tagsChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_creation_activity);

        eventNameInput = findViewById(R.id.eventNameInput);
        eventLocationInput = findViewById(R.id.eventLocationInput);
        eventDateInput = findViewById(R.id.eventDateInput);
        eventTimeInput = findViewById(R.id.eventTimeInput);
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput);
        eventSpacesInput = findViewById(R.id.eventSpacesInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        otherCategoryInput = findViewById(R.id.otherCategoryInput);
        tagsChipGroup = findViewById(R.id.chipGroupTags);
        tagInput = findViewById(R.id.tagInput);
        eventImagePreview = findViewById(R.id.eventImagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCancel = findViewById(R.id.btnCancel);

        setupTagInput();
        setupImagePicker();
        setupCategorySpinner();

        eventDateInput.setOnClickListener(v -> showDatePicker());
        eventTimeInput.setOnClickListener(v -> showTimePicker());

        uploadImageButton.setOnClickListener(v -> openImagePicker());
        btnCreateEvent.setOnClickListener(v -> createEvent());
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Event not created.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.event_categories)
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Other".equalsIgnoreCase(selected)) {
                    otherCategoryInput.setVisibility(View.VISIBLE);
                } else {
                    otherCategoryInput.setVisibility(View.GONE);
                    otherCategoryInput.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                otherCategoryInput.setVisibility(View.GONE);
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            eventDateInput.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            eventTimeInput.setText(formattedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            eventImagePreview.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            Log.e("EventCreationActivity", "Image loading failed", e);
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void createEvent() {
        String name = eventNameInput.getText().toString().trim();
        String location = eventLocationInput.getText().toString().trim();
        String date = eventDateInput.getText().toString().trim();
        String time = eventTimeInput.getText().toString().trim();
        String description = eventDescriptionInput.getText().toString().trim();
        String spacesInput = eventSpacesInput.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String customCategory = otherCategoryInput.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty() || date.isEmpty() || time.isEmpty()
                || description.isEmpty() || spacesInput.isEmpty()
                || selectedCategory.isEmpty()
                || ("Other".equalsIgnoreCase(selectedCategory) && customCategory.isEmpty())) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int availableSpaces;
        try {
            availableSpaces = Integer.parseInt(spacesInput);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Available spaces must be a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) tagsChipGroup.getChildAt(i);
            tags.add(chip.getText().toString());
        }

        if (tags.isEmpty()) {
            Toast.makeText(this, "Please add at least one tag.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in to create an event.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String finalCategory = "Other".equalsIgnoreCase(selectedCategory) ? customCategory : selectedCategory;
        String eventId = FirebaseDatabase.getInstance().getReference("Events").push().getKey();
        if (eventId == null) {
            Toast.makeText(this, "Failed to generate event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("event_images").child(eventId);
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveEventToDatabase(eventId, name, location, date, time, description,
                                        availableSpaces, finalCategory, tags, userId, imageUrl);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show());
        } else {
            saveEventToDatabase(eventId, name, location, date, time, description,
                    availableSpaces, finalCategory, tags, userId, null);
        }
    }

    private void saveEventToDatabase(String eventId, String name, String location, String date, String time,
                                     String description, int availableSpaces, String category, List<String> tags,
                                     String userId, String imageUrl) {

        Event newEvent = new Event(eventId, name, location, date, time, description,
                availableSpaces, category, tags, userId, imageUrl);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        databaseReference.setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create event.", Toast.LENGTH_SHORT).show());
    }

    private void setupTagInput() {
        tagInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_COMMA || keyCode == KeyEvent.KEYCODE_SPACE)) {
                String tagText = tagInput.getText().toString().trim();
                if (!tagText.isEmpty()) {
                    Chip chip = new Chip(this);
                    chip.setText(tagText);
                    chip.setChipIconResource(R.drawable.ic_tag);
                    chip.setCloseIconVisible(true);
                    chip.setCloseIconResource(R.drawable.ic_closetag);
                    chip.setChipIconTintResource(R.color.colorPrimary);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                    chip.setCloseIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                    chip.setOnCloseIconClickListener(view -> tagsChipGroup.removeView(chip));
                    tagsChipGroup.addView(chip);
                    tagInput.setText("");
                }
                return true;
            }
            return false;
        });
    }
}











