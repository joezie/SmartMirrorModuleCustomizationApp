package com.example.smartmirrormodulecustomizationapp;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 *  View holder for items of type seek bar
 *  Suitable for integer type values
 */
class SeekbarItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView fieldName;
    private final SeekBar bar;
    private final TextView progress;

    SeekbarItemViewHolder(final View itemView) {
        super(itemView);

        fieldName = itemView.findViewById(R.id.fieldName_seekbar_layout);
        bar = itemView.findViewById(R.id.bar_seekbar_layout);
        progress = itemView.findViewById(R.id.progress_seekbar_layout);
    }

    final TextView getFieldName() {
        return fieldName;
    }

    final SeekBar getBar() {
        return bar;
    }

    final TextView getProgress() { return progress; }

}
