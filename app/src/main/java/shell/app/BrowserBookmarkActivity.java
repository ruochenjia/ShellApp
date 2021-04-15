package shell.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.io.CharsetV2;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.shell.R;
import com.mindrex.standardassets.E;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class BrowserBookmarkActivity extends ActivityFramework {
    private JSONArray bookmarks = new JSONArray();
    private ListView listView;
    private CustomAdaptor adaptor;

    public BrowserBookmarkActivity() {
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(listView);

        try {
            bookmarks = getBookmarks();
        } catch (Exception e) {
            Toast.makeText(this, R.string.get_saved_bookmarks_error_message, Toast.LENGTH_SHORT).show();
            E.print(this, e);
        }

        try {
            adaptor = new CustomAdaptor(bookmarks);
            listView.setAdapter(adaptor);
        } catch (Exception e) {
            E.print(this, e);
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            final String url = adaptor.getUrl(position);
            final Intent i = new Intent(BrowserBookmarkActivity.this, BrowserActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse(url), "*/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            ArrayList<String> list = new ArrayList<>();
            if (bookmarks != null) {
                for (int c = 0; c < bookmarks.length(); c++) {
                    try {
                        list.add(bookmarks.get(c).toString());
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    }
                }
            }
            list.remove(position);
            bookmarks = new JSONArray(list);
            saveBookmarks();
            refreshContent();
            return true;
        });
    }

    @Override
    public final void onResume() {
        listView.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public final void onPause() {
        try {
            saveBookmarks();
        } catch (Exception e) {
            E.print(this, e);
        }
        listView.setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onDestroy() {
        try {
            saveBookmarks();
        } catch (Exception e) {
            E.print(this, e);
        }
        super.onDestroy();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_bookmarks, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addBookmark:
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                a.setTitle(R.string.add_bookmark);
                LinearLayout b = new LinearLayout(this);
                b.setOrientation(LinearLayout.VERTICAL);
                AppCompatTextView c = new AppCompatTextView(this);
                AppCompatEditText d = new AppCompatEditText(this);
                AppCompatTextView e = new AppCompatTextView(this);
                AppCompatEditText f = new AppCompatEditText(this);
                c.setText(R.string.title);
                c.setTextColor(getResources().getColor(R.color.contents));
                c.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                d.setInputType(InputType.TYPE_CLASS_TEXT);
                d.setHint(R.string.title);
                e.setText(R.string.url);
                e.setTextColor(getResources().getColor(R.color.contents));
                e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                f.setInputType(InputType.TYPE_CLASS_TEXT);
                f.setHint(R.string.url);
                b.addView(c);
                b.addView(d);
                b.addView(e);
                b.addView(f);
                a.setView(b);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                    String urlOriginal = Objects.requireNonNull(f.getText()).toString();
                    String url;
                    if (!(urlOriginal.contains("://") || urlOriginal.contains("about:")))
                        url = "http://" + urlOriginal;
                    else
                        url = urlOriginal;
                    try {
                        final JSONObject a1 = new JSONObject();
                        a1.put("title", Objects.requireNonNull(d.getText()).toString());
                        a1.put("url", url);
                        bookmarks.put(a1);
                        saveBookmarks();
                    } catch (Exception e1) {
                        Toast.makeText(getContext(), R.string.add_bookmark_error_message, Toast.LENGTH_SHORT).show();
                    }
                    refreshContent();
                });
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            case R.id.removeBookmark:
                Toast.makeText(this, R.string.delete_bookmark_hint, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public Context getContext() {
        return this;
    }

    final void refreshContent() {
        try {
            adaptor = new CustomAdaptor(bookmarks);
            listView.setAdapter(adaptor);
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    final void saveBookmarks() {
        try {
            final JSONObject a = new JSONObject();
            a.put("formatVersion", versionCode);
            a.put("bookmarks", bookmarks);
            final File d = new File(applicationFolder, "bookmarks");
            new TextWriter(d).write(a.toString(2));
        } catch (Exception e) {
            Toast.makeText(this, R.string.save_bookmarks_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    final JSONArray getBookmarks() throws Exception {
        final File a = new File(applicationFolder, "browser_bookmarks");
        final String b = new TextReader(a).read();
        final JSONObject e = new JSONObject(b);
        return (JSONArray) e.get("bookmarks");
    }

    private static final class CustomAdaptor extends BaseAdapter {
        private String[] bookmarkTitles, bookmarkUrls;

        CustomAdaptor(JSONArray data) throws Exception {
            final List<String> titles = new ArrayList<>();
            final List<String> urls = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                final JSONObject jsonObject = (JSONObject) data.get(i);
                final String title = jsonObject.get("title").toString();
                final String url = jsonObject.get("url").toString();
                titles.add(title);
                urls.add(url);
            }
            bookmarkTitles = new String[titles.size()];
            bookmarkUrls = new String[urls.size()];
            bookmarkTitles = titles.toArray(bookmarkTitles);
            bookmarkUrls = urls.toArray(bookmarkUrls);
        }

        @Override
        public final int getCount() {
            return bookmarkTitles.length;
        }

        @Override
        public final Object getItem(final int position) {
            return position;
        }

        @Override
        public final long getItemId(final int position) {
            return position;
        }

        public final String getUrl(final int position) {
            return bookmarkUrls[position];
        }

        @Override
        public final View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_items, parent, false);
            AppCompatImageView a = convertView.findViewById(R.id.bookmarkIcon);
            AppCompatTextView b = convertView.findViewById(R.id.bookmarkTitle);
            AppCompatTextView c = convertView.findViewById(R.id.bookmarkSummary);
            b.setText(bookmarkTitles[position]);
            c.setText(bookmarkUrls[position]);
            return convertView;
        }

        @NonNull
        @Override
        public final String toString() {
            return "CustomAdaptor{" +
                    "bookmarkTitles=" + Arrays.toString(bookmarkTitles) +
                    ", bookmarkUrls=" + Arrays.toString(bookmarkUrls) +
                    '}';
        }
    }


    @NonNull
    @Override
    public final String toString() {
        return "BrowserBookmarkActivity{" +
                "bookmarks=" + bookmarks +
                ", listView=" + listView +
                ", adaptor=" + adaptor +
                '}';
    }
}