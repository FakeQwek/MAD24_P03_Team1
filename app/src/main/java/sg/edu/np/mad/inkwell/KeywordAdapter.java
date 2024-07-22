package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class KeywordAdapter extends ArrayAdapter<String> {

    private BookmarkManager bookmarkManager;

    public KeywordAdapter(Context context, List<String> keywords) {
        super(context, 0, keywords);
        bookmarkManager = BookmarkManager.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String keyword = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_keyword, parent, false);
        }

        TextView keywordText = convertView.findViewById(R.id.keyword_text);
        ImageView bookmarkIcon = convertView.findViewById(R.id.bookmark_icon);

        keywordText.setText(keyword);
        updateBookmarkIcon(bookmarkIcon, bookmarkManager.isBookmarked(keyword));

        bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarkManager.isBookmarked(keyword)) {
                    bookmarkManager.removeBookmark(keyword);
                    updateBookmarkIcon(bookmarkIcon, false);
                } else {
                    bookmarkManager.addBookmark(keyword);
                    updateBookmarkIcon(bookmarkIcon, true);
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private void updateBookmarkIcon(ImageView bookmarkIcon, boolean isBookmarked) {
        if (isBookmarked) {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24);
        } else {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_border_24);
        }
    }
}
