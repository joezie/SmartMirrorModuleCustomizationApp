package com.example.smartmirrormodulecustomizationapp;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 *  View holder for items of type edit text
 *  Suitable for string type values
 */
class EdittextItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView fieldName;
    private final EditText inputText;

    EdittextItemViewHolder(final View itemView) {
        super(itemView);

        fieldName = itemView.findViewById(R.id.fieldName_edittext_layout);
        inputText = itemView.findViewById(R.id.textbox_edittext_layout);
    }

    TextView getFieldName() {
        return fieldName;
    }

    EditText getInputText() {
        return inputText;
    }

}
