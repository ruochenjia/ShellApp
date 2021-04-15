package shell.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.mindrex.io.IO;
import com.mindrex.shell.R;
import com.mindrex.standardassets.CommandUtils;
import com.mindrex.standardassets.E;
import com.mindrex.standardassets.Filex;
import com.mindrex.util.Sfx;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import shell.app.IconAlertDialogAdaptor.Item;

// Notice: This file might have been changed from original.

public final class ApkExtractor extends ActivityFramework implements TextWatcher, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, Handler.Callback {
    private ListView listView;
    private AppCompatEditText searchBar;
    private CustomAdaptor adaptor;
    private ProgressDialog dialog;
    private View bottomBar;
    private List<ApplicationInfo> info, info1;
    private final Handler handler;

    private AlertDialog.Builder menu;
    private final Item[] menu1 = new Item[]{
            new Item(R.string.extract_apk, R.drawable.package_extractor),
            new Item(R.string.application_information, R.drawable.about),
            new Item(R.string.manage_application, R.drawable.developer_tools),
            new Item(R.string.disable_package, R.drawable.disable),
            new Item(R.string.uninstall_package, R.drawable.delete)
    };
    private final Item[] menu2 = new Item[]{
            new Item(R.string.extract_apk, R.drawable.package_extractor),
            new Item(R.string.application_information, R.drawable.about),
            new Item(R.string.manage_application, R.drawable.developer_tools),
            new Item(R.string.enable_package, R.drawable.enable),
            new Item(R.string.uninstall_package, R.drawable.delete)
    };

