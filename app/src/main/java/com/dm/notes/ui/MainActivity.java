package com.dm.notes.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dm.notes.R;
import com.dm.notes.databinding.ActivityMainBinding;
import com.dm.notes.helpers.DatabaseHelper;
import com.dm.notes.models.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesCallback {

    private final List<Note> notes = new ArrayList<>();

    private NotesAdapter adapter;
    private SQLiteDatabase db;

    private int currentNotePosition;
    private long currentNoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME, "");
            values.put(DatabaseHelper.COLUMN_TEXT, "");
            currentNoteId = db.insert(DatabaseHelper.TABLE, null, values);

            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            intent.putExtra("id", currentNoteId);
            startActivity(intent);

            currentNotePosition = adapter.getItemCount();

            notes.add(new Note(currentNoteId, ""));
        });

        db = new DatabaseHelper(this).getWritableDatabase();

        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME},
                null, null, null, null, null)) {

            while (cursor.moveToNext()) {
                notes.add(new Note(cursor.getLong(0), cursor.getString(1)));
            }
        }

        adapter = new NotesAdapter(this, notes);

        binding.notesList.addItemDecoration(new GridSpacingItemDecoration(2,
                Math.round(3 * getResources().getDisplayMetrics().density)));
        binding.notesList.setLayoutManager(new GridLayoutManager(this, 2));
        binding.notesList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_NAME},
                "_id = ?", new String[]{String.valueOf(currentNoteId)}, null, null, null)) {

            while (cursor.moveToNext()) {
                notes.get(currentNotePosition).setText(cursor.getString(0));
            }
        }

        adapter.notifyItemChanged(currentNotePosition);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra("id", note.getId());
        startActivity(intent);

        currentNoteId = note.getId();
        currentNotePosition = position;
    }

    @Override
    public void onNoteLongClicked(Note note, int position) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.warn)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    db.delete(DatabaseHelper.TABLE, "_id = ?",
                            new String[]{String.valueOf(note.getId())});

                    notes.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                })
                .create()
                .show();
    }
}