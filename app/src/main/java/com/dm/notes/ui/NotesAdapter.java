package com.dm.notes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dm.notes.databinding.NoteLayoutBinding;
import com.dm.notes.helpers.Utils;
import com.dm.notes.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    public List<Note> notes;
    public List<Note> selected;
    public NotesCallback callback;

    public NotesAdapter(NotesCallback callback, List<Note> notes, List<Note> selected) {
        this.callback = callback;
        this.notes = notes;
        this.selected = selected;
    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NoteLayoutBinding rowItem = NoteLayoutBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        holder.binding.note.setText(Utils.fromHtml(notes.get(position).getText()));
        holder.binding.getRoot().setChecked(selected.contains(notes.get(position)));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final NoteLayoutBinding binding;

        public ViewHolder(NoteLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            View.OnLongClickListener longClickListener = v -> {
                Note note = notes.get(getLayoutPosition());

                if (selected.contains(note)) selected.remove(note);
                else selected.add(note);

                binding.getRoot().toggle();

                callback.onNoteSelected(note);
                return true;
            };

            binding.getRoot().setOnClickListener((v) -> {
                if (!selected.isEmpty()) {
                    longClickListener.onLongClick(v);
                    return;
                }

                callback.onNoteClicked(notes.get(getLayoutPosition()), getLayoutPosition());
            });

            binding.getRoot().setOnLongClickListener(longClickListener);
        }
    }

    public interface NotesCallback {
        void onNoteClicked(Note note, int position);

        void onNoteSelected(Note note);
    }
}