    public ApkExtractor() {
        this.handler = new Handler(Looper.myLooper(), this);
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apk_extractor);
        listView = findViewById(R.id.mainContent);
        searchBar = findViewById(R.id.searchBar);
        bottomBar = findViewById(R.id.bottomBar);

        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        dialog = ProgressDialog.show(ApkExtractor.this, getResources().getString(R.string.loading), getResources().getString(R.string.please_wait));
        dialog.show();
        menu = new AlertDialog.Builder(this);
        menu.setPositiveButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
        handler.post(this);
        findViewById(R.id.cancelButton).setOnClickListener(view -> {
            adaptor.removeSelections();
            bottomBar.setVisibility(View.GONE);
        });
        findViewById(R.id.extractButton).setOnClickListener(view -> {
            final List<Integer> a = new ArrayList<>();
            for (int i = 0; i < adaptor.selection.length; i++)
                if (adaptor.isSelected(i)) a.add(i);
            extractApk(a.toArray(new Integer[0]));
            adaptor.removeSelections();
            bottomBar.setVisibility(View.GONE);
        });
    }

    @Override
    public final boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case 1:
                adaptor = new CustomAdaptor(info);
                listView.setAdapter(adaptor);
                listView.setOnItemClickListener(ApkExtractor.this);
                listView.setOnItemLongClickListener(ApkExtractor.this);
                searchBar.addTextChangedListener(ApkExtractor.this);
            case 0:
                dialog.dismiss();
                return true;
            default:
                return false;
        }
    }

    @Override
    public final void run() {
        info = new ArrayList<>();
        info1 = new ArrayList<>();
        try {
            info.addAll(ApplicationInfo.getInstalledApplicationList(this));
            info1.addAll(info);
        } catch (Exception e) {
            E.print(this, e);
        }
        handler.sendEmptyMessage(1);
    }

    @Override
    public final void onResume() {
        getContentView().setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apk_extractor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchApp:
                if (searchBar.getVisibility() == View.GONE)
                    searchBar.setVisibility(View.VISIBLE);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public final void onBackPressed() {
        if (searchBar.getVisibility() != View.GONE) {
            searchBar.setText("");
            searchBar.setVisibility(View.GONE);
        } else finish();
    }

    @Override
    public final void beforeTextChanged(final CharSequence s, int start, int count, int after) {
    }

    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adaptor.setSelection(position, !adaptor.isSelected(position));
        if (adaptor.selectedItemCount > 0) bottomBar.setVisibility(View.VISIBLE);
        else bottomBar.setVisibility(View.GONE);
    }

    @Override
    public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final String s = adaptor.getItem(position).packageName;
        menu.setTitle(adaptor.getItem(position).label);
        try {
            if (adaptor.getItem(position).enabled)
                menu.setAdapter(new IconAlertDialogAdaptor(menu1), (dialog, which) -> {
                    switch (which) {
                        case 0:
                            extractApk(new Integer[]{position});
                            break;
                        case 1:
                            // unf
                            break;
                        case 2:
                            try {
                                final Intent i = new Intent();
                                i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.setData(Uri.parse("package:" + s));
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivity(i);
                            } catch (Exception e) {
                                E.print(view.getContext(), e);
                            }
                            break;
                        case 3:
                            if (CommandUtils.hasRootAccess)
                                try {
                                    CommandUtils.executeWithError("su", "pm disable " + s, "exit");
                                } catch (Exception t) {
                                    E.print(view.getContext(), t);
                                }
                            else RootRequiredDialog.show(view.getContext());
                            break;
                        case 4:
                            if (CommandUtils.hasRootAccess)
                                try {
                                    CommandUtils.executeWithError("su", "pm uninstall " + s, "exit");
                                } catch (Exception t) {
                                    E.print(view.getContext(), t);
                                }
                            else RootRequiredDialog.show(view.getContext());
                            break;
                        default:
                            break;
                    }
                });
            else menu.setAdapter(new IconAlertDialogAdaptor(menu2), (dialog, which) -> {
                switch (which) {
                    case 0:
                        extractApk(new Integer[]{position});
                        break;
                    case 1:
                        // unf
                        break;
                    case 2:
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setData(Uri.parse("package:" + s));
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(i);
                        break;
                    case 3:
                        if (CommandUtils.hasRootAccess)
                            try {
                                CommandUtils.executeWithError("su", "pm enable " + s, "exit");
                            } catch (Exception t) {
                                E.print(view.getContext(), t);
                            }
                        else RootRequiredDialog.show(view.getContext());
                        break;
                    case 4:
                        if (CommandUtils.hasRootAccess)
                            try {
                                CommandUtils.executeWithError("su", "pm uninstall " + s, "exit");
                            } catch (Exception t) {
                                E.print(view.getContext(), t);
                            }
                        else RootRequiredDialog.show(view.getContext());
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            E.print(view.getContext(), e);
        }
        menu.show();
        return true;
    }

    @Override
    public final void onTextChanged(final CharSequence s, int start, int before, int count) {
        final String v = Sfx.nonnull(searchBar.getText()).toString().toLowerCase();
        info.clear();
        if (v.length() > 0) {
            for (int i = 0; i < info1.size(); i++) {
                final String a = info1.get(i).label.toLowerCase();
                final String b = info1.get(i).packageName.toLowerCase();
                if (a.contains(v) || b.contains(v))
                    info.add(info1.get(i));
            }
        } else info.addAll(info1);
        adaptor.setData(info);
    }

    @Override
    public final void afterTextChanged(final Editable s) {
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    final void extractApk(Integer[] pos) {
        dialog = ProgressDialog.show(this, getResources().getString(R.string.loading), getResources().getString(R.string.please_wait));
        dialog.show();
        handler.post(() -> {
            for (int i : pos) {
                final Filex j = new Filex(storagePath + "/ExtractedApk", adaptor.getItem(i).label + "_" + adaptor.getItem(i).version + ".apk");
                try {
                    final Filex f = new Filex(adaptor.getItem(i).sourceDir);
                    j.createParentDir();
                    if (f.exists()) {
                        final InputStream a = f.getInputStream();
                        final OutputStream b = j.getOutputStream();
                        final byte[] c = new byte[IO.bufferSize];
                        int d;
                        while ((d = a.read(c)) > 0) {
                            b.write(c, 0, d);
                        }
                        a.close();
                        b.close();
                    }
                    Toast.makeText(this, getResources().getString(R.string.apk_extracted_message).replace("$path", j.getPath()), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    E.print(this, e);
                }
            }
            handler.sendEmptyMessage(0);
        });
    }

    private static final class ApplicationInfo implements Comparable<ApplicationInfo> {
        public static byte sortingRule = 0; // 0: package name; 1: label; 2: install time; 3: update time

        public final String packageName;
        public final String label;
        public final String version;
        public final Integer versionCode;
        public final Drawable icon;
        public final Long installTime;
        public final Long updateTime;
        public final String sourceDir;
        public final Boolean enabled;

        public ApplicationInfo(PackageManager pm, @NonNull String packageName) throws Exception {
            this.packageName = packageName;
            this.label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
            this.icon = pm.getApplicationIcon(packageName);
            this.version = pm.getPackageInfo(packageName, 0).versionName;
            this.versionCode = pm.getPackageInfo(packageName, 0).versionCode;
            this.installTime = pm.getPackageInfo(packageName, 0).firstInstallTime;
            this.updateTime = pm.getPackageInfo(packageName, 0).lastUpdateTime;
            this.sourceDir = pm.getApplicationInfo(packageName, 0).sourceDir;
            this.enabled = pm.getApplicationInfo(packageName, 0).enabled;
        }

        @Override
        public final int compareTo(ApplicationInfo o) {
            switch (sortingRule) {
                case 0:
                    return packageName.compareTo(o.packageName);
                case 1:
                    return label.compareTo(o.label);
                case 2:
                    return installTime.compareTo(o.installTime);
                case 3:
                    return updateTime.compareTo(o.updateTime);
                default:
                    return 0;
            }
        }

        public static ArrayList<ApplicationInfo> getInstalledApplicationList(Context ctx) throws Exception {
            final PackageManager a = ctx.getPackageManager();
            final List<android.content.pm.ApplicationInfo> b = a.getInstalledApplications(PackageManager.GET_META_DATA);
            final List<String> c = new ArrayList<>();
            final ArrayList<ApplicationInfo> d = new ArrayList<>();
            for (int i = 0; i < b.size(); i++) c.add(b.get(i).packageName);
            Collections.sort(c);
            for (String s : c) d.add(new ApplicationInfo(a, s));
            return d;
        }
    }

    private static final class CustomAdaptor extends BaseAdapter {
        private final List<ApplicationInfo> data;
        private boolean[] selection;
        private int selectedItemCount = 0;

        public CustomAdaptor(List<ApplicationInfo> d) {
            this.data = d;
            this.selection = new boolean[data.size()];
        }

        public final void setData(List<ApplicationInfo> d) {
            this.data.clear();
            this.data.addAll(d);
            this.selection = new boolean[data.size()];
            notifyDataSetChanged();
        }

        public final void setSelection(int pos, boolean v) {
            if (selection[pos] != v) {
                selection[pos] = v;
                if (v) selectedItemCount++;
                else selectedItemCount--;
            }
            notifyDataSetChanged();
        }

        public final void removeSelections() {
            Arrays.fill(selection, false);
            selectedItemCount = 0;
            notifyDataSetChanged();
        }

        public final boolean isSelected(int pos) {
            return selection[pos];
        }

        @Override
        public final int getCount() {
            return data.size();
        }

        @Override
        public final ApplicationInfo getItem(int position) {
            return data.get(position);
        }

        @Override
        public final long getItemId(int position) {
            return position;
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_list_items, parent, false);
            ViewHolder h = (ViewHolder) convertView.getTag();
            if (h == null) {
                h = new ViewHolder(
                        convertView.findViewById(R.id.appIcon),
                        convertView.findViewById(R.id.appName),
                        convertView.findViewById(R.id.appVersion),
                        convertView.findViewById(R.id.appPackageName));
                convertView.setTag(h);
            }
            convertView.setBackground(ContextCompat.getDrawable(convertView.getContext(), R.drawable.list_item));
            h.appIcon.setImageDrawable(data.get(position).icon);
            h.appName.setText(data.get(position).label);
            h.appVersion.setText(convertView.getResources().getString(R.string.application_version_description)
                    .replace("$version_name", data.get(position).version)
                    .replace("$version_code", data.get(position).versionCode.toString()));
            h.appPackageName.setText(convertView.getResources().getString(R.string.application_package_name_description)
                    .replace("$package_name", data.get(position).packageName));
            convertView.setSelected(selection[position]);
            return convertView;
        }

        private static final class ViewHolder {
            public final AppCompatImageView appIcon;
            public final AppCompatTextView appName;
            public final AppCompatTextView appVersion;
            public final AppCompatTextView appPackageName;

            public ViewHolder(@NonNull AppCompatImageView a, @NonNull AppCompatTextView b, @NonNull AppCompatTextView c, @NonNull AppCompatTextView d) {
                this.appIcon = a;
                this.appName = b;
                this.appVersion = c;
                this.appPackageName = d;
            }

            @NonNull
            @Override
            public final String toString() {
                return "ViewHolder{" +
                        "appIcon=" + appIcon +
                        ", appName=" + appName +
                        ", appVersion=" + appVersion +
                        ", appPackageName=" + appPackageName +
                        '}';
            }
        }

        @NonNull
        @Override
        public final String toString() {
            return "CustomAdaptor{" +
                    "data=" + data +
                    ", selection=" + Arrays.toString(selection) +
                    '}';
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "ApkExtractor{" +
                "listView=" + listView +
                ", searchBar=" + searchBar +
                ", adaptor=" + adaptor +
                ", dialog=" + dialog +
                ", info=" + info +
                ", info1=" + info1 +
                ", handler=" + handler +
                ", menu=" + menu +
                ", menu1=" + Arrays.toString(menu1) +
                ", menu2=" + Arrays.toString(menu2) +
                '}';
    }
}