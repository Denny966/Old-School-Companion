package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.AdapterTimerClickListener;
import com.dennyy.oldschoolcompanion.models.Timers.Timer;

import java.util.ArrayList;

public class TimersAdapter extends GenericAdapter<Timer> {

    private AdapterTimerClickListener listener;

    public TimersAdapter(Context context, ArrayList<Timer> timers, AdapterTimerClickListener listener) {
        super(context, timers);
        this.listener = listener;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.timer_row, null);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.timer_listview_title);
            viewHolder.description = convertView.findViewById(R.id.timer_listview_description);
            viewHolder.duration = convertView.findViewById(R.id.timer_listview_duration);
            viewHolder.deleteButton = convertView.findViewById(R.id.timer_delete);
            viewHolder.confirmDeleteButton = convertView.findViewById(R.id.timer_delete_confirm);
            viewHolder.cancelButton = convertView.findViewById(R.id.timer_delete_cancel);
            viewHolder.editButton = convertView.findViewById(R.id.timer_edit);
            viewHolder.startButton = convertView.findViewById(R.id.timer_startstop);
            viewHolder.timerDeleteContainer = convertView.findViewById(R.id.timer_delete_container);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Timer timer = getItem(i);
        viewHolder.title.setText(timer.title);
        viewHolder.description.setText(timer.description);
        viewHolder.duration.setText(context.getString((timer.isRepeating ? R.string.timer_repeated : R.string.timer_once), formatSeconds(timer.interval)));
        viewHolder.startButton.setText(context.getString(timer.isActive() ? R.string.timer_stop : R.string.timer_start));
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.deleteButton.setVisibility(View.GONE);
                viewHolder.timerDeleteContainer.setVisibility(View.VISIBLE);
            }
        });
        viewHolder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.timerDeleteContainer.setVisibility(View.GONE);
                viewHolder.deleteButton.setVisibility(View.VISIBLE);
            }
        });
        viewHolder.confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onConfirmDeleteClick(timer);
            }
        });
        viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onEditClick(timer);
            }
        });
        viewHolder.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStartClick(timer);
            }
        });
        return convertView;
    }

    private String formatSeconds(int timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int secondsLeft = timeInSeconds - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String formattedTime = "";
        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + ":";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds;

        return formattedTime;
    }

    private static class ViewHolder {
        public TextView title;
        public TextView description;
        public TextView duration;
        public Button deleteButton;
        public Button cancelButton;
        public Button confirmDeleteButton;
        public Button editButton;
        public Button startButton;
        public LinearLayout timerDeleteContainer;
    }
}
