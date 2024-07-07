package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.ApiStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Declaration of variables

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    String currentFirebaseUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private ArrayList<Object> allNotes;

    private NotesActivity notesActivity;

    // NotesAdapter constructor
    public NotesAdapter(ArrayList<Object> allNotes, NotesActivity notesActivity) {
        this.allNotes = allNotes;
        this.notesActivity = notesActivity;
    }

    // Returns an int based on whether the item is a file or not
    @Override
    public int getItemViewType(int position) {
        if (allNotes.get(position).getClass().getName().equals("sg.edu.np.mad.inkwell.File")) {
            return 0;
        } else {
            return 2;
        }
    }

    // NotesAdapter onCreateViewHolder
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file, viewGroup, false);
            RecyclerView.ViewHolder holder = new FileViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder, viewGroup, false);
            RecyclerView.ViewHolder holder = new FolderViewHolder(view);
            return holder;
        }
    }

    // NotesAdapter onBindViewHolder
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // If item is a file
        if (holder.getItemViewType() == 0) {
            FileViewHolder fileViewHolder = (FileViewHolder) holder;
            File file = (File) allNotes.get(position);
            fileViewHolder.fileButton.setText(file.getTitle());

            if (!NotesActivity.fileIds.contains(file.id)) {
                NotesActivity.fileIds.add(file.id);
                NotesActivity.files.add(file);
            }

            EditText noteTitle = notesActivity.findViewById(R.id.noteTitle);

            EditText noteBody = notesActivity.findViewById(R.id.noteBody);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

            fileViewHolder.fileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (noteTitle.getVisibility() == View.GONE) {
                        noteTitle.setVisibility(View.VISIBLE);
                        noteBody.setVisibility(View.VISIBLE);
                    }

                    NotesActivity.selectedNoteId = file.getId();
                    Log.d("tester11", String.valueOf(NotesActivity.fileOrderIndex));

                    if (NotesActivity.fileOrderIndex == -1) {
                        NotesActivity.fileOrderIndex++;
                    }
                    else if (NotesActivity.fileOrder.get(NotesActivity.fileOrderIndex) != file) {
                        NotesActivity.fileOrderIndex++;
                    }

                    Log.d("tester11", String.valueOf(NotesActivity.fileOrderIndex));

                    noteTitle.setText(file.getTitle());
                    noteBody.setText(file.getBody());

                    if (NotesActivity.fileOrder.isEmpty()) {
                        NotesActivity.fileOrder.add(file);
                        fileViewHolder.fileButton.setEnabled(true);
                    } else if (NotesActivity.fileOrder.get(NotesActivity.fileOrderIndex - 1) != file) {
                        if (NotesActivity.fileOrderIndex != NotesActivity.fileOrder.size()) {
                            int count = NotesActivity.fileOrder.size() - NotesActivity.fileOrderIndex;
                            for (int i = 0; i < count; i++) {
                                NotesActivity.fileOrder.remove(NotesActivity.fileOrder.size() - 1);
                            }
                        }
                        NotesActivity.fileOrder.add(file);
                    }
                }
            });

            fileViewHolder.fileButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Create new BottomSheetDialog to show note_bottom_sheet.xml
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                    View view = LayoutInflater.from(notesActivity).inflate(R.layout.note_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    // Get bottomSheetDeleteButton Button and set text
                    Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
                    bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

                    // Set on click listener to bottomSheetDeleteButton to delete note
                    bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            file.docRef.update("type", "deleted");

                            fileViewHolder.fileButton.setVisibility(View.GONE);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    Button fileInformationButton = view.findViewById(R.id.fileInformationButton);
                    fileInformationButton.setText("Note Information");
                    fileInformationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();

                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                            View view = LayoutInflater.from(notesActivity).inflate(R.layout.notes_information_bottom_sheet, null);
                            bottomSheetDialog.setContentView(view);
                            bottomSheetDialog.show();

                            TextView dateCreated = view.findViewById(R.id.dateCreated);
                            dateCreated.setText(simpleDateFormat.format(file.dateCreated));

                            TextView dateUpdated = view.findViewById(R.id.dateUpdated);
                            dateUpdated.setText(simpleDateFormat.format(file.dateUpdated));
                        }
                    });

                    Button shareButton = view.findViewById(R.id.shareButton);
                    shareButton.setText("Share With Community");

                    shareButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map<String, Object> communityNoteData = new HashMap<>();
                            communityNoteData.put("title", file.getTitle());
                            communityNoteData.put("body", file.getBody());
                            communityNoteData.put("email", currentFirebaseUserEmail);
                            communityNoteData.put("uid", currentFirebaseUserUid);

                            db.collection("community").document().set(communityNoteData);
                        }
                    });
                    return false;
                }
            });

            // Set text change listener on noteTitle
            noteTitle.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(noteTitle) {
                @Override
                public void onTextChanged(EditText noteTitle, Editable s) {
                    // Update note title data in firebase
                    if (file.id == NotesActivity.selectedNoteId) {
                        file.docRef.update("title", noteTitle.getText().toString());
                        file.setTitle(noteTitle.getText().toString());
                        fileViewHolder.fileButton.setText(noteTitle.getText().toString());

                        Date currentDate = Calendar.getInstance().getTime();

                        String dateString = simpleDateFormat.format(currentDate);

                        file.docRef.update("dateUpdated", dateString);

                        file.setDateUpdated(currentDate);
                    }
                }
            });

            // Set text change listener on noteBody
            noteBody.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(noteBody) {
                @Override
                public void onTextChanged(EditText noteBody, Editable s) {
                    // Update note body data in firebase
                    if (file.id == NotesActivity.selectedNoteId) {
                        file.docRef.update("body", noteBody.getText().toString());
                        file.setBody(noteBody.getText().toString());

                        Date currentDate = Calendar.getInstance().getTime();

                        String dateString = simpleDateFormat.format(currentDate);

                        file.docRef.update("dateUpdated", dateString);

                        file.setDateUpdated(currentDate);
                    }
                }
            });
            // If item is a folder
        } else {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            Folder folder = (Folder) allNotes.get(position);
            folderViewHolder.folderButton.setText(folder.getTitle());
            ArrayList<Object> folderAllNotes = new ArrayList<>();

            recyclerView(folderAllNotes, folderViewHolder.recyclerView);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

            if (folder.bookmarkColour.equals("red")) {
                folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                folderViewHolder.bookmark.setColorFilter(Color.parseColor("#e23a2e"));
            } else if (folder.bookmarkColour.equals("blue")) {
                folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                folderViewHolder.bookmark.setColorFilter(Color.parseColor("#1a73e8"));
            } else if (folder.bookmarkColour.equals("yellow")) {
                folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                folderViewHolder.bookmark.setColorFilter(Color.parseColor("#fbbf12"));
            } else if (folder.bookmarkColour.equals("green")) {
                folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                folderViewHolder.bookmark.setColorFilter(Color.parseColor("#279847"));
            }

            folder.colRef.document(String.valueOf(folder.id)).collection("files")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String docNoteType = document.getData().get("type").toString();
                                    if (docNoteType.equals("file")) {
                                        if (Integer.parseInt(document.getId()) > NotesActivity.currentNoteId) {
                                            NotesActivity.currentNoteId = Integer.parseInt(document.getId());
                                        }

                                        File file;
                                        try {
                                            file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference(), simpleDateFormat.parse(document.getData().get("dateCreated").toString()), simpleDateFormat.parse(document.getData().get("dateUpdated").toString()));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        folderAllNotes.add(file);
                                        folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());
                                    } else if (docNoteType.equals("folder")) {
                                        if (Integer.parseInt(document.getId()) > NotesActivity.currentNoteId) {
                                            NotesActivity.currentNoteId = Integer.parseInt(document.getId());
                                        }

                                        Folder folder2;
                                        try {
                                            folder2 = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, folder.colRef.document(String.valueOf(folder.id)).collection("files"), simpleDateFormat.parse(document.getData().get("dateCreated").toString()), simpleDateFormat.parse(document.getData().get("dateUpdated").toString()), document.getData().get("bookmarkColour").toString());
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        folderAllNotes.add(folder2);
                                        folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());
                                    }
                                }
                            } else {
                                Log.d("testing", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            // Toggles visibility of the children elements of a folder
            folderViewHolder.folderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (folderViewHolder.recyclerView.getVisibility() == View.VISIBLE) {
                        folderViewHolder.recyclerView.setVisibility(View.GONE);
                        folderViewHolder.chevron.setImageResource(R.drawable.chevron_up);
                    } else {
                        folderViewHolder.recyclerView.setVisibility(View.VISIBLE);
                        folderViewHolder.chevron.setImageResource(R.drawable.chevron_down);
                    }
                }
            });

            // On long clicking a folder bring up a menu
            folderViewHolder.folderButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Create new BottomSheetDialog to show folder_bottom_sheet.xml
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                    View view = LayoutInflater.from(notesActivity).inflate(R.layout.folder_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    // Get bottomSheetNewFileButton Button and set text
                    Button bottomSheetNewFileButton = view.findViewById(R.id.bottomSheetNewNoteButton);
                    bottomSheetNewFileButton.setText(R.string.bottom_sheet_new_note_button);

                    // Set on click listener to bottomSheetNewFileButton to create new file
                    bottomSheetNewFileButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotesActivity.currentNoteId++;

                            Date currentDate = Calendar.getInstance().getTime();

                            String dateString = simpleDateFormat.format(currentDate);

                            Map<String, Object> fileData = new HashMap<>();
                            fileData.put("title", "Title");
                            fileData.put("body", "Enter your text");
                            fileData.put("type", "file");
                            fileData.put("dateCreated", dateString);
                            fileData.put("dateUpdated", dateString);

                            folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)).set(fileData);

                            File file = new File("Title", "Enter your text", NotesActivity.currentNoteId, "file", folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)), currentDate, currentDate);
                            NotesActivity.fileIds.add(file.id);
                            NotesActivity.files.add(file);
                            folderAllNotes.add(0, file);
                            folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());

                            folder.colRef.document(String.valueOf(folder.id)).update("dateUpdated", dateString);

                            folder.setDateUpdated(currentDate);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_added, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    // Get bottomSheetNewFolderButton Button and set text
                    Button bottomSheetNewFolderButton = view.findViewById(R.id.bottomSheetNewFolderButton);
                    bottomSheetNewFolderButton.setText(R.string.bottom_sheet_new_folder_button);

                    // Set on click listener to bottomSheetNewFolderButton to create new folder
                    bottomSheetNewFolderButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotesActivity.currentNoteId++;

                            Date currentDate = Calendar.getInstance().getTime();

                            String dateString = simpleDateFormat.format(currentDate);

                            Map<String, Object> folderData = new HashMap<>();
                            folderData.put("title", "Folder");
                            folderData.put("body", "");
                            folderData.put("type", "folder");
                            folderData.put("dateCreated", dateString);
                            folderData.put("dateUpdated", dateString);
                            folderData.put("bookmarkColour", "none");

                            folder.colRef.document(String.valueOf(folder.id)).collection("files").document(String.valueOf(NotesActivity.currentNoteId)).set(folderData);

                            Folder folder2 = new Folder("Folder", "", NotesActivity.currentNoteId, "folder", folder.colRef.document(String.valueOf(folder.id)).collection("files"), currentDate, currentDate, "none");
                            folderAllNotes.add(0, folder2);
                            folderViewHolder.recyclerView.getAdapter().notifyItemInserted(folderViewHolder.getAdapterPosition());

                            folder.colRef.document(String.valueOf(folder.id)).update("dateUpdated", dateString);

                            folder.setDateUpdated(currentDate);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_added, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    // Get bottomSheetRenameButton Button and set text
                    Button bottomSheetRenameButton = view.findViewById(R.id.bottomSheetRenameButton);
                    bottomSheetRenameButton.setText(R.string.bottom_sheet_rename_button);

                    // Set on click listener to bottomSheetRenameButton to rename folder
                    bottomSheetRenameButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View renamePopupView = LayoutInflater.from(notesActivity).inflate(R.layout.rename_popup, null);

                            PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                            TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

                            // Get renameButton Button
                            Button renameButton = renamePopupView.findViewById(R.id.renameButton);

                            // Set on click listener to renameButton to open rename_popup.xml
                            renameButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folder.colRef.document(String.valueOf(folder.id)).update("title", renameEditText.getText().toString());

                                    Date currentDate = Calendar.getInstance().getTime();

                                    String dateString = simpleDateFormat.format(currentDate);

                                    folder.colRef.document(String.valueOf(folder.id)).update("dateUpdated", dateString);

                                    folder.setDateUpdated(currentDate);

                                    folderViewHolder.folderButton.setText(renameEditText.getText().toString());

                                    renamePopupWindow.dismiss();
                                }
                            });

                            renamePopupWindow.showAtLocation(renamePopupView, Gravity.CENTER, 0, 0);

                            bottomSheetDialog.dismiss();
                        }
                    });

                    // Get bottomSheetDeleteButton Button and set text
                    Button bottomSheetDeleteButton = view.findViewById(R.id.bottomSheetDeleteButton);
                    bottomSheetDeleteButton.setText(R.string.bottom_sheet_delete_button);

                    // Set on click listener to bottomSheetDeleteButton to delete folder
                    bottomSheetDeleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            folder.colRef.document(String.valueOf(folder.id)).update("type", "deleted");

                            folderViewHolder.constraintLayout.setVisibility(View.GONE);

                            bottomSheetDialog.dismiss();

                            Toast toast = new Toast(notesActivity);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) notesActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                            toast.setView(view);
                            toast.show();
                        }
                    });

                    Button folderInformationButton = view.findViewById(R.id.folderInformationButton);
                    folderInformationButton.setText("Folder Information");

                    folderInformationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();

                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                            View view = LayoutInflater.from(notesActivity).inflate(R.layout.notes_information_bottom_sheet, null);
                            bottomSheetDialog.setContentView(view);
                            bottomSheetDialog.show();

                            TextView dateCreated = view.findViewById(R.id.dateCreated);
                            dateCreated.setText(simpleDateFormat.format(folder.dateCreated));

                            TextView dateUpdated = view.findViewById(R.id.dateUpdated);
                            dateUpdated.setText(simpleDateFormat.format(folder.dateUpdated));
                        }
                    });

                    Button bookmarkButton = view.findViewById(R.id.bookmarkButton);
                    bookmarkButton.setText("Bookmark");

                    bookmarkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();

                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(notesActivity);
                            View view = LayoutInflater.from(notesActivity).inflate(R.layout.bookmark_colour_bottom_sheet, null);
                            bottomSheetDialog.setContentView(view);
                            bottomSheetDialog.show();

                            ImageButton none = view.findViewById(R.id.none);

                            none.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folderViewHolder.bookmark.setVisibility(View.GONE);
                                    folder.setBookmarkColour("none");
                                    folder.colRef.document(String.valueOf(folder.id)).update("bookmarkColour", "none");
                                }
                            });

                            ImageButton red = view.findViewById(R.id.red);

                            red.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                                    folderViewHolder.bookmark.setColorFilter(Color.parseColor("#e23a2e"));
                                    folder.setBookmarkColour("red");
                                    folder.colRef.document(String.valueOf(folder.id)).update("bookmarkColour", "red");
                                }
                            });

                            ImageButton blue = view.findViewById(R.id.blue);

                            blue.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                                    folderViewHolder.bookmark.setColorFilter(Color.parseColor("#1a73e8"));
                                    folder.setBookmarkColour("blue");
                                    folder.colRef.document(String.valueOf(folder.id)).update("bookmarkColour", "blue");
                                }
                            });

                            ImageButton yellow = view.findViewById(R.id.yellow);

                            yellow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                                    folderViewHolder.bookmark.setColorFilter(Color.parseColor("#fbbf12"));
                                    folder.setBookmarkColour("yellow");
                                    folder.colRef.document(String.valueOf(folder.id)).update("bookmarkColour", "yellow");
                                }
                            });

                            ImageButton green = view.findViewById(R.id.green);

                            green.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    folderViewHolder.bookmark.setVisibility(View.VISIBLE);
                                    folderViewHolder.bookmark.setColorFilter(Color.parseColor("#279847"));
                                    folder.setBookmarkColour("green");
                                    folder.colRef.document(String.valueOf(folder.id)).update("bookmarkColour", "green");
                                }
                            });
                        }
                    });
                    return false;
                }
            });

        }
    }

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Object> allNotes, RecyclerView recyclerView) {
        NotesAdapter adapter = new NotesAdapter(allNotes, notesActivity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(notesActivity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Returns the size of allNotes
    public int getItemCount() { return allNotes.size(); }
}
