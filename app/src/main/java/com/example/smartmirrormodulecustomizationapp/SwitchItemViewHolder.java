package com.example.smartmirrormodulecustomizationapp;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 *  View holder for items of type switch
 *  Suitable for boolean type values
 */
class SwitchItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView fieldName;
    private final Switch switchButton;

    SwitchItemViewHolder(final View itemView) {
        super(itemView);

        fieldName = itemView.findViewById(R.id.fieldName_switch_layout);
        switchButton = itemView.findViewById(R.id.button_switch_layout);
    }

    TextView getFieldName() {
        return fieldName;
    }

    Switch getSwitchButton() {
        return switchButton;
    }

}
