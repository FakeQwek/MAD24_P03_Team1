package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarkCollectionActivity extends AppCompatActivity {

    private ListView bookmarkListView;
    private BookmarkAdapter adapter;
    private List<String> bookmarkedWords;
    private BookmarkManager bookmarkManager;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_collection);

        bookmarkListView = findViewById(R.id.bookmark_list_view);
        backArrow = findViewById(R.id.back_arrow);

        bookmarkManager = BookmarkManager.getInstance(this);
        Set<String> bookmarks = bookmarkManager.getBookmarkedWords();
        bookmarkedWords = new ArrayList<>(bookmarks);

        adapter = new BookmarkAdapter(this, bookmarkedWords);
        bookmarkListView.setAdapter(adapter);

        bookmarkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedWord = bookmarkedWords.get(position);
                bookmarkManager.removeBookmark(selectedWord);
                bookmarkedWords.remove(selectedWord);
                adapter.notifyDataSetChanged();
                notifyBookmarkChange(selectedWord, false);
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void notifyBookmarkChange(String word, boolean isBookmarked) {
        Intent intent = new Intent();
        intent.putExtra("word", word);
        intent.putExtra("isBookmarked", isBookmarked);
        setResult(RESULT_OK, intent);
    }

    private class BookmarkAdapter extends ArrayAdapter<String> {

        private Context context;

        public BookmarkAdapter(Context context, List<String> bookmarks) {
            super(context, 0, bookmarks);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String keyword = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_keyword, parent, false);
            }

            TextView keywordText = convertView.findViewById(R.id.keyword_text);
            ImageView bookmarkIcon = convertView.findViewById(R.id.bookmark_icon);

            keywordText.setText(keyword);
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24);

            bookmarkIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookmarkManager.removeBookmark(keyword);
                    bookmarkedWords.remove(keyword);
                    notifyBookmarkChange(keyword, false);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}
