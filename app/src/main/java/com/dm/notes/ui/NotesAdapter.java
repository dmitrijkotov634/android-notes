package com.dm.notes.ui;

import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dm.notes.R;
import com.dm.notes.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    public List<Note> data;

    public NotesAdapter(List<Note> data) {
        this.data = data;
    }

    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(NotesAdapter.ViewHolder holder, int position) {
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
        private TextView text;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            this.text = view.findViewById(R.id.note);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), NoteEditorActivity.class);
            intent.putExtra("id", data.get(getLayoutPosition()).id);

            view.getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            Intent intent = new Intent(view.getContext(), NoteEditorActivity.class);
            intent.putExtra("id", data.get(getLayoutPosition()).id);
            intent.putExtra("del", true);

            view.getContext().startActivity(intent);

            return true;
        }
    }
}
