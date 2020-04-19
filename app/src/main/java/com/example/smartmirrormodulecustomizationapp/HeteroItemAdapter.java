package com.example.smartmirrormodulecustomizationapp;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
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

    // field name and config list of module configs
    private final List<String> fieldNames;
    private final List<Object> configs;

    // stores range of integer type field (field name, (min, max))
    private final Map<String, Pair<Integer, Integer>> rangeMap;

    /**
     * Constructor for module without integer type field
     *
     * @param itemMap stores (field name, config)
     */
    HeteroItemAdapter(final Map<String, Object> itemMap) {
        fieldNames = new ArrayList<>(itemMap.keySet());
        configs = new ArrayList<>(itemMap.values());

        rangeMap = null;
    }

    /**
     * Constructor for module with integer type field
     *
     * @param itemMap  stores (field name, config)
     * @param rangeMap stores (field name, (min, max))
     */
    HeteroItemAdapter(final Map<String, Object> itemMap,
                      final Map<String, Pair<Integer, Integer>> rangeMap) {
        fieldNames = new ArrayList<>(itemMap.keySet());
        configs = new ArrayList<>(itemMap.values());

        this.rangeMap = rangeMap;
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
     * Set the field and integer value of the seek bar type row;
     * Set seek bar change action to display progress number
     *
     * @param viewHolder The seek bar type view holder
     * @param position   The row index
     */
    @SuppressLint("SetTextI18n")
    private void configureSeekbarItemViewHolder(SeekbarItemViewHolder viewHolder, int position) {

        // get field name and integer value
        final String fieldName = fieldNames.get(position);
        final Integer intValue = (Integer) configs.get(position);
        viewHolder.getFieldName().setText(fieldName);

        // get min and max value of the integer type field field
        final Pair<Integer, Integer> rangePair = rangeMap.get(fieldName);
        assert rangePair != null;
        final int rangeMin = rangePair.first, rangeMax = rangePair.second;

        // set seek bar progress and progress display
        final SeekBar bar = viewHolder.getBar();
        final TextView progressView = viewHolder.getProgress();
        final int barProgress = getProgressOfValue(intValue, rangeMin, rangeMax);
        progressView.setText(Integer.toString(intValue));
        progressView.setX(getProgressPos(bar, barProgress, Integer.toString(intValue)));
        bar.setProgress(barProgress);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Display progress along with seek bar movement
             *
             * @param seekBar  the seek bar whose progress would be shown
             * @param progress the progress of seek bar
             * @param fromUser dummy argument
             */
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress,
                                          final boolean fromUser) {

                final String progressString = Integer.toString(getValueOfProgress(
                        progress, rangeMin, rangeMax));
                progressView.setText(progressString);
                progressView.setX(getProgressPos(seekBar, progress, progressString));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    /**
     * Set the field and string value of the edit text type row
     *
     * @param viewHolder The edit text type view holder
     * @param position   The row index
     */
    private void configureEdittextItemViewHolder(final EdittextItemViewHolder viewHolder,
                                                 final int position) {

        final String fieldName = fieldNames.get(position);
        final String stringValue = (String) configs.get(position);
        viewHolder.getFieldName().setText(fieldName);
        viewHolder.getInputText().setText(stringValue, TextView.BufferType.EDITABLE);

    }

    /**
     * Get the corresponding value of a progress within a range (min, max)
     *
     * @param progress the progress in [0, 100]
     * @param min      the lower bound of range
     * @param max      the upper bound of range
     * @return         the corresponding value of the progress
     */
    private int getValueOfProgress(final int progress, final int min, final int max) {

        return (int)((long)progress * ((long)max - (long)min) / 100 + (long)min);

    }

    /**
     * Get the corresponding progress of a value within a range (min, max)
     *
     * @param value the value to be converted into progress
     * @param min   the lower bound of range
     * @param max   the upper bound of range
     * @return      the corresponding progress of the value
     */
    private int getProgressOfValue(final int value, final int min, final int max) {

        return (int)(((long)value - (long)min) * 100 / (long)max);

    }

    /**
     * Get the X position of the display progress of seek bar
     *
     * @param bar            the bar whose cursor would be get X position
     * @param progress       the progress of the cursor on the bar
     * @param progressString the display string of the progress
     * @return               the X position of the display progress
     */
    private float getProgressPos(final SeekBar bar, final int progress,
                                 final String progressString) {

        int offset = progress * (525 - 2 * bar.getThumbOffset()) / bar.getMax();
        return 210 + offset + bar.getThumbOffset() / 2f - progressString.length() * 25;

    }

} // END of SwitchItemAdapter
