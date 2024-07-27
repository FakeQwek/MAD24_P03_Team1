package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {

    private static BookmarkManager instance;
    private SharedPreferences sharedPreferences;
    private Set<String> bookmarkedWords;

    private BookmarkManager(Context context) {
        sharedPreferences = context.getSharedPreferences("Bookmarks", Context.MODE_PRIVATE);
        bookmarkedWords = sharedPreferences.getStringSet("bookmarkedWords", new HashSet<>());
    }

    public static synchronized BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context);
        }
        return instance;
    }

    public Set<String> getBookmarkedWords() {
        return new HashSet<>(bookmarkedWords);
    }

    public boolean isBookmarked(String word) {
        return bookmarkedWords.contains(word);
    }

    public void addBookmark(String word) {
        bookmarkedWords.add(word);
        saveBookmarks();
    }

    public void removeBookmark(String word) {
        bookmarkedWords.remove(word);
        saveBookmarks();
    }

    private void saveBookmarks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("bookmarkedWords", bookmarkedWords);
        editor.apply();
    }
}
