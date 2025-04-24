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

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.*;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.common.api.Status;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.*;

public class UpdateEventActivity extends AppCompatActivity {

    private Event event;

    private EditText eventNameInput, eventLocationInput, eventDateInput, eventTimeInput,
            eventDescriptionInput, eventSpacesInput, tagInput, otherCategoryInput;
    private Button uploadImageButton, removeImageButton, btnConfirmUpdate, btnCancelEdit;
    private ImageView eventImagePreview;
    private Spinner categorySpinner;
    private FlexboxLayout tagInputContainer;
    private ProgressBar progressBar;

    private Uri imageUri;
    private boolean imageLocked = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        eventImagePreview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("UpdateEventActivity", "Image loading failed", e);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    eventLocationInput.setText(place.getAddress());
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                    if (result.getData() != null) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Log.e("UpdateEventActivity", "Places error: " + status.getStatusMessage());
                    } else {
                        Log.e("UpdateEventActivity", "Places error: intent data is null.");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        MaterialToolbar updatedEventDetailsToolBar = findViewById(R.id.toolbarUpdatedEventDetails);
        updatedEventDetailsToolBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupPlacesAutocomplete();
        setupDateTimePickers();
        setupCategorySpinner();
        setupTagInput();
        checkAttendeeRestrictions();
        populateFields();

        uploadImageButton.setOnClickListener(v -> openImagePicker());
        removeImageButton.setOnClickListener(v -> {
            imageUri = null;
            eventImagePreview.setImageResource(R.drawable.ic_upload_placeholder);
        });

        btnConfirmUpdate.setOnClickListener(v -> updateEventToDatabase());
        btnCancelEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void initializeViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        eventLocationInput = findViewById(R.id.eventLocationInput);
        eventDateInput = findViewById(R.id.eventDateInput);
        eventTimeInput = findViewById(R.id.eventTimeInput);
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput);
        eventSpacesInput = findViewById(R.id.eventSpacesInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        otherCategoryInput = findViewById(R.id.otherCategoryInput);
        tagInputContainer = findViewById(R.id.tagInputContainer);
        tagInput = findViewById(R.id.tagInput);
        eventImagePreview = findViewById(R.id.eventImagePreview);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        removeImageButton = findViewById(R.id.removeImageButton);
        btnConfirmUpdate = findViewById(R.id.btnConfirmUpdate);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        progressBar = findViewById(R.id.loadingSpinner);
    }

    private void populateFields() {
        eventNameInput.setText(event.getName());
        eventLocationInput.setText(event.getLocation());
        eventDateInput.setText(event.getDate());
        eventTimeInput.setText(event.getTime());
        eventDescriptionInput.setText(event.getDescription());
        eventSpacesInput.setText(String.valueOf(event.getAvailableSpaces()));

        String category = event.getCategory();
        String[] categories = getResources().getStringArray(R.array.event_categories);
        boolean matched = false;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) {
                categorySpinner.setSelection(i);
                matched = true;
                break;
            }
        }
        if (!matched) {
            categorySpinner.setSelection(Arrays.asList(categories).indexOf("Other"));
            otherCategoryInput.setVisibility(View.VISIBLE);
            otherCategoryInput.setText(category);
        }

        for (String tag : event.getTags()) {
            addTagChip(tag);
        }

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this).load(event.getImageUrl()).into(eventImagePreview);
        }
    }

    private void checkAttendeeRestrictions() {
        DatabaseReference joinedRef = FirebaseDatabase.getInstance().getReference("EventParticipants").child(event.getId());
        joinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasAttendees = snapshot.exists();
                if (hasAttendees) {
                    eventNameInput.setEnabled(false);
                    uploadImageButton.setEnabled(false);
                    removeImageButton.setEnabled(false);
                    imageLocked = true;
                    Toast.makeText(UpdateEventActivity.this,
                            "This event already has attendees — name and image are locked.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateEventActivity.this, "Failed to check attendees.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void setupDateTimePickers() {
        eventDateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String formattedDate = getString(R.string.formatted_date, day, month + 1, year);
                eventDateInput.setText(formattedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        eventTimeInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, min) -> {
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, min);
                eventTimeInput.setText(formattedTime);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
    }

    private void setupPlacesAutocomplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_API_KEY_HERE");
        }
        eventLocationInput.setFocusable(false);
        eventLocationInput.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
            autocompleteLauncher.launch(intent);
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, getResources().getStringArray(R.array.event_categories));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ("Other".equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                    otherCategoryInput.setVisibility(View.VISIBLE);
                } else {
                    otherCategoryInput.setVisibility(View.GONE);
                    otherCategoryInput.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTagInput() {
        tagInput.setOnKeyListener((v, keyCode, keyEvent) -> {
            String text = tagInput.getText().toString().trim();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_COMMA)
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN && !text.isEmpty()) {
                if (!isDuplicateTag(text)) addTagChip(text);
                tagInput.setText("");
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DEL && tagInput.getText().toString().isEmpty()) {
                if (tagInputContainer.getChildCount() > 1)
                    tagInputContainer.removeViewAt(tagInputContainer.getChildCount() - 2);
                return true;
            }
            return false;
        });
    }

    private boolean isDuplicateTag(String tag) {
        for (int i = 0; i < tagInputContainer.getChildCount() - 1; i++) {
            View view = tagInputContainer.getChildAt(i);
            if (view instanceof Chip && ((Chip) view).getText().toString().equalsIgnoreCase(tag)) return true;
        }
        return false;
    }

    private void addTagChip(String tag) {
        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setChipIconResource(R.drawable.ic_tag);
        chip.setCloseIconResource(R.drawable.ic_closetag);
        chip.setChipIconTintResource(R.color.colorPrimary);
        chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        chip.setCloseIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
        chip.setOnCloseIconClickListener(view -> tagInputContainer.removeView(chip));
        tagInputContainer.addView(chip, tagInputContainer.getChildCount() - 1);
    }

    private void updateEventToDatabase() {
        progressBar.setVisibility(View.VISIBLE);

        String name = eventNameInput.getText().toString().trim();
        String location = eventLocationInput.getText().toString().trim();
        String date = eventDateInput.getText().toString().trim();
        String time = eventTimeInput.getText().toString().trim();
        String desc = eventDescriptionInput.getText().toString().trim();
        String spacesStr = eventSpacesInput.getText().toString().trim();
        String cat = categorySpinner.getSelectedItem().toString();
        String otherCat = otherCategoryInput.getText().toString().trim();

        if ((name.isEmpty() && !imageLocked) || location.isEmpty() || date.isEmpty() || time.isEmpty()
                || desc.isEmpty() || spacesStr.isEmpty() || ("Other".equalsIgnoreCase(cat) && otherCat.isEmpty())) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int spaces;
        try {
            spaces = Integer.parseInt(spacesStr);
        } catch (NumberFormatException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Available spaces must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagInputContainer.getChildCount() - 1; i++) {
            View view = tagInputContainer.getChildAt(i);
            if (view instanceof Chip) tags.add(((Chip) view).getText().toString().toLowerCase());
        }

        String finalCategory = "Other".equalsIgnoreCase(cat) ? otherCat : cat;

        if (!imageLocked) {
            event.setName(name.toLowerCase());
        }

        event.setLocation(location.toLowerCase());
        event.setCategory(finalCategory.toLowerCase());
        event.setTags(tags);

        event.setDate(date);
        event.setTime(time);
        event.setDescription(desc);
        event.setAvailableSpaces(spaces);

        if (!imageLocked && imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("event_images")
                    .child(event.getId());

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            event.setImageUrl(uri.toString());
                            pushUpdateToFirebase(event);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    });

            return;
        }

        pushUpdateToFirebase(event);
    }

    private void pushUpdateToFirebase(Event event) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Events").child(event.getId());
        ref.setValue(event)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateEventActivity", "Database update failed", e);
                });
    }
}



