package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.QuestDifficulty;
import com.dennyy.oldschoolcompanion.enums.QuestLength;
import com.dennyy.oldschoolcompanion.enums.QuestSortType;
import com.dennyy.oldschoolcompanion.helpers.Constants;
import com.dennyy.oldschoolcompanion.interfaces.QuestListeners;
import com.dennyy.oldschoolcompanion.models.General.Quest;

import java.util.*;

public class QuestListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Quest> quests;
    private LayoutInflater inflater;
    private ArrayList<String> headers;
    private HashMap<String, ArrayList<Quest>> questsMap = new HashMap<>();
    private QuestListeners.AdapterClickListener listener;
    private QuestSortType questSortType;
    private boolean reversedSort;
    private SharedPreferences preferences;

    public QuestListAdapter(Context context, ArrayList<Quest> quests, QuestListeners.AdapterClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.quests = new ArrayList<>(quests);
        this.listener = listener;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.questSortType = QuestSortType.fromValue(preferences.getInt(Constants.PREF_QUEST_SORT_TYPE, QuestSortType.NAME.getValue()));
        this.reversedSort = preferences.getBoolean(Constants.PREF_QUEST_SORT_DIRECTION, false);
        updateSorting(this.questSortType, true);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return questsMap.get(headers.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public Quest getChild(int groupPosition, int childPosition) {
        return questsMap.get(headers.get(groupPosition)).get(childPosition);
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
            convertView = inflater.inflate(R.layout.quest_list_header, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = convertView.findViewById(R.id.quest_list_header);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (questSortType == QuestSortType.MEMBERS) {
            String group = getGroup(groupPosition);
            viewHolder.textView.setText(context.getResources().getString(group.equals(String.valueOf(true)) ? R.string.members : R.string.freetoplay));
        }
        else if (questSortType == QuestSortType.COMPLETION) {
            String group = getGroup(groupPosition);
            viewHolder.textView.setText(context.getString(group.equals(String.valueOf(true)) ? R.string.quest_completed : R.string.quest_quest_not_completed));
        }
        else {
            viewHolder.textView.setText(getGroup(groupPosition));
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.quest_list_item, null);
        TextView textView = convertView.findViewById(R.id.quest_list_name);
        TextView doneButton = convertView.findViewById(R.id.quest_list_button_done);

        final Quest quest = getChild(groupPosition, childPosition);
        textView.setText(quest.name);
        if (quest.isCompleted()) {
            doneButton.setText(context.getString(R.string.quest_undo));
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setAlpha(0.5f);
            doneButton.setAlpha(0.5f);
        }
        else {
            doneButton.setText(context.getString(R.string.quest_done));
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setAlpha(1f);
            doneButton.setAlpha(1f);
        }
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCompleted = !quest.isCompleted();
                quest.setCompleted(isCompleted);
                listener.onQuestDoneClick(quest, isCompleted);
                notifyDataSetChanged();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onQuestClick(quest);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public void updateSorting(QuestSortType questSortType) {
        updateSorting(questSortType, false);
    }

    public void updateSorting(QuestSortType questSortType, boolean keepSortOrder) {
        if (!keepSortOrder) {
            reversedSort = this.questSortType == questSortType && !reversedSort;
        }
        this.questSortType = questSortType;
        saveSortPreferences();
        Set<String> tempHeaders = new LinkedHashSet<>();

        switch (questSortType) {
            case LENGTH:
                List<QuestLength> questLengths = Arrays.asList(QuestLength.values());
                if (reversedSort) {
                    Collections.reverse(questLengths);
                }
                for (QuestLength questLength : questLengths) {
                    tempHeaders.add(questLength.name);
                }
                break;
            case DIFFICULTY:
                List<QuestDifficulty> questDifficulties = Arrays.asList(QuestDifficulty.values());
                if (reversedSort) {
                    Collections.reverse(questDifficulties);
                }
                for (QuestDifficulty questDifficulty : questDifficulties) {
                    tempHeaders.add(questDifficulty.name);
                }
                break;
            case QP:
                String[] questPointsArray = { "1", "2", "3", "4", "5", "6", "10" };
                List<String> questPoints = Arrays.asList(questPointsArray);
                if (reversedSort) {
                    Collections.reverse(questPoints);
                }
                tempHeaders.addAll(questPoints);
                break;
            case MEMBERS:
                String[] membersArray = { String.valueOf(true), String.valueOf(false) };
                List<String> members = Arrays.asList(membersArray);
                if (reversedSort) {
                    Collections.reverse(members);
                }
                tempHeaders.addAll(members);
                break;
            case COMPLETION:
                String[] completionTypesArray = { String.valueOf(true), String.valueOf(false) };
                List<String> completionTypes = Arrays.asList(completionTypesArray);
                if (reversedSort) {
                    Collections.reverse(completionTypes);
                }
                tempHeaders.addAll(completionTypes);
                break;
            default:
                for (Quest quest : quests) {
                    tempHeaders.add(quest.name.substring(0, 1));
                }
                if (reversedSort) {
                    List<String> list = new ArrayList<>(tempHeaders);
                    Collections.reverse(list);
                    tempHeaders = new LinkedHashSet<>(list);
                }
                break;

        }
        questsMap.clear();
        for (String header : tempHeaders) {
            questsMap.put(header, new ArrayList<Quest>());
        }

        for (Quest quest : quests) {
            switch (questSortType) {
                case LENGTH:
                    ArrayList<Quest> lengthSortedList = questsMap.get(String.valueOf(quest.questLength.name));
                    lengthSortedList.add(quest);
                    break;
                case DIFFICULTY:
                    ArrayList<Quest> difficultySortedList = questsMap.get(String.valueOf(quest.questDifficulty.name));
                    difficultySortedList.add(quest);
                    break;
                case QP:
                    ArrayList<Quest> qpSortedList = questsMap.get(String.valueOf(quest.questPoints));
                    qpSortedList.add(quest);
                    break;
                case MEMBERS:
                    ArrayList<Quest> membersSortedList = questsMap.get(String.valueOf(quest.isMembers));
                    membersSortedList.add(quest);
                    break;
                case COMPLETION:
                    ArrayList<Quest> completionSortedList = questsMap.get(String.valueOf(quest.isCompleted()));
                    completionSortedList.add(quest);
                    break;
                default:
                    String letter = quest.name.substring(0, 1);
                    ArrayList<Quest> questsInMap = questsMap.get(letter);
                    questsInMap.add(quest);
                    break;
            }
        }

        headers = new ArrayList<>(tempHeaders);
        notifyDataSetChanged();
    }

    private void saveSortPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_QUEST_SORT_TYPE, questSortType.getValue());
        editor.putBoolean(Constants.PREF_QUEST_SORT_DIRECTION, reversedSort);
        editor.apply();
    }

    public QuestSortType getQuestSortType() {
        return questSortType;
    }

    private static class ViewHolder {
        public TextView textView;
        public TextView doneButton;
    }
}
