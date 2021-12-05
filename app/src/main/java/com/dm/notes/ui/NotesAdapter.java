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
    public List<Note> data;
    public NotesCallback callback;

    public NotesAdapter(NotesCallback callback, List<Note> data) {
        this.callback = callback;
        this.data = data;
    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NoteLayoutBinding rowItem = NoteLayoutBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        holder.binding.note.setText(Utils.fromHtml(data.get(position).getText()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final NoteLayoutBinding binding;

        public ViewHolder(NoteLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            callback.onNoteClicked(data.get(getLayoutPosition()), getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            callback.onNoteLongClicked(data.get(getLayoutPosition()), getLayoutPosition());
            return true;
        }
    }

    public interface NotesCallback {
        void onNoteClicked(Note note, int position);

        void onNoteLongClicked(Note note, int position);
    }
}
