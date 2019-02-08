package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.Diaries;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiariesMap;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.Diary;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.DiaryRequirement;
import com.dennyy.oldschoolcompanion.models.AchievementDiary.MissingRequirement;

import java.util.ArrayList;
import java.util.Map;

public class DiaryListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private DiariesMap diariesMap;
    private ArrayList<String> headers = new ArrayList<>();
    private LayoutInflater inflater;
    private String[] stats;

    public DiaryListAdapter(Context context, DiariesMap diariesMap) {
        this.context = context;
        this.diariesMap = diariesMap;
        this.inflater = LayoutInflater.from(context);
        this.stats = new String[]{};
        for (Map.Entry<String, Diaries> kvp : diariesMap.entrySet()) {
            headers.add(kvp.getKey());
        }
    }

    @Override
    public int getGroupCount() {
        return diariesMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return diariesMap.get(headers.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public Diary getChild(int groupPosition, int childPosition) {
        return diariesMap.get(headers.get(groupPosition)).get(childPosition);
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

        final Diary diary = getChild(groupPosition, childPosition);
        final ArrayList<MissingRequirement> missingRequirements = diary.getMissingRequirements(stats);
        boolean canComplete = missingRequirements != null && missingRequirements.size() < 1;
        textViewWrapper.setBackground(context.getDrawable(canComplete ? R.drawable.diary_can_complete_background : R.drawable.diary_cannot_complete_background));
        statusImage.setImageDrawable(context.getDrawable(canComplete ? R.drawable.baseline_done_24 : R.drawable.baseline_close_24));
        textView.setText(diary.diaryType.toString());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reqsTextView.getVisibility() == View.VISIBLE) {
                    reqsTextView.setVisibility(View.GONE);
                    return;
                }
                reqsTextView.setText(getRequirements(diary, missingRequirements));
                reqsTextView.setVisibility(View.VISIBLE);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public void updateStats(String[] stats) {
        this.stats = stats;
        notifyDataSetChanged();
    }

    private String getRequirements(Diary diary, ArrayList<MissingRequirement> missingRequirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(context.getString(R.string.diary_requirements));
        if (missingRequirements != null) {
            for (MissingRequirement missingRequirement : missingRequirements) {
                sb.append("\n").append(context.getString(R.string.diary_level_needed, missingRequirement.skill, missingRequirement.requiredLevel, missingRequirement.getDifference(), missingRequirement.currentLevel));
            }
        }
        else {
            for (DiaryRequirement diaryRequirement : diary.requirements) {
                sb.append("\n").append(context.getString(R.string.diary_skill_requirement, diaryRequirement.requiredLevel, diaryRequirement.skill));
            }
        }
        //sb.append("\n");

        //sb.append("\n\n").append(context.getString(R.string.quest_requirements));

        for (String questRequirement : diary.questRequirements) {
            sb.append("\n").append(questRequirement);
        }
        return sb.toString();
    }

    private static class ViewHolder {
        public TextView textView;
    }
}
