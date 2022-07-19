package com.dm.notes.ui;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dm.notes.R;
import com.dm.notes.databinding.ActivityMainBinding;
import com.dm.notes.helpers.DatabaseHelper;
import com.dm.notes.models.Note;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("NotifyDataSetChanged")
public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesCallback {

    private final Set<Note> selected = new HashSet<>();
    private final List<Note> notes = new ArrayList<>();

    private ActivityMainBinding binding;

    private NotesAdapter adapter;
    private SQLiteDatabase db;

    private Note currentNote;

    SearchView.OnCloseListener listener = () -> {
        binding.fab.setVisibility(View.VISIBLE);

        adapter = new NotesAdapter(MainActivity.this, notes, selected);
        binding.notesList.setAdapter(adapter);

        invalidateOptionsMenu();

        binding.notesList.scrollToPosition(notes.size() - 1);
        return false;
    };

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
            intent.putExtra("editing", true);
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

        adapter = new NotesAdapter(this, notes, selected);

        binding.notesList.addItemDecoration(new GridSpacingItemDecoration(2,
                Math.round(3 * getResources().getDisplayMetrics().density)));
        binding.notesList.setAdapter(adapter);

        binding.notesList.scrollToPosition(notes.size() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.remove) {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Material3_Body_Text)
                    .setTitle(R.string.remove)
                    .setIcon(R.drawable.ic_baseline_delete_24)
                    .setMessage(R.string.warning)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        for (Note note : selected) {
                            if (notes != adapter.notes)
                                adapter.notes.remove(note);

                            notes.remove(note);
                            db.delete(DatabaseHelper.TABLE, "_id = ?",
                                    new String[]{String.valueOf(note.getId())});
                        }

                        adapter.notifyDataSetChanged();
                        selected.clear();
                        invalidateOptionsMenu();
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!selected.isEmpty()) {
            getMenuInflater().inflate(R.menu.main_selected_menu, menu);
            return true;
        }

        getMenuInflater().inflate(R.menu.main_search_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        searchView.setOnSearchClickListener(v -> binding.fab.setVisibility(View.GONE));
        searchView.setOnCloseListener(listener);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                doSearch(search);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                doSearch(search);
                return true;
            }

            private void doSearch(String search) {
                List<Note> filtered = new ArrayList<>();

                search = search.trim().toLowerCase();
                for (Note note : notes) {
                    if (Html.fromHtml(note.getText()).toString().toLowerCase().contains(search))
                        filtered.add(note);
                }

                adapter = new NotesAdapter(MainActivity.this, filtered, selected);

                binding.notesList.setAdapter(adapter);
                binding.notesList.scrollToPosition(filtered.size() - 1);
            }
        });

        return true;
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

                Note newNote = new Note(currentNote.getId(), cursor.getString(0));

                notes.remove(position);
                notes.add(position, newNote);

                if (selected.contains(currentNote)) {
                    selected.remove(currentNote);
                    selected.add(newNote);
                }

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
        intent.putExtra("editing", note.getText().isEmpty());

        startActivity(intent);

        currentNote = note;

        listener.onClose();
    }

    @Override
    public void onNoteSelected(Note note) {
        if (binding.fab.getVisibility() == View.VISIBLE)
            invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (!selected.isEmpty()) {
            selected.clear();
            invalidateOptionsMenu();
            adapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();
    }
}