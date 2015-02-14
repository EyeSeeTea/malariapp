package org.eyeseetea.malariacare.questions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Questions {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Question> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Question> ITEM_MAP = new HashMap<>();

    static {
        // Add 3 sample items.
        addItem(new Question("1", "Item 1"));
        addItem(new Question("2", "Item 2"));
        addItem(new Question("3", "Item 3"));
    }

    private static void addItem(Question item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Question {
        public String id;
        public String content;

        public Question(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
