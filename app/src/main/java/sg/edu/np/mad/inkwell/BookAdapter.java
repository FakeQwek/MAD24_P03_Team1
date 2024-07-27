package sg.edu.np.mad.inkwell;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
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
        ProgressBar progressBar;
        TextView progressText;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImageView = itemView.findViewById(R.id.bookCoverImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            bookAuthorTextView = itemView.findViewById(R.id.bookAuthorTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showEditBookDialog(books.get(position));
                }
            });
        }

        public void bind(Book book) {
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
            Glide.with(itemView.getContext()).load(book.getCoverUrl()).into(bookCoverImageView);
            updateProgressBar(book);

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

        private void showEditBookDialog(Book book) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_book, null);
            builder.setView(dialogView);

            EditText editTitleEditText = dialogView.findViewById(R.id.editTitleEditText);
            EditText editAuthorEditText = dialogView.findViewById(R.id.editAuthorEditText);
            EditText editCoverUrlEditText = dialogView.findViewById(R.id.editCoverUrlEditText);
            EditText editNotesEditText = dialogView.findViewById(R.id.editNotesEditText);
            Spinner pagesSpinner = dialogView.findViewById(R.id.pagesSpinner);
            Spinner doneSpinner = dialogView.findViewById(R.id.doneSpinner);
            Button removeBookButton = dialogView.findViewById(R.id.removeBookButton);
            Button saveBookButton = dialogView.findViewById(R.id.saveBookButton);

            editTitleEditText.setText(book.getTitle());
            editAuthorEditText.setText(book.getAuthor());
            editCoverUrlEditText.setText(book.getCoverUrl());
            editNotesEditText.setText(book.getNotes());

            // Populate spinners
            ArrayAdapter<Integer> pagesAdapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, generateNumberList(1, 1000));
            pagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pagesSpinner.setAdapter(pagesAdapter);
            pagesSpinner.setSelection(book.getTotalPages() - 1);

            ArrayAdapter<Integer> doneAdapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, generateNumberList(0, book.getTotalPages()));
            doneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            doneSpinner.setAdapter(doneAdapter);
            doneSpinner.setSelection(book.getCurrentPage());

            builder.setTitle("Edit Reading List");
            AlertDialog dialog = builder.create();

            removeBookButton.setOnClickListener(v -> {
                onBookChangeListener.onBookRemoved(book);
                dialog.dismiss();
            });

            saveBookButton.setOnClickListener(v -> {
                book.setTitle(editTitleEditText.getText().toString());
                book.setAuthor(editAuthorEditText.getText().toString());
                book.setCoverUrl(editCoverUrlEditText.getText().toString());
                book.setNotes(editNotesEditText.getText().toString());
                book.setTotalPages(pagesSpinner.getSelectedItemPosition() + 1);
                book.setCurrentPage(doneSpinner.getSelectedItemPosition());

                updateProgressBar(book);
                onBookChangeListener.onBookUpdated(book);
                dialog.dismiss();
            });

            dialog.show();
        }

        private void updateProgressBar(Book book) {
            int progress = book.getProgress();
            progressBar.setProgress(progress);
            progressText.setText(String.format("%d%%/100%%", progress));
            if (progress == 100) {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.GRAY));
            }
        }

        private List<Integer> generateNumberList(int start, int end) {
            List<Integer> list = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                list.add(i);
            }
            return list;
        }
    }

    public interface OnBookChangeListener {
        void onBookUpdated(Book book);
        void onBookRemoved(Book book);
    }
}
