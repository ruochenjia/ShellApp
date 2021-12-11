package shell.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mindrex.android.appsupport.Color;
import com.mindrex.android.appsupport.CommandUtils;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.SelectDialog;
import com.mindrex.android.appsupport.SelectDialogItem;
import com.mindrex.data.Array;
import com.mindrex.gm.GmSystem;
import com.mindrex.io.File;
import com.mindrex.shell.R;

import java.util.ArrayList;
import java.util.List;

// Notice: This file might have been changed from original.

public final class ApkExtractor extends ActivityFramework implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    private ListView listView;
    private AppCompatEditText searchBar;
    private CustomAdaptor adaptor;
    private ProgressDialog dialog;
    private SwipeRefreshLayout refresh;
    private View bottomBar;
    private final List<PackageInfo> info = new ArrayList<>();
    private final List<PackageInfo> info1 = new ArrayList<>();

    public ApkExtractor() {
        super(R.layout.apk_extractor);
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new Thread(this).start();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public final void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public final void afterTextChanged(Editable s) {
                CharSequence a = searchBar.getText();
                info.clear();
                if (a != null && a.length() > 0) {
                    for (PackageInfo d : info1) {
                        final String label = getPackageManager().getApplicationLabel(d.applicationInfo).toString();
                        if (a.toString().toLowerCase(GmSystem.defaultLocale).equals(label.toLowerCase(GmSystem.defaultLocale)))
                            info.add(d);
                    }
                } else info.addAll(info1);
                adaptor.setData(info);
            }
        });
        refresh.setOnRefreshListener(this);
    }

    @Override
    public final boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case 1:
                adaptor = new CustomAdaptor();
                adaptor.setData(info);
                listView.setAdapter(adaptor);
                refresh.setRefreshing(false);
                return true;
            case 0:
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
        new Thread(this).start();
        refresh.setRefreshing(false);
    }

    @Override
    public final void run() {
        Looper.prepare();
        try {
            info.clear();
            info.addAll(getPackageManager().getInstalledPackages(0));
        } catch (Exception e) {
            E.print(this, e);
        }
        info1.clear();
        info1.addAll(info);
        handler.sendEmptyMessage(1);
        Looper.loop();
    }

    @Override
    public final void onResume() {
        super.onResume();
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

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int a = item.getItemId();
        if (a == R.id.searchApp) {
            if (searchBar.getVisibility() == View.GONE)
                searchBar.setVisibility(View.VISIBLE);
            return true;
        } else if (a == android.R.id.home) {
            onBackPressed();
            return true;
        } else return super.onOptionsItemSelected(item);
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
        if (adaptor.selectedItemCount > 0)
            bottomBar.setVisibility(View.VISIBLE);
        else bottomBar.setVisibility(View.GONE);
    }

    private void openAppSettings(String pkg) {
        try {
            final Intent i = new Intent();
            i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse("package:" + pkg));
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    @SuppressWarnings("unused")
    private void openAppInfo(String pkg) {
    }

    @Override
    public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final ApplicationInfo a = adaptor.getItem(position).applicationInfo;
        final String s = a.packageName;
        try {
            final SelectDialogItem[] b = a.enabled ? new SelectDialogItem[]{
                    new SelectDialogItem(Res.string(R.string.extract_apk), null, Res.drawable(R.drawable.package_extractor, getTheme()), d -> extractApk(position)),
                    new SelectDialogItem(Res.string(R.string.application_information), null, Res.drawable(R.drawable.about, getTheme()), (d) -> openAppInfo(s)),
                    new SelectDialogItem(Res.string(R.string.manage_application), null, Res.drawable(R.drawable.developer_tools), (d) -> openAppSettings(s)),
                    new SelectDialogItem(Res.string(R.string.disable_package), null, Res.drawable(R.drawable.disable, getTheme()), (d) -> {
                        if (CommandUtils.hasRootAccess()) {
                            if (!pm.disable(s))
                                Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        } else RootRequiredDialog.show(this);
                    }),
                    new SelectDialogItem(Res.string(R.string.uninstall_package), null, Res.drawable(R.drawable.delete, getTheme()), (d) -> {
                        if (CommandUtils.hasRootAccess()) {
                            if (!pm.uninstall(s))
                                Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        } else RootRequiredDialog.show(this);
                    })
            } : new SelectDialogItem[]{
                    new SelectDialogItem(Res.string(R.string.extract_apk), null, Res.drawable(R.drawable.package_extractor, getTheme()), (d) -> extractApk(position)),
                    new SelectDialogItem(Res.string(R.string.application_information), null, Res.drawable(R.drawable.about, getTheme()), (d) -> openAppInfo(s)),
                    new SelectDialogItem(Res.string(R.string.manage_application), null, Res.drawable(R.drawable.developer_tools, getTheme()), (d) -> openAppSettings(s)),
                    new SelectDialogItem(Res.string(R.string.enable_package), null, Res.drawable(R.drawable.enable, getTheme()), (d) -> {
                        if (CommandUtils.hasRootAccess()) {
                            if (!pm.enable(s))
                                Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        } else RootRequiredDialog.show(this);
                    }),
                    new SelectDialogItem(Res.string(R.string.uninstall_package), null, Res.drawable(R.drawable.delete, getTheme()), (d) -> {
                        if (CommandUtils.hasRootAccess()) {
                            if (!pm.uninstall(s))
                                Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        } else RootRequiredDialog.show(this);
                    })
            };
            SelectDialog.show(this, getPackageManager().getApplicationLabel(a), b, SelectDialog.cancelButton);
        } catch (Exception e) {
            E.print(this, e);
        }
        return true;
    }


    private void extractApk(int... pos) {
        dialog = ProgressDialog.show(this, Res.string(R.string.loading), Res.string(R.string.please_wait));
        dialog.show();
        new Thread(() -> {
            Looper.prepare();
            for (int i : pos) {
                final PackageInfo pi = adaptor.getItem(i);
                final ApplicationInfo ai = pi.applicationInfo;
                final CharSequence label = getPackageManager().getApplicationLabel(ai);
                final String version = pi.versionName;
                final File dir = new File(storagePath, "ExtractedApk");
                if (!dir.exists())
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdir();
                final File f = new File(dir, label + "_" + version + ".apk");
                if (new File(ai.sourceDir).copyTo(f))
                    Toast.makeText(this, Res.string(R.string.apk_extracted_message).replace("$path", f.getPath()), Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, R.string.copy_failed, Toast.LENGTH_SHORT).show();
            }
            handler.sendEmptyMessage(0);
            Looper.loop();
        }).start();
    }

    private static final class CustomAdaptor extends BaseAdapter {
        private final Drawable defaultDrawable;
        private final Drawable selectedDrawable;
        private final List<PackageInfo> data = new ArrayList<>();
        private boolean[] selection;
        private int selectedItemCount = 0;

        private CustomAdaptor() {
            final StateListDrawable a = new StateListDrawable();
            a.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_focused}, new ColorDrawable(Color.GRAY.toInt()));
            a.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT.toInt()));
            this.defaultDrawable = a;
            this.selectedDrawable = new ColorDrawable(Color.GRAY.toInt());
        }

        public final void setData(List<PackageInfo> pi) {
            data.clear();
            data.addAll(pi);
            selection = new boolean[data.size()];
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
            return data.size();
        }

        @Override
        public final PackageInfo getItem(int position) {
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
            ViewHolder vh = (ViewHolder) convertView.getTag();
            if (vh == null) {
                vh = new ViewHolder(convertView);
                final PackageInfo pi = data.get(position);
                final ApplicationInfo ai = pi.applicationInfo;
                final PackageManager pm = parent.getContext().getPackageManager();
                vh.appIcon.setImageDrawable(pm.getApplicationIcon(ai));
                vh.appName.setText(pm.getApplicationLabel(ai));
                vh.appVersion.setText(pi.versionName);
                vh.appPackageName.setText(pi.packageName);
                convertView.setTag(vh);
            }
            final boolean b = selection[position];
            convertView.setBackground(b ? selectedDrawable : defaultDrawable);
            return convertView;
        }

        private static final class ViewHolder {
            private final AppCompatImageView appIcon;
            private final AppCompatTextView appName;
            private final AppCompatTextView appVersion;
            private final AppCompatTextView appPackageName;

            private ViewHolder(View v) {
                this.appIcon = v.findViewById(R.id.appIcon);
                this.appName = v.findViewById(R.id.appName);
                this.appVersion = v.findViewById(R.id.appVersion);
                this.appPackageName = v.findViewById(R.id.appPackageName);
            }
        }
    }
}