package com.dm.notes.ui;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dm.notes.R;
import com.dm.notes.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    public List<Note> data;
    public NoteEventListener callback;

    public NotesAdapter(Context context, List<Note> data) {
        this.callback = (NoteEventListener) context;
        this.data = data;
    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        SpannableString text = SpannableString.valueOf(Html.fromHtml(this.data.get(position).text));
        if (text.toString().endsWith("\n\n"))
            text = (SpannableString) text.subSequence(0, text.length() - 2);

        holder.text.setText(text);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView text;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            this.text = view.findViewById(R.id.note);
        }

        @Override
        public void onClick(View view) {
            callback.onNoteClicked(data.get(getLayoutPosition()));
        }

        @Override
        public boolean onLongClick(View view) {
            callback.onNoteLongClicked(data.get(getLayoutPosition()));
            return true;
        }
    }

    public interface NoteEventListener {
        void onNoteClicked(Note note);

        void onNoteLongClicked(Note note);
    }
}
