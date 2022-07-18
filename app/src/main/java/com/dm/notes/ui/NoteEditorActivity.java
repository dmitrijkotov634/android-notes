package com.dm.notes.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dm.notes.R;
import com.dm.notes.databinding.ActivityNoteBinding;
import com.dm.notes.helpers.DatabaseHelper;
import com.dm.notes.helpers.Utils;

public class NoteEditorActivity extends AppCompatActivity {
    private ActivityNoteBinding binding;

    private SQLiteDatabase db;

    private Intent intent;

    private boolean hasChanges;
    private boolean editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                hasChanges = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        binding.fab2.setOnClickListener(view -> changeMode(!editing));

        binding.noteName.addTextChangedListener(watcher);
        binding.noteText.addTextChangedListener(watcher);

        db = new DatabaseHelper(this).getWritableDatabase();

        intent = getIntent();

        changeMode(intent.getBooleanExtra("empty", true));

        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_TEXT},
                "_id = ?",
                new String[]{String.valueOf(intent.getLongExtra("id", 0))},
                null, null, null)) {

            cursor.moveToFirst();

            binding.noteName.setText(Utils.fromHtml(cursor.getString(0)));
            binding.noteText.setText(Utils.fromHtml(cursor.getString(1)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (editing)
            getMenuInflater().inflate(R.menu.editor_menu, menu);

        return true;
    }

    private void changeMode(boolean editing) {
        binding.noteText.setLinksClickable(!editing);
        binding.noteText.setMovementMethod(editing ? null : LinkMovementMethod.getInstance());

        Linkify.addLinks(binding.noteText, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

        binding.noteName.setFocusable(editing);
        binding.noteName.setCursorVisible(editing);
        binding.noteName.setFocusableInTouchMode(editing);

        binding.noteText.setFocusable(editing);
        binding.noteText.setCursorVisible(editing);
        binding.noteText.setFocusableInTouchMode(editing);

        binding.fab2.setImageResource(editing ? R.drawable.ic_baseline_check_24 : R.drawable.ic_edit);

        invalidateOptionsMenu();

        if (!editing) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }

        saveChanges();

        this.editing = editing;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        applySpan(getSpan(item.getItemId()));
        return true;
    }

    private CharacterStyle getSpan(int id) {
        if (id == R.id.bold)
            return new StyleSpan(Typeface.BOLD);
        else if (id == R.id.italic)
            return new StyleSpan(Typeface.ITALIC);
        else if (id == R.id.underline)
            return new UnderlineSpan();
        else if (id == R.id.strikethrough)
            return new StrikethroughSpan();

        return null;
    }

    @Override
    public void onBackPressed() {
        saveChanges();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        saveChanges();
        super.onStop();
    }

    private void saveChanges() {
        if (hasChanges) {
            ContentValues values = new ContentValues();

            String name;
            if (binding.noteName.getText().toString().isEmpty()) {
                String[] lines = binding.noteText.getText().toString().split("\n", 2);
                name = lines[0].length() > 50 ? lines[0].substring(0, 50) : lines[0];
            } else
                name = Html.toHtml(binding.noteName.getText());

            values.put(DatabaseHelper.COLUMN_NAME, name);
            values.put(DatabaseHelper.COLUMN_TEXT, Html.toHtml(binding.noteText.getText()));

            db.update(DatabaseHelper.TABLE, values, "_id = ?",
                    new String[]{String.valueOf(intent.getLongExtra("id", 0))});

            hasChanges = false;
        }
    }

    private void applySpan(CharacterStyle span) {
        EditText input = binding.noteText.isFocused() ? binding.noteText : (binding.noteName.isFocused() ? binding.noteName : null);

        if (input == null)
            return;

        int start = input.getSelectionStart();
        int end = input.getSelectionEnd();

        Spannable text = new SpannableString(input.getText());

        if (start != end) {
            if (span == null) {
                CharacterStyle[] spans = text.getSpans(start, end, CharacterStyle.class);
                for (CharacterStyle selectSpan : spans)
                    text.removeSpan(selectSpan);
            } else {
                text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            input.setText(text);
            input.setSelection(start, end);
        }
    }
}