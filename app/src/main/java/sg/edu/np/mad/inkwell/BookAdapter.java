package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private OnBookChangeListener onBookChangeListener;

    public BookAdapter(List<Book> books, OnBookChangeListener onBookChangeListener) {
        this.books = books;
        this.onBookChangeListener = onBookChangeListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCoverImageView;
        TextView bookTitleTextView;
        TextView bookAuthorTextView;
        Spinner statusSpinner;
        EditText notesEditText;
        Button removeBookButton;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookCoverImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            bookAuthorTextView = itemView.findViewById(R.id.bookAuthorTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            notesEditText = itemView.findViewById(R.id.notesEditText);
            removeBookButton = itemView.findViewById(R.id.removeBookButton);

            removeBookButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBookChangeListener.onBookRemoved(books.get(position));
                }
            });

            statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        books.get(pos).setStatus(parent.getItemAtPosition(position).toString());
                        onBookChangeListener.onBookUpdated(books.get(pos));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            notesEditText.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        books.get(pos).setNotes(notesEditText.getText().toString());
                        onBookChangeListener.onBookUpdated(books.get(pos));
                    }
                }
            });
        }

        public void bind(Book book) {
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
            notesEditText.setText(book.getNotes());
            Glide.with(itemView.getContext()).load(book.getCoverUrl()).into(bookCoverImageView);

            switch (book.getStatus()) {
                case "To Read":
                    statusSpinner.setSelection(0);
                    break;
                case "Reading":
                    statusSpinner.setSelection(1);
                    break;
                case "Completed":
                    statusSpinner.setSelection(2);
                    break;
            }
        }
    }

    public interface OnBookChangeListener {
        void onBookUpdated(Book book);
        void onBookRemoved(Book book);
    }
}
