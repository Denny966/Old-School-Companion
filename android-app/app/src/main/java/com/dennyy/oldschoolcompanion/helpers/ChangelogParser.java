package com.dennyy.oldschoolcompanion.helpers;

import android.content.Context;
import android.util.Xml;

import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.enums.ChangelogType;
import com.dennyy.oldschoolcompanion.models.Changelog.Changelog;
import com.dennyy.oldschoolcompanion.models.Changelog.ChangelogEntries;
import com.dennyy.oldschoolcompanion.models.Changelog.ChangelogEntry;
import com.dennyy.oldschoolcompanion.models.Changelog.Changelogs;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;


public class ChangelogParser {

    private Context context;

    private static final String TAG_CHANGELOG = "changelog";
    private static final String TAG_CHANGELOG_VERSION = "changelogversion";
    private static final String TAG_CHANGELOG_TEXT = "changelogtext";
    private static final String TAG_CHANGELOG_IMPORTANT = "changelogimportant";
    private static final String TAG_CHANGELOG_BUG = "changelogbug";
    private static final String TAG_CHANGELOG_NEW = "changelognew";

    private static final String ATTRIBUTE_VERSION_NAME = "versionName";
    private static final String ns = null;

    public ChangelogParser(Context context) {
        this.context = context;
    }

    public Changelogs parse() throws XmlPullParserException, IOException {
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.changelog)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        }
    }

    private Changelogs readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        Changelogs changelogs = new Changelogs();
        parser.require(XmlPullParser.START_TAG, ns, TAG_CHANGELOG);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_CHANGELOG_VERSION)) {
                String versionName = parser.getAttributeValue(ns, ATTRIBUTE_VERSION_NAME);
                ChangelogEntries entries = readEntries(parser);
                Changelog changelog = new Changelog(versionName, entries);
                changelogs.add(changelog);
            }
            else {
                skip(parser);
            }
        }
        return changelogs;
    }

    private ChangelogEntries readEntries(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_CHANGELOG_VERSION);
        ChangelogEntries entries = new ChangelogEntries();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case TAG_CHANGELOG_TEXT: {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.text = readChangelog(parser, TAG_CHANGELOG_TEXT);
                    entry.changelogType = ChangelogType.REGULAR;
                    entries.add(entry);
                    break;
                }
                case TAG_CHANGELOG_NEW: {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.text = readChangelog(parser, TAG_CHANGELOG_NEW);
                    entry.changelogType = ChangelogType.NEW;
                    entries.add(entry);
                    break;
                }
                case TAG_CHANGELOG_BUG: {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.text = readChangelog(parser, TAG_CHANGELOG_BUG);
                    entry.changelogType = ChangelogType.BUG_FIX;
                    entries.add(entry);
                    break;
                }
                case TAG_CHANGELOG_IMPORTANT: {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.text = readChangelog(parser, TAG_CHANGELOG_IMPORTANT);
                    entry.changelogType = ChangelogType.IMPORTANT;
                    entries.add(entry);
                    break;
                }
                default:
                    skip(parser);
                    break;
            }
        }
        return entries;
    }

    private String readChangelog(XmlPullParser parser, String requireTag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, requireTag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, requireTag);
        return title;
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}