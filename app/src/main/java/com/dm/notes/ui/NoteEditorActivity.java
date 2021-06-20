package com.dm.notes.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dm.notes.R;
import com.dm.notes.helpers.DatabaseHelper;

public class NoteEditorActivity extends AppCompatActivity {
    private SQLiteDatabase db;

    private EditText noteName;
    private EditText noteText;

    private Intent intent;
    private boolean hasChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        noteName = findViewById(R.id.note_name);
        noteText = findViewById(R.id.note_text);

        Button bold = findViewById(R.id.bold);
        Button italic = findViewById(R.id.italic);
        Button underline = findViewById(R.id.underline);
        Button strikethrough = findViewById(R.id.strikethrough);
        Button normal = findViewById(R.id.normal);

        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySpan(new StyleSpan(Typeface.BOLD));
            }
        });

        italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySpan(new StyleSpan(Typeface.ITALIC));
            }
        });

        underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySpan(new UnderlineSpan());
            }
        });

        strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySpan(new StrikethroughSpan());
            }
        });

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText;
                if (noteText.isFocused())
                    editText = noteText;
                else if (noteName.isFocused())
                    editText = noteName;
                else
                    return;

                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();

                Spannable text = new SpannableString(editText.getText());
                CharacterStyle[] spans = text.getSpans(start, end, CharacterStyle.class);
                for (CharacterStyle span : spans)
                    text.removeSpan(span);

                editText.setText(text);
                editText.setSelection(start, end);
            }
        });

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

        noteText.addTextChangedListener(watcher);
        noteName.addTextChangedListener(watcher);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();

        intent = getIntent();

        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_TEXT},
                "_id = ?",
                new String[]{String.valueOf(intent.getLongExtra("id", 0))},
                null, null, null)) {

            cursor.moveToFirst();
            SpannableString name = SpannableString.valueOf(Html.fromHtml(cursor.getString(0)));
            SpannableString text = SpannableString.valueOf(Html.fromHtml(cursor.getString(1)));

            if (name.toString().endsWith("\n\n"))
                name = (SpannableString) name.subSequence(0, name.length() - 2);
            if (text.toString().endsWith("\n\n"))
                text = (SpannableString) text.subSequence(0, text.length() - 2);

            noteName.setText(name);
            noteText.setText(text);
        }

        if (intent.getBooleanExtra("del", false)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.warn)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.delete(DatabaseHelper.TABLE, "_id = ?",
                                    new String[]{String.valueOf(intent.getLongExtra("id", 0))});

                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create()
                    .show();
        }
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

    public void saveChanges() {
        if (hasChanges) {
            ContentValues values = new ContentValues();

            String[] lines;
            if (noteName.getText().toString().equals("") &&
                    (lines = noteText.getText().toString().split("\n", 2)).length > 0) {

                values.put(DatabaseHelper.COLUMN_NAME,
                        lines[0].length() > 50 ? lines[0].substring(0, 50) : lines[0]);
            } else
                values.put(DatabaseHelper.COLUMN_NAME, Html.toHtml(noteName.getText()));

            values.put(DatabaseHelper.COLUMN_TEXT, Html.toHtml(noteText.getText()));

            db.update(DatabaseHelper.TABLE, values, "_id = ?",
                    new String[]{String.valueOf(intent.getLongExtra("id", 0))});

            hasChanges = false;
        }
    }

    public void applySpan(CharacterStyle span) {
        EditText editText;
        if (noteText.isFocused())
            editText = noteText;
        else if (noteName.isFocused())
            editText = noteName;
        else
            return;

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start != end) {
            Spannable text = new SpannableString(editText.getText());
            text.setSpan(span, start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editText.setText(text);
            editText.setSelection(start, end);
        }
    }
}