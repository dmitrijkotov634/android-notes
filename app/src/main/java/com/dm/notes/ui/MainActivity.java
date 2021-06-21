package com.dm.notes.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dm.notes.R;
import com.dm.notes.helpers.DatabaseHelper;
import com.dm.notes.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NoteEventListener {

    private final List<Note> notes = new ArrayList<>();

    private NotesAdapter adapter;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView notesList = findViewById(R.id.notes_list);
        FloatingActionButton fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NAME, "");
                values.put(DatabaseHelper.COLUMN_TEXT, "");
                long id = db.insert(DatabaseHelper.TABLE, null, values);

                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();

        adapter = new NotesAdapter(this, notes);

        notesList.addItemDecoration(new GridSpacingItemDecoration(2,
                Math.round(3 * getResources().getDisplayMetrics().density)));
        notesList.setLayoutManager(new GridLayoutManager(this, 2));
        notesList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        notes.clear();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME},
                null, null, null, null, null)) {

            while (cursor.moveToNext()) {
                String name = cursor.getString(1);
                notes.add(new Note(cursor.getLong(0),
                        name.equals("") ? getString(R.string.default_name) + " №" + cursor.getLong(0) : name));
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNoteClicked(Note note) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra("id", note.id);
        startActivity(intent);
    }

    @Override
    public void onNoteLongClicked(Note note) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.warn)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.delete(DatabaseHelper.TABLE, "_id = ?",
                                new String[]{String.valueOf(note.id)});

                        onResume();
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