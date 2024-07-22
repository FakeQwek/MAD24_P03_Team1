package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KeywordSearchActivity extends AppCompatActivity {

    private static final int BOOKMARK_REQUEST_CODE = 1;
    private EditText keywordSearchBar;
    private ListView keywordListView;
    private KeywordAdapter adapter;
    private List<String> keywords;
    private RequestQueue requestQueue;
    private BookmarkManager bookmarkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyword_search);

        keywordSearchBar = findViewById(R.id.keyword_search_bar);
        keywordListView = findViewById(R.id.keyword_list_view);

        requestQueue = Volley.newRequestQueue(this);

        // Initialize the keyword list
        keywords = new ArrayList<>();
        bookmarkManager = BookmarkManager.getInstance(this);
        adapter = new KeywordAdapter(this, keywords);
        keywordListView.setAdapter(adapter);

        keywordSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchKeywords(s.toString());
                } else {
                    keywords.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        keywordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedKeyword = keywords.get(position);
                Intent intent = new Intent(KeywordSearchActivity.this, Dictionary.class);
                intent.putExtra("selected_word", selectedKeyword);
                startActivity(intent);
            }
        });
    }

    private void searchKeywords(String searchTerm) {
        String url = "https://api.datamuse.com/words?sp=" + searchTerm + "*";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(KeywordSearchActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(stringRequest);
    }

    private void parseJson(String jsonStr) {
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            keywords.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String word = jsonObject.getString("word");
                keywords.add(word);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BOOKMARK_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged();
        }
    }
}
