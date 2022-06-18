package com.dm.notes.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dm.notes.R;
import com.dm.notes.databinding.ActivityMainBinding;
import com.dm.notes.helpers.DatabaseHelper;
import com.dm.notes.models.Note;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesCallback {

    private final List<Note> notes = new ArrayList<>();

    private ActivityMainBinding binding;

    private NotesAdapter adapter;
    private SQLiteDatabase db;

    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME, "");
            values.put(DatabaseHelper.COLUMN_TEXT, "");
            long noteId = db.insert(DatabaseHelper.TABLE, null, values);

            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            intent.putExtra("id", noteId);
            startActivity(intent);

            notes.add(currentNote = new Note(noteId, ""));
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
        binding.notesList.setAdapter(adapter);

        binding.notesList.scrollToPosition(notes.size() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentNote == null)
            return;

        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_NAME},
                "_id = ?", new String[]{String.valueOf(currentNote.getId())}, null, null, null)) {

            while (cursor.moveToNext()) {
                int position = notes.indexOf(currentNote);

                notes.remove(position);
                notes.add(position, new Note(currentNote.getId(), cursor.getString(0)));

                adapter.notifyItemChanged(position);
                binding.notesList.scrollToPosition(position);

                currentNote = null;
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra("id", note.getId());
        startActivity(intent);

        currentNote = note;
    }

    @Override
    public void onNoteLongClicked(Note note, int position) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.warning)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    db.delete(DatabaseHelper.TABLE, "_id = ?",
                            new String[]{String.valueOf(note.getId())});

                    notes.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                })
                .show();
    }
}