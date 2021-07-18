package shell.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mindrex.android.appsupport.Color;
import com.mindrex.android.appsupport.CommandUtils;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Filex;
import com.mindrex.android.appsupport.SelectDialog;
import com.mindrex.android.appsupport.SelectDialogItem;
import com.mindrex.data.Array;
import com.mindrex.gm.GmSystem;
import com.mindrex.shell.R;
import com.mindrex.util.Sfx;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.List;

// Notice: This file might have been changed from original.

public final class ApkExtractor extends ActivityFramework implements TextWatcher, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    private ListView listView;
    private AppCompatEditText searchBar;
    private CustomAdaptor adaptor;
    private ProgressDialog dialog;
    private SwipeRefreshLayout refresh;
    private View bottomBar;
    private ApplicationInfo[] info, info1;

    public ApkExtractor() {
    }

    @Override
    public final void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        ApplicationInfo.pm = newBase.getPackageManager();
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apk_extractor);
        listView = findViewById(R.id.list);
        searchBar = findViewById(R.id.searchBar);
        bottomBar = findViewById(R.id.bottomBar);
        refresh = findViewById(R.id.refreshLayout);
        refresh.setRefreshing(true);
        findViewById(R.id.cancelButton).setOnClickListener(view -> {
            adaptor.removeSelections();
            bottomBar.setVisibility(View.GONE);
        });
        findViewById(R.id.extractButton).setOnClickListener(view -> {
            int[] a = new int[0];
            for (int i = 0; i < adaptor.selection.length; i++)
                if (adaptor.isSelected(i))
                    a = Array.append(a, i);
            extractApk(a);
            adaptor.removeSelections();
            bottomBar.setVisibility(View.GONE);
        });
        handler.post(this);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        searchBar.addTextChangedListener(this);
        refresh.setOnRefreshListener(this);
    }

    @Override
    public final boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case 1:
                adaptor = new CustomAdaptor();
                adaptor.data = info;
                adaptor.selection = new boolean[info.length];
                listView.setAdapter(adaptor);
            case 0:
                refresh.setRefreshing(false);
                if (dialog != null)
                    dialog.dismiss();
                return true;
            case 2:

            default:
                return false;
        }
    }

    @Override
    public final void onRefresh() {
        updatePackageInfo();
        refresh.setRefreshing(false);
    }

    @Override
    public final void run() {
        try {
            info = ApplicationInfo.getInstalledApplicationList();
        } catch (Exception e) {
            E.print(this, e);
            info = new ApplicationInfo[0];
        }
        info1 = info;
        handler.sendEmptyMessage(1);
    }

    @Override
    public final void onResume() {
        super.onResume();
//        if (ApplicationInfo.pm == null)
//            ApplicationInfo.pm = getPackageManager();
        getContentView().setVisibility(View.VISIBLE);
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
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adaptor.setSelection(position, !adaptor.isSelected(position));
        if (adaptor.selectedItemCount > 0) bottomBar.setVisibility(View.VISIBLE);
        else bottomBar.setVisibility(View.GONE);
    }

    private boolean exec(String s) throws Exception {
        CharSequence a = new StringBuilder();
        for (String b : Shell.su(s).exec().getErr())
            ((StringBuilder) a).append(b);
        a = a.toString();
        if (a.length() > 0)
            throw new Exception(((String) a));
        else return true;
    }

    private void updatePackageInfo(int... pos) {
        if (pos.length > 0) {
            try {
                for (int a : pos)
                    info1[a] = new ApplicationInfo(info1[a].packageName);
            } catch (Exception e) {
                E.print(this, e);
            }
            info = info1;
        } else run();
    }

    @Override
    public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final ApplicationInfo a = adaptor.getItem(position);
        final String s = a.packageName;
        try {
            final SelectDialogItem[] b = a.enabled ? new SelectDialogItem[]{
                    new SelectDialogItem(getResources().getString(R.string.extract_apk), null, ContextCompat.getDrawable(this, R.drawable.package_extractor), d -> extractApk(position)),
                    new SelectDialogItem(getResources().getString(R.string.application_information), null, ContextCompat.getDrawable(this, R.drawable.about), (d) -> {
                        // unf
                    }),
                    new SelectDialogItem(getResources().getString(R.string.manage_application), null, ContextCompat.getDrawable(this, R.drawable.developer_tools), (d) -> {
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
                            E.print(this, e);
                        }
                    }),
                    new SelectDialogItem(getResources().getString(R.string.disable_package), null, ContextCompat.getDrawable(this, R.drawable.disable), (d) -> {
                        if (CommandUtils.hasRootAccess) {
                            try {
                                assert exec("pm disable " + s);
                            } catch (Exception t) {
                                E.print(this, t);
                            }
                            updatePackageInfo(position);
                        } else RootRequiredDialog.show(this);
                    }),
                    new SelectDialogItem(getResources().getString(R.string.uninstall_package), null, ContextCompat.getDrawable(this, R.drawable.delete), (d) -> {
                        if (CommandUtils.hasRootAccess) {
                            try {
                                assert exec("pm uninstall " + s);
                            } catch (Exception t) {
                                E.print(this, t);
                            }
                            updatePackageInfo(position);
                        } else RootRequiredDialog.show(this);
                    })
            } : new SelectDialogItem[]{
                    new SelectDialogItem(getResources().getString(R.string.extract_apk), null, ContextCompat.getDrawable(this, R.drawable.package_extractor), (d) -> extractApk(position)),
                    new SelectDialogItem(getResources().getString(R.string.application_information), null, ContextCompat.getDrawable(this, R.drawable.about), (d) -> {
                        // unf
                    }),
                    new SelectDialogItem(getResources().getString(R.string.manage_application), null, ContextCompat.getDrawable(this, R.drawable.developer_tools), (d) -> {
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
                            E.print(this, e);
                        }
                    }),
                    new SelectDialogItem(getResources().getString(R.string.enable_package), null, ContextCompat.getDrawable(this, R.drawable.enable), (d) -> {
                        if (CommandUtils.hasRootAccess) {
                            try {
                                assert exec("pm disable " + s);
                            } catch (Exception t) {
                                E.print(this, t);
                            }
                            updatePackageInfo(position);
                        } else RootRequiredDialog.show(this);
                    }),
                    new SelectDialogItem(getResources().getString(R.string.uninstall_package), null, ContextCompat.getDrawable(this, R.drawable.delete), (d) -> {
                        if (CommandUtils.hasRootAccess) {
                            try {
                                assert exec("pm uninstall " + s);
                            } catch (Exception t) {
                                E.print(this, t);
                            }
                            updatePackageInfo(position);
                        } else RootRequiredDialog.show(this);
                    })
            };
            SelectDialog.show(this, a.label, b, SelectDialog.cancelButton);
        } catch (Exception e) {
            E.print(this, e);
        }
        return true;
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public final void afterTextChanged(Editable s) {
        CharSequence a = searchBar.getText();
        if (a != null && a.length() > 0) {
            a = String.valueOf(a).toLowerCase(GmSystem.defaultLocale);
            info = new ApplicationInfo[0];
            for (ApplicationInfo d : info1) {
                final String b = d.label.toLowerCase(GmSystem.defaultLocale);
                final String c = d.packageName.toLowerCase(GmSystem.defaultLocale);
                if (b.contains(a) || c.contains(a))
                    info = Array.append(info, d);
            }
        } else info = info1;
        adaptor.setData(info);
    }

    private void extractApk(int... pos) {
        dialog = ProgressDialog.show(this, getResources().getString(R.string.loading), getResources().getString(R.string.please_wait));
        dialog.show();
        handler.post(() -> {
            for (int i : pos) {
                final Filex j = new Filex(storagePath + "/ExtractedApk", adaptor.getItem(i).label + "_" + adaptor.getItem(i).version + ".apk");
                try {
                    final Filex f = new Filex(adaptor.getItem(i).sourceDir);
                    j.createParentDir();
                    f.copyTo(j);
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
        public static PackageManager pm;

        public final String packageName;
        public final String label;
        public final String version;
        public final Integer versionCode;
        public final Drawable icon;
        public final Long installTime;
        public final Long updateTime;
        public final String sourceDir;
        public final Boolean enabled;

        public ApplicationInfo(@NonNull String packageName) throws Exception {
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
            return compare(this, o);
        }

        public static int compare(ApplicationInfo o1, ApplicationInfo o2) {
            switch (sortingRule) {
                case 0:
                    return o1.packageName.compareTo(o2.packageName);
                case 1:
                    return o1.label.compareTo(o2.label);
                case 2:
                    return o1.installTime.compareTo(o2.installTime);
                case 3:
                    return o1.updateTime.compareTo(o2.updateTime);
                default:
                    return 0;
            }
        }

        public static ApplicationInfo[] getInstalledApplicationList() throws Exception {
            final List<android.content.pm.ApplicationInfo> b = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            final ApplicationInfo[] c = new ApplicationInfo[b.size()];
            for (int i = 0; i < c.length; i++)
                c[i] = new ApplicationInfo(b.get(i).packageName);
            Arrays.sort(c);
            return c;
        }
    }

    private static final class CustomAdaptor extends BaseAdapter {
        private final Drawable defaultDrawable;
        private final Drawable selectedDrawable;
        private ApplicationInfo[] data;
        private boolean[] selection;
        private int selectedItemCount = 0;

        private CustomAdaptor() {
            final StateListDrawable a = new StateListDrawable();
            a.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_focused}, new ColorDrawable(Color.GRAY.toInt()));
            a.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT.toInt()));
            this.defaultDrawable = a;
            this.selectedDrawable = new ColorDrawable(Color.GRAY.toInt());
        }

        public final void setData(ApplicationInfo[] a) {
            data = a;
            selection = new boolean[data.length];
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
            Array.fill(selection, false);
            selectedItemCount = 0;
            notifyDataSetChanged();
        }

        public final boolean isSelected(int pos) {
            return selection[pos];
        }

        @Override
        public final int getCount() {
            return data.length;
        }

        @Override
        public final ApplicationInfo getItem(int position) {
            return data[position];
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
            h.appIcon.setImageDrawable(data[position].icon);
            h.appName.setText(data[position].label);
            h.appVersion.setText(convertView.getResources().getString(R.string.application_version_description)
                    .replace("$version_name", data[position].version)
                    .replace("$version_code", data[position].versionCode.toString()));
            h.appPackageName.setText(convertView.getResources().getString(R.string.application_package_name_description)
                    .replace("$package_name", data[position].packageName));
            convertView.setSelected(selection[position]);
            convertView.setBackground(selection[position] ? selectedDrawable : defaultDrawable);
            return convertView;
        }

        private static final class ViewHolder {
            public final AppCompatImageView appIcon;
            public final AppCompatTextView appName;
            public final AppCompatTextView appVersion;
            public final AppCompatTextView appPackageName;

            public ViewHolder(AppCompatImageView a, AppCompatTextView b, AppCompatTextView c, AppCompatTextView d) {
                this.appIcon = a;
                this.appName = b;
                this.appVersion = c;
                this.appPackageName = d;
            }
        }
    }
}