package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.models.AchievementDiary.Diary;
import com.dennyy.osrscompanion.models.AchievementDiary.DiaryLevel;
import com.dennyy.osrscompanion.models.AchievementDiary.DiaryRequirement;

import java.util.ArrayList;
import java.util.Map;

public class DiaryListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private Diary diaries;
    private ArrayList<String> headers = new ArrayList<>();
    private LayoutInflater inflater;

    public DiaryListAdapter(Context context, Diary diaries) {
        this.context = context;
        this.diaries = diaries;
        this.inflater = LayoutInflater.from(context);
        for (Map.Entry<String, ArrayList<DiaryLevel>> kvp : diaries.entrySet()) {
            headers.add(kvp.getKey());
        }
    }

    @Override
    public int getGroupCount() {
        return diaries.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return diaries.get(headers.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public DiaryLevel getChild(int groupPosition, int childPosition) {
        return diaries.get(headers.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.diary_calc_list_header, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = convertView.findViewById(R.id.diary_calc_list_header);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(getGroup(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.diary_calc_list_item, null);
        TextView textView = convertView.findViewById(R.id.diary_calc_list_item);
        LinearLayout textViewWrapper = convertView.findViewById(R.id.diary_calc_list_item_wrapper);
        ImageView statusImage = convertView.findViewById(R.id.diary_calc_list_item_status);
        final TextView reqsTextView = convertView.findViewById(R.id.diary_calc_list_item_reqs);

        final DiaryLevel diaryLevel = getChild(groupPosition, childPosition);
        textViewWrapper.setBackground(context.getDrawable(diaryLevel.canComplete() ? R.drawable.diary_can_complete_background : R.drawable.diary_cannot_complete_background));
        statusImage.setImageDrawable(context.getDrawable(diaryLevel.canComplete() ? R.drawable.baseline_done_24 : R.drawable.baseline_close_24));
        textView.setText(diaryLevel.diaryType.toString());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (diaryLevel.canComplete() || reqsTextView.getVisibility() == View.VISIBLE) {
                    reqsTextView.setVisibility(View.GONE);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n").append(context.getString(R.string.diary_requirements));
                for (DiaryRequirement diaryRequirement : diaryLevel.missingRequirements) {
                    sb.append("\n").append(context.getString(R.string.diary_level_needed, diaryRequirement.skill, diaryRequirement.requiredLevel, diaryRequirement.getDifference(), diaryRequirement.currentLevel));
                }
                reqsTextView.setText(sb.toString());
                reqsTextView.setVisibility(View.VISIBLE);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public void updateList(Diary diaries) {
        this.diaries.clear();
        this.diaries.putAll(diaries);
        this.headers.clear();
        for (Map.Entry<String, ArrayList<DiaryLevel>> kvp : diaries.entrySet()) {
            headers.add(kvp.getKey());
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView textView;
    }
}
