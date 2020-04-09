package com.example.smartmirrormodulecustomizationapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Adapter for RecyclerView that can display items of different types
 *  Supported types: switch, seek bar, and edit text
 */
public class HeteroItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // item types
    private static final int SWITCH = 1;
    private static final int SEEKBAR = 2;
    private static final int EDITTEXT = 3;

    // stores items with different types (field name, config)
    private final Map<String, Object> itemMap;
    private final List<String> fieldNames;
    private final List<Object> configs;

    HeteroItemAdapter(Map<String, Object> itemMap) {
        this.itemMap = itemMap;
        fieldNames = new ArrayList<>(itemMap.keySet());
        configs = new ArrayList<>(itemMap.values());
    }

    /**
     * Create a view holder of the given type
     *
     * @param parent   The parent module's view group
     * @param viewType The view type of the corresponding view holder to be created
     * @return         A new view holder of the corresponding type
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SWITCH:
                View switchView = inflater.inflate(R.layout.switch_item_layout, parent, false);
                viewHolder = new SwitchItemViewHolder(switchView);
                break;
            case SEEKBAR:
                View seekbarView = inflater.inflate(R.layout.seekbar_item_layout, parent, false);
                viewHolder = new SeekbarItemViewHolder(seekbarView);
                break;
            case EDITTEXT:
                View edittextView = inflater.inflate(R.layout.edittext_item_layout, parent, false);
                viewHolder = new EdittextItemViewHolder(edittextView);
                break;
            default:
                throw new IllegalStateException("Unexpected View Type: " + viewType);
        }
        return viewHolder;
    }

    /**
     * Bind a given row with a view holder
     *
     * @param viewHolder The view holder to be bound to
     * @param position   The row number
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = viewHolder.getItemViewType();
        switch (viewType) {
            case SWITCH:
                SwitchItemViewHolder switchHolder = (SwitchItemViewHolder) viewHolder;
                configureSwitchItemViewHolder(switchHolder, position);
                break;
            case SEEKBAR:
                SeekbarItemViewHolder seekbarHolder = (SeekbarItemViewHolder) viewHolder;
                configureSeekbarItemViewHolder(seekbarHolder, position);
                break;
            case EDITTEXT:
                EdittextItemViewHolder edittextHolder = (EdittextItemViewHolder) viewHolder;
                configureEdittextItemViewHolder(edittextHolder, position);
                break;
            default:
                throw new IllegalStateException("Unexpected View Type: " + viewType);
        }
    }

    /**
     * Get the count of config items
     *
     * @return The count of config items
     */
    @Override
    public int getItemCount() {
        return configs.size();
    }

    /**
     * Get the type of item at given position
     *
     * @param position The row index
     * @return         The type of corresponding item
     */
    @Override
    public int getItemViewType(int position) {
        if (configs.get(position) instanceof Boolean) {
            return SWITCH;
        } else if (configs.get(position) instanceof Integer) {
            return SEEKBAR;
        } else if (configs.get(position) instanceof String) {
            return EDITTEXT;
        }
        return -1;
    }

    /**
     * Set the field and boolean value of the switch type row
     *
     * @param viewHolder The switch type view holder
     * @param position   The row index
     */
    private void configureSwitchItemViewHolder(SwitchItemViewHolder viewHolder, int position) {
        final String fieldName = fieldNames.get(position);
        final Boolean boolValue = (Boolean) configs.get(position);
        viewHolder.getFieldName().setText(fieldName);
        viewHolder.getSwitchButton().setChecked(boolValue);
    }

    /**
     * Set the field and integer value of the seek bar type row
     *
     * @param viewHolder The seek bar type view holder
     * @param position   The row index
     */
    private void configureSeekbarItemViewHolder(SeekbarItemViewHolder viewHolder, int position) {
        final String fieldName = fieldNames.get(position);
        final Integer intValue = (Integer) configs.get(position);
        viewHolder.getFieldName().setText(fieldName);
        viewHolder.getBar().setProgress(intValue);
    }

    /**
     * Set the field and string value of the edit text type row
     *
     * @param viewHolder The edit text type view holder
     * @param position   The row index
     */
    private void configureEdittextItemViewHolder(EdittextItemViewHolder viewHolder, int position) {
        final String fieldName = fieldNames.get(position);
        final String stringValue = (String) configs.get(position);
        viewHolder.getFieldName().setText(fieldName);
        viewHolder.getInputText().setText(stringValue, TextView.BufferType.EDITABLE);
    }

} // END of SwitchItemAdapter
