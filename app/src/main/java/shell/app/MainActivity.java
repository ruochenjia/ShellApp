package shell.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.mindrex.android.SignJar;
import com.mindrex.android.appsupport.AlertDialog;
import com.mindrex.android.appsupport.ArchiveType;
import com.mindrex.android.appsupport.Color;
import com.mindrex.android.appsupport.CommandUtils;
import com.mindrex.android.appsupport.DialogButton;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Filex;
import com.mindrex.android.appsupport.LinearLayout;
import com.mindrex.android.appsupport.SelectDialog;
import com.mindrex.android.appsupport.SelectDialogItem;
import com.mindrex.android.appsupport.Thumbnail;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.android.appsupport.htmlviewer.HtmlViewer;
import com.mindrex.android.debugger.Log;
import com.mindrex.annotation.TestOnly;
import com.mindrex.data.Array;
import com.mindrex.gm.GmSystem;
import com.mindrex.io.ArchiveEntry;
import com.mindrex.io.ArchiveInput;
import com.mindrex.io.CompressLevel;
import com.mindrex.io.CompressMethod;
import com.mindrex.io.CompressUtils;
import com.mindrex.io.File;
import com.mindrex.io.FileSystem;
import com.mindrex.io.IOUtils;
import com.mindrex.io.KeyUtils;
import com.mindrex.io.SevenZInput;
import com.mindrex.io.SevenZOutput;
import com.mindrex.io.TarInput;
import com.mindrex.io.TarOutput;
import com.mindrex.io.TextWriter;
import com.mindrex.io.ZipEntry;
import com.mindrex.io.ZipInput;
import com.mindrex.io.ZipOutput;
import com.mindrex.shell.R;
import com.mindrex.util.FilePermission;
import com.mindrex.util.Sfx;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dalvik.system.DexFile;

// Notice: This file might have been changed from original.

public final class MainActivity extends ActivityFramework implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, TextWatcher, Thread.UncaughtExceptionHandler, Handler.Callback {
    private ListView listView;
    private FileAdaptor adaptor;
    private View contentView, mainNavigationBar, selectionNavigationBar, copyFileNavigationBar;
    private AppCompatImageView backButton, homeButton, createButton, refreshButton, upDirButton, cancelButton, selectAllButton, copyFileButton, deleteFileButton, moreItemButton;
    private AppCompatButton cancelCopyButton, pasteButton;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ProgressDialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final StringHolder currentFile = new StringHolder();
    private boolean[] selection;
    private Filex[] fileList;
    private List<String> origins;
    private int selectedCount = 0;

    private View storagePartitionInternal, storagePartitionExternal, storagePartitionRoot, storagePartitionData, storagePartitionCache, storagePartitionSystem;
    private ProgressBar storagePartitionInternalProgressBar, storagePartitionExternalProgressBar, storagePartitionRootProgressBar, storagePartitionDataProgressBar, storagePartitionCacheProgressBar, storagePartitionSystemProgressBar;
    private AppCompatTextView storagePartitionInternalInfo, storagePartitionExternalInfo, storagePartitionRootInfo, storagePartitionDataInfo, storagePartitionCacheInfo, storagePartitionSystemInfo;

    private boolean fileManagerInitialized = false;
    private boolean doubleBackToExitPressedOnce = false;

    private final Object pauseLock = new Object();
    private boolean isPaused = false;
    private boolean stopping = false;
    private boolean movingFile = false;
    private boolean extractingFile = false;
    private boolean reverseOrder = false;

    private final Runnable setupContentData = new Runnable() {
        @Override
        public final void run() {
            final Filex v = new Filex(currentFile.toString());
            if (new File(v.getPath()).canRead() || CommandUtils.hasRootAccess) {
                fileList = v.list();
            } else {
                RootRequiredDialog.show(MainActivity.this);
                fileList = new Filex[0];
            }
            if (reverseOrder) Arrays.sort(fileList, Collections.reverseOrder());
            else Arrays.sort(fileList);
            final FileData[] a = new FileData[fileList.length];
            int folderCount = 0;
            int fileCount = 0;
            for (int i = 0; i < fileList.length; i++) {
                final FileData b = new FileData();
                final Filex f = fileList[i];
                b.name = f.getName();
                if (f.isDirectory()) {
                    b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.folder);
                    b.description = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", GmSystem.defaultLocale).format(f.lastModified());
                    folderCount++;
                } else {
                    switch (FileExtension.CheckFileExtension(f)) {
                        case IMAGE:
                            try {
                                b.icon = new BitmapDrawable(getResources(), Thumbnail.createImageThumbnail(f, (byte) 100));
                            } catch (Exception e) {
                                b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.image_file);
                            }
                            break;
                        case MUSIC:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.music_file);
                            break;
                        case TEXT:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.text_document);
                            break;
                        case CODE:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.code_file);
                            break;
                        case ZIP_ARCHIVE:
                        case ARCHIVE:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.archive_file);
                            break;
                        case VIDEO:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.video_file);
                            break;
                        case APK:
                            try {
                                final PackageManager c = getPackageManager();
                                final ApplicationInfo d = c.getPackageArchiveInfo(f.getPath(), 0).applicationInfo;
                                b.icon = c.getApplicationIcon(d);
                            } catch (Exception e) {
                                b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.apk_file);
                            }
                            break;
                        case XAPK:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.apk_bundle);
                            break;
                        case HTML:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.html_file);
                            break;
                        case DEFAULT:
                        case UNKNOWN:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.file);
                            break;
                        default:
                            b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.file_unknown);
                            break;
                    }
                    b.description = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", GmSystem.defaultLocale).format(f.lastModified()) + " " + new FileSizeTransform(f.size()).toString();
                    fileCount++;
                }
                a[i] = b;
            }
            selection = new boolean[fileList.length];
            adaptor.setData(a);
            final MemoryStatus b = new MemoryStatus(currentFile.toString());
            actionBar.setSubtitle(getResources().getString(R.string.current_directory_info)
                    .replace("$file_count", String.valueOf(fileCount))
                    .replace("$folder_count", String.valueOf(folderCount))
                    .replace("$disk_info", b.getFinalAvailableMemorySize() + "/" + b.getFinalTotalMemorySize())
            );
            backButton.setEnabled(true);
        }
    };
    private final Runnable copyFile = new Runnable() {
        @Override
        public final void run() {
            if (origins.size() > 0) {
                for (int i = 0; i < origins.size(); i++) {
                    synchronized (pauseLock) {
                        if (stopping) break;
                        else if (isPaused) {
                            try {
                                synchronized (pauseLock) {
                                    pauseLock.wait();
                                }
                            } catch (Exception e) {
                                break;
                            }
                        }
                    }
                    final Filex a = new Filex(origins.get(i));
                    final Filex b = new Filex(currentFile.toString(), a);
                    if (movingFile) {
                        if (!b.exists()) try {
                            a.moveTo(b);
                        } catch (Exception e) {
                            E.print(MainActivity.this, e);
                        }
                        else showFileExistsDialog(a, b);
                    } else {
                        if (!b.exists()) try {
                            a.copyTo(b);
                        } catch (Exception e) {
                            E.print(MainActivity.this, e);
                        }
                        else showFileExistsDialog(a, b);
                    }
                    if (stopping) break;
                }
            }
            handler.sendEmptyMessage(0);
        }
    };

    public MainActivity() {
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);

        final AlertDialog a = new AlertDialog(this);
        a.setTitle("Title");
        a.setMessage("Message");
        a.setView(new HtmlViewer(this, null));
        a.setPositiveButton("PositiveButton", (b, c) -> b.dismiss());
        a.setNegativeButton("NegativeButton", (b, c) -> {
        });
        a.setNeutralButton("NeutralButton", (b, c) -> {
        });
        a.show();

        listView = findViewById(R.id.mainContent);
        contentView = findViewById(R.id.fileManagerContentView);
        mainNavigationBar = findViewById(R.id.mainNavigationBar);
        selectionNavigationBar = findViewById(R.id.selectionNavigationBar);
        copyFileNavigationBar = findViewById(R.id.copyFileNavigationBar);
        backButton = findViewById(R.id.back);
        homeButton = findViewById(R.id.home);
        createButton = findViewById(R.id.create);
        refreshButton = findViewById(R.id.refreshContents);
        upDirButton = findViewById(R.id.parentFolder);
        cancelButton = findViewById(R.id.cancel);
        selectAllButton = findViewById(R.id.selectAll);
        copyFileButton = findViewById(R.id.copyFile);
        deleteFileButton = findViewById(R.id.deleteFile);
        moreItemButton = findViewById(R.id.moreItem);
        cancelCopyButton = findViewById(R.id.cancelButton);
        pasteButton = findViewById(R.id.pasteButton);
        navigationView = findViewById(R.id.navigationView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View v = navigationView.getHeaderView(0);
        storagePartitionInternalProgressBar = v.findViewById(R.id.storagePartitionInternalProgressBar);
        storagePartitionInternalInfo = v.findViewById(R.id.storagePartitionInternalInfo);
        storagePartitionExternal = v.findViewById(R.id.storagePartitionExternal);
        storagePartitionExternalProgressBar = v.findViewById(R.id.storagePartitionExternalProgressBar);
        storagePartitionInternal = v.findViewById(R.id.storagePartitionInternal);
        storagePartitionExternalInfo = v.findViewById(R.id.storagePartitionExternalInfo);
        storagePartitionRoot = v.findViewById(R.id.storagePartitionRoot);
        storagePartitionRootProgressBar = v.findViewById(R.id.storagePartitionRootProgressBar);
        storagePartitionRootInfo = v.findViewById(R.id.storagePartitionRootInfo);
        storagePartitionData = v.findViewById(R.id.storagePartitionData);
        storagePartitionDataProgressBar = v.findViewById(R.id.storagePartitionDatarogressBar);
        storagePartitionDataInfo = v.findViewById(R.id.storagePartitionDataInfo);
        storagePartitionCache = v.findViewById(R.id.storagePartitionCache);
        storagePartitionCacheProgressBar = v.findViewById(R.id.storagePartitionCacheProgressBar);
        storagePartitionCacheInfo = v.findViewById(R.id.storagePartitionCacheInfo);
        storagePartitionSystem = v.findViewById(R.id.storagePartitionSystem);
        storagePartitionSystemProgressBar = v.findViewById(R.id.storagePartitionSystemProgressBar);
        storagePartitionSystemInfo = v.findViewById(R.id.storagePartitionSystemInfo);

        drawerLayout = (DrawerLayout) contentView;
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            @Override
            public final void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                refreshStoragePartitionContents();
            }

            @Override
            public final void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        actionBar = getSupportActionBar();

        currentFile.setValue(storagePath);
    }

    @Override
    public final boolean handleMessage(@NonNull Message message) {
        switch (message.what) {
            case 0:
                copyFileNavigationBar.setVisibility(View.GONE);
                mainNavigationBar.setVisibility(View.VISIBLE);
                if (origins.size() > 0) origins.clear();
            case 1:
                dialog.dismiss();
                handler.postDelayed(this::refreshContent, 200);
                return true;
            default:
                return false;
        }
    }

    @Override
    public final void run() {
        if (!CommandUtils.hasRootAccess)
            Toast.makeText(this, R.string.no_root_layout, Toast.LENGTH_SHORT).show();
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            Toast.makeText(this, R.string.unsupported_system_version_warning, Toast.LENGTH_LONG).show();
        initializeStoragePartitionContents();
        if (!new File(getExternalFilesDir(null), ".registered_classes").exists())
            new Thread() {
                @Override
                public final void run() {
                    try {
                        verifyDexFile();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

                @Override
                public final synchronized void start() {
                    setPriority(3);
                    setDaemon(true);
                    setUncaughtExceptionHandler(MainActivity.this);
                    super.start();
                }
            }.start();
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (!fileManagerInitialized) initializeFileManager();
        else refreshContent();
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public final void onPause() {
        contentView.setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onDestroy() {
        removeTempFiles();
        super.onDestroy();
    }

    @Override
    protected final void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public final void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
        refreshContent();
    }

    @Override
    public final void onBackPressed() {
        upDirButton.callOnClick();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int i = item.getItemId();
        if (i == R.id.sortBy) {
            final AlertDialog a = new AlertDialog(this);
            a.setTitle(R.string.sort_by);
            final RadioGroup b = new RadioGroup(this);
            b.setOrientation(RadioGroup.VERTICAL);
            final AppCompatRadioButton c = new AppCompatRadioButton(this);
            c.setText(R.string.name);
            c.setId(R.id.a);
            b.addView(c, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final AppCompatRadioButton d = new AppCompatRadioButton(this);
            d.setText(R.string.modified_date);
            d.setId(R.id.b);
            b.addView(d, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final AppCompatRadioButton e = new AppCompatRadioButton(this);
            e.setText(R.string.size);
            e.setId(R.id.c);
            b.addView(e, 2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final AppCompatCheckBox f = new AppCompatCheckBox(this);
            f.setText(R.string.reverse_order);
            b.addView(f, 3, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (Filex.sortingRule == Filex.SORT_BY_NAME) b.check(R.id.a);
            else if (Filex.sortingRule == Filex.SORT_BY_MODIFIED_TIME) b.check(R.id.b);
            else if (Filex.sortingRule == Filex.SORT_BY_SIZE) b.check(R.id.c);
            else b.check(R.id.a);
            f.setChecked(reverseOrder);
            a.setView(b);
            a.setButtons(new DialogButton(DialogButton.POSITIVE,
                    getResources().getString(R.string.selection_confirm),
                    g -> {
                        final int r = b.getCheckedRadioButtonId();
                        if (r == R.id.b) Filex.sortingRule = Filex.SORT_BY_MODIFIED_TIME;
                        else if (r == R.id.c) Filex.sortingRule = Filex.SORT_BY_SIZE;
                        else Filex.sortingRule = Filex.SORT_BY_NAME;
                        reverseOrder = f.isChecked();
                        refreshContent();
                    }), AlertDialog.cancelButton);
            a.show();
            return true;
        } else if (i == R.id.settings) {
            // getPackageManager().setComponentEnabledSetting(new ComponentName(this, Application.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return true;
        } else if (i == R.id.command) {

            return true;
        } else if (toggle.onOptionsItemSelected(item))
            return true;
        else return super.onOptionsItemSelected(item);
    }

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 /*RESULT_OK*/ && data != null) {
            final Uri uri = data.getData();
            if (uri != null && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (selectedCount == 0)
            try {
                Filex f = new Filex(fileList[position]);
                if (f.isDirectory()) {
                    currentFile.setValue(f.getPath());
                    refreshContent();
                } else {
                    if (f.requireRootAccess())
                        f = new Filex(Utils.saveTempFile(f.getPath()));
                    final Filex xf = f;
                    FileExtension fileExtension = FileExtension.CheckFileExtension(xf);
                    if (fileExtension == FileExtension.IMAGE) {
                        Application.sendIntentData(this, xf.toFile(), "image/*");
                    } else if (fileExtension == FileExtension.MUSIC) {
                        Application.sendIntentData(this, xf.toFile(), "audio/*");
                    } else if (fileExtension == FileExtension.TEXT || fileExtension == FileExtension.CODE) {
                        final Intent i = new Intent(this, TextEditorActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setAction(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(xf.toFile()), "text/plain");
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivity(i);
                    } else if (fileExtension == FileExtension.ZIP_ARCHIVE || fileExtension == FileExtension.ARCHIVE) {
                        final String s = xf.getName();
                        if (xf.getExtension().equals(".7z")) {
                            SelectDialog.show(this, s, new SelectDialogItem[]{
                                    new SelectDialogItem(
                                            getResources().getString(R.string.view),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.view),
                                            a -> {
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_here),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final ArchiveInput c = new SevenZInput(new FileInputStream(xf.getPath()));
                                                        c.extract(currentFile.toString());
                                                        c.close();
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_archive_name),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final Filex d = new Filex(currentFile.toString(), IOUtils.removeFileExtension(xf.getPath()) + "_extracted");
                                                        if (!d.exists()) d.mkdir();
                                                        else if (!d.isDirectory()) {
                                                            if (d.isFile() && d.size() > 0)
                                                                d.moveTo(d.getPath() + ".bak");
                                                            else d.delete();
                                                            d.mkdir();
                                                        }
                                                        final ArchiveInput c = new SevenZInput(new FileInputStream(xf.getPath()));
                                                        c.extract(d.getPath());
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_path),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                origins.add(xf.getPath());
                                                cancelButton.callOnClick();
                                                mainNavigationBar.setVisibility(View.GONE);
                                                copyFileNavigationBar.setVisibility(View.VISIBLE);
                                                movingFile = false;
                                                extractingFile = true;
                                            }
                                    )
                            }, AlertDialog.cancelButton);
                        } else if (xf.getExtension().equals(".tar")) {
                            SelectDialog.show(this, s, new SelectDialogItem[]{
                                    new SelectDialogItem(
                                            getResources().getString(R.string.view),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.view),
                                            a -> {
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_here),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final ArchiveInput c = new TarInput(new FileInputStream(xf.getPath()));
                                                        c.extract(currentFile.toString());
                                                        c.close();
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_archive_name),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final Filex d = new Filex(currentFile.toString(), IOUtils.removeFileExtension(xf.getPath()) + "_extracted");
                                                        if (!d.exists()) d.mkdir();
                                                        else if (!d.isDirectory()) {
                                                            if (d.isFile() && d.size() > 0)
                                                                d.moveTo(d.getPath() + ".bak");
                                                            else d.delete();
                                                            d.mkdir();
                                                        }
                                                        final ArchiveInput c = new TarInput(new FileInputStream(xf.getPath()));
                                                        c.extract(d.getPath());
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_path),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                origins.add(xf.getPath());
                                                cancelButton.callOnClick();
                                                mainNavigationBar.setVisibility(View.GONE);
                                                copyFileNavigationBar.setVisibility(View.VISIBLE);
                                                movingFile = false;
                                                extractingFile = true;
                                            }
                                    )
                            }, AlertDialog.cancelButton);
                        } else {
                            SelectDialog.show(this, s, new SelectDialogItem[]{
                                    new SelectDialogItem(
                                            getResources().getString(R.string.view),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.view),
                                            a -> {
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_here),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final ArchiveInput c = new ZipInput(new FileInputStream(xf.getPath()));
                                                        c.extract(currentFile.toString());
                                                        c.close();
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_archive_name),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                                this.dialog.show();
                                                handler.post(() -> {
                                                    try {
                                                        final Filex d = new Filex(currentFile.toString(), IOUtils.removeFileExtension(xf.getPath()) + "_extracted");
                                                        if (!d.exists()) d.mkdir();
                                                        else if (!d.isDirectory()) {
                                                            if (d.isFile() && d.size() > 0)
                                                                d.moveTo(d.getPath() + ".bak");
                                                            else d.delete();
                                                            d.mkdir();
                                                        }
                                                        final ArchiveInput c = new ZipInput(new FileInputStream(xf.getPath()));
                                                        c.extract(d.getPath());
                                                    } catch (Exception e) {
                                                        E.print(this, e);
                                                    }
                                                    handler.sendEmptyMessage(1);
                                                });
                                            }
                                    ),
                                    new SelectDialogItem(
                                            getResources().getString(R.string.extract_to_path),
                                            null,
                                            ContextCompat.getDrawable(this, R.drawable.package_extractor),
                                            a -> {
                                                origins.add(xf.getPath());
                                                cancelButton.callOnClick();
                                                mainNavigationBar.setVisibility(View.GONE);
                                                copyFileNavigationBar.setVisibility(View.VISIBLE);
                                                movingFile = false;
                                                extractingFile = true;
                                            }
                                    )
                            }, AlertDialog.cancelButton);
                        }
                    } else if (fileExtension == FileExtension.VIDEO) {
                        Application.sendIntentData(this, xf.toFile(), "video/*");
                    } else if (fileExtension == FileExtension.XAPK) {// unf
                    } else if (fileExtension == FileExtension.HTML) {
                        Application.sendIntentData(this, xf.toFile(), "text/html");
                    } else if (fileExtension == FileExtension.PDF) {
                        Application.sendIntentData(this, xf.toFile(), "application/pdf");
                    } else if (fileExtension == FileExtension.APK) {
                        Application.sendIntentData(this, xf.toFile(), "application/vnd.android.package-archive");
                    } else if (fileExtension == FileExtension.COMPRESSED) {
                        final String s = xf.getName();
                        AlertDialog.show(this, s, getResources().getString(R.string.extract_file_confirm_message),
                                new DialogButton(DialogButton.POSITIVE, getResources().getString(R.string.selection_confirm),
                                        a -> {
                                            this.dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                            this.dialog.show();
                                            handler.post(() -> {
                                                try {
                                                    ArchiveType archiveType = ArchiveType.fromFileExtension(s);
                                                    if (archiveType == ArchiveType.BROTLI) {
                                                        CompressUtils.extractBrotli(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.BZIP2) {
                                                        CompressUtils.extractBz2(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.GZIP) {
                                                        CompressUtils.extractGz(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.LZ4) {
                                                        CompressUtils.extractLz4(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.PACK200) {
                                                        CompressUtils.extractPack200(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.SNAPPY) {
                                                        CompressUtils.extractSnappy(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.XZ) {
                                                        CompressUtils.extractXz(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.Z) {
                                                        CompressUtils.extractZ(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else if (archiveType == ArchiveType.ZSTD) {
                                                        CompressUtils.extractZst(new FileInputStream(xf.getPath()), new FileOutputStream(IOUtils.removeFileExtension(xf.getPath()), false));
                                                    } else {
                                                        throw new IllegalArgumentException("Unsupported file type: " + s);
                                                    }
                                                } catch (Exception l) {
                                                    E.print(this, l);
                                                }
                                                handler.sendEmptyMessage(1);
                                            });
                                        }), AlertDialog.cancelButton);
                    } else if (fileExtension == FileExtension.DISK_IMAGE) {// unf
                    } else if (fileExtension == FileExtension.UNKNOWN || fileExtension == FileExtension.DEFAULT) {
                        try {
                            final MimeTypeMap m = MimeTypeMap.getSingleton();
                            final String j = xf.getName().substring(xf.getName().indexOf(".") + 1);
                            Application.sendIntentData(this, xf.toFile(), m.getMimeTypeFromExtension(j));
                        } catch (Exception e) {
                            E.print(this, e);
                        }
                    }
                }
            } catch (Exception e) {
                E.print(this, e);
            }
        else setSelection(position);
    }

    @Override
    public final boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        setSelection(position);
        return true;
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public final void afterTextChanged(Editable s) {
    }

    @Override
    public final void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.c(t, e);
    }

    final void refreshContent() {
        swipeRefreshLayout.setRefreshing(true);
        try {
            adaptor = new FileAdaptor();
            handler.post(setupContentData);
            if (selectedCount >= 0) {
                selectedCount = 0;
                if (selectionNavigationBar.getVisibility() == View.VISIBLE) {
                    selectionNavigationBar.setVisibility(View.GONE);
                    mainNavigationBar.setVisibility(View.VISIBLE);
                }
            }
            listView.setAdapter(adaptor);
            actionBar.setTitle(currentFile.toString());
        } catch (Exception e) {
            E.print(this, e);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private Object tmp;

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @SuppressLint("NonConstantResourceId")
    final void initializeFileManager() {
        origins = new ArrayList<>();
        adaptor = new FileAdaptor();
        handler.post(setupContentData);
        listView.setSelector(ContextCompat.getDrawable(this, R.drawable.list_item));
        listView.setAdapter(adaptor);
        actionBar.setTitle(currentFile.toString());
        backButton.setOnClickListener(view -> goBack());
        homeButton.setOnClickListener(view -> {
            currentFile.setValue(storagePath);
            refreshContent();
        });
        createButton.setOnClickListener(view -> {
            final AlertDialog a = new AlertDialog(this);
            a.setTitle(R.string.create);
            final AppCompatEditText b = new AppCompatEditText(this);
            b.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            b.setHint(R.string.file_name);
            a.setView(b);
            a.setPositiveButton(R.string.folder, (dialog, which) -> {
                if (b.getText() != null) {
                    final String s = b.getText().toString();
                    if (!s.equals("")) {
                        final Filex f = new Filex(currentFile.toString(), s);
                        if (!f.exists()) try {
                            f.mkdir();
                        } catch (Exception e) {
                            E.print(this, e);
                        }
                        else
                            Toast.makeText(this, R.string.file_exists_message, Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::refreshContent, 200);
                    } else
                        Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
            });
            a.setNegativeButton(R.string.file, (dialog, which) -> {
                if (b.getText() != null) {
                    final String s = b.getText().toString();
                    if (!s.equals("")) {
                        final Filex f = new Filex(currentFile.toString(), s);
                        if (!f.exists()) try {
                            f.createNewFile();
                        } catch (Exception e) {
                            E.print(this, e);
                        }
                        else
                            Toast.makeText(this, R.string.file_exists_message, Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::refreshContent, 200);
                    } else
                        Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
            });
            a.setNeutralButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.show();
        });
        upDirButton.setOnClickListener(view -> {
            if (currentFile.equals(FileSystem.separator) || FileSystem.parseParent(currentFile).length() == 0) {
                if (doubleBackToExitPressedOnce)
                    finish();
                else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, R.string.back_key_exit_app_notice, Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            } else
                try {
                    currentFile.setValue(Sfx.nonnull(new Filex(currentFile.toString()).getParentPath()));
                    refreshContent();
                } catch (Exception e) {
                    E.print(this, e);
                }
        });
        refreshButton.setOnClickListener(view -> refreshContent());
        backButton.setOnLongClickListener(Utils::toastContentDescription);
        homeButton.setOnLongClickListener(Utils::toastContentDescription);
        createButton.setOnLongClickListener(Utils::toastContentDescription);
        refreshButton.setOnLongClickListener(Utils::toastContentDescription);
        upDirButton.setOnLongClickListener(Utils::toastContentDescription);
        cancelButton.setOnClickListener(view -> {
            Array.fill(selection, false);
            adaptor.setSelection(selection);
            adaptor.notifyDataSetChanged();
            selectedCount = 0;
            selectionNavigationBar.setVisibility(View.GONE);
            mainNavigationBar.setVisibility(View.VISIBLE);
        });
        selectAllButton.setOnClickListener(view -> {
            Array.fill(selection, true);
            adaptor.setSelection(selection);
            adaptor.notifyDataSetChanged();
            selectedCount = selection.length;
        });
        copyFileButton.setOnClickListener(view -> {
            for (int i = 0; i < selection.length; i++) {
                if (selection[i]) origins.add(fileList[i].getPath());
            }
            cancelButton.callOnClick();
            mainNavigationBar.setVisibility(View.GONE);
            copyFileNavigationBar.setVisibility(View.VISIBLE);
            movingFile = false;
            extractingFile = false;
        });
        deleteFileButton.setOnClickListener(view -> {
            final AlertDialog a = new AlertDialog(this);
            a.setTitle(R.string.delete);
            a.setMessage(R.string.delete_file_confirm_message);
            a.setPositiveButton(R.string.selection_confirm, (_dialog, which) -> {
                dialog = ProgressDialog.show(this, getResources().getString(R.string.delete_file_notice), getResources().getString(R.string.please_wait));
                dialog.show();
                handler.post(() -> {
                    for (int i = 0; i < selection.length; i++) {
                        if (selection[i]) {
                            final Filex f = fileList[i];
                            try {
                                f.delete();
                            } catch (Exception e) {
                                E.print(this, e);
                            }
                        }
                    }
                    handler.sendEmptyMessage(1);
                });
            });
            a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.show();
        });
        moreItemButton.setOnClickListener(view -> {
            if (selectedCount == 1) {
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i]) {
                        tmp = fileList[i];
                        break;
                    }
                }
                final Filex f = (Filex) Sfx.nonnull(tmp);
                if (f.isDirectory()) {
                    SelectDialog.show(this, getResources().getString(R.string.menu), new SelectDialogItem[]{
                            new SelectDialogItem(
                                    getResources().getString(R.string.move),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.move),
                                    a -> {
                                        for (int i = 0; i < selection.length; i++)
                                            if (selection[i]) origins.add(fileList[i].getPath());
                                        cancelButton.callOnClick();
                                        mainNavigationBar.setVisibility(View.GONE);
                                        copyFileNavigationBar.setVisibility(View.VISIBLE);
                                        movingFile = true;
                                    }
                            ),
                            new SelectDialogItem(
                                    getResources().getString(R.string.rename),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.rename),
                                    a -> {
                                        final AlertDialog b = new AlertDialog(this);
                                        b.setTitle(R.string.rename);
                                        final AppCompatEditText c = new AppCompatEditText(this);
                                        c.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                        c.setHint(R.string.file_name);
                                        c.setText(f.getName());
                                        b.setView(c);
                                        b.setButtons(new DialogButton(DialogButton.POSITIVE,
                                                getResources().getString(R.string.selection_confirm),
                                                d -> {
                                                    CharSequence s = c.getText();
                                                    if (s != null) {
                                                        if (s.length() > 0) {
                                                            try {
                                                                f.moveTo(new Filex(currentFile.toString(), s.toString()));
                                                            } catch (Exception e) {
                                                                E.print(this, e);
                                                            }
                                                            handler.postDelayed(this::refreshContent, 200);
                                                        } else
                                                            Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                    } else
                                                        Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                }
                                        ), AlertDialog.cancelButton);
                                        b.show();
                                    }
                            ),
                            new SelectDialogItem(
                                    getResources().getString(R.string.compress),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.compress),
                                    a -> showCompressDialog(f)
                            ),
                            new SelectDialogItem(
                                    getResources().getString(R.string.properties),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.properties),
                                    a -> showPropertyDialog(f)
                            )
                    }, AlertDialog.cancelButton);
                } else {
                    final FileExtension g = FileExtension.CheckFileExtension(f);
                    if (g == FileExtension.ZIP_ARCHIVE || g == FileExtension.APK) {
                        SelectDialog.show(this, getResources().getString(R.string.menu), new SelectDialogItem[]{
                                new SelectDialogItem(
                                        getResources().getString(R.string.open_with),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.open_with),
                                        a -> Application.sendIntentData(this, f.toFile(), "*/*")
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.move),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.move),
                                        a -> {
                                            for (int l = 0; l < selection.length; l++)
                                                if (selection[l])
                                                    origins.add(fileList[l].getPath());
                                            cancelButton.callOnClick();
                                            mainNavigationBar.setVisibility(View.GONE);
                                            copyFileNavigationBar.setVisibility(View.VISIBLE);
                                            movingFile = true;
                                            extractingFile = false;
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.rename),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.rename),
                                        a -> {
                                            final AlertDialog b = new AlertDialog(this);
                                            b.setTitle(R.string.rename);
                                            final AppCompatEditText c = new AppCompatEditText(this);
                                            c.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                            c.setHint(R.string.file_name);
                                            c.setText(f.getName());
                                            b.setView(c);
                                            b.setButtons(new DialogButton(DialogButton.POSITIVE,
                                                    getResources().getString(R.string.selection_confirm),
                                                    d -> {
                                                        CharSequence s = c.getText();
                                                        if (s != null) {
                                                            if (s.length() > 0) {
                                                                try {
                                                                    f.moveTo(new Filex(currentFile.toString(), s.toString()));
                                                                } catch (Exception e) {
                                                                    E.print(this, e);
                                                                }
                                                                handler.postDelayed(this::refreshContent, 200);
                                                            } else
                                                                Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                        } else
                                                            Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                    }
                                            ), AlertDialog.cancelButton);
                                            b.show();
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.compress),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.compress),
                                        a -> showCompressDialog(f)
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.share),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.share),
                                        a -> {
                                            final Intent i = new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            i.setDataAndType(FileProvider.getUriForFile(this, getPackageName() + ".provider", f.toFile()), "*/*");
                                            startActivity(Intent.createChooser(i, f.getName()));
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.properties),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.properties),
                                        a -> showPropertyDialog(f)
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.sign),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.sign),
                                        a -> showSignatureDialog(f)
                                )
                        }, AlertDialog.cancelButton);
                    } else {
                        SelectDialog.show(this, getResources().getString(R.string.menu), new SelectDialogItem[]{
                                new SelectDialogItem(
                                        getResources().getString(R.string.open_with),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.open_with),
                                        a -> Application.sendIntentData(this, f.toFile(), "*/*")
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.move),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.move),
                                        a -> {
                                            for (int l = 0; l < selection.length; l++)
                                                if (selection[l])
                                                    origins.add(fileList[l].getPath());
                                            cancelButton.callOnClick();
                                            mainNavigationBar.setVisibility(View.GONE);
                                            copyFileNavigationBar.setVisibility(View.VISIBLE);
                                            movingFile = true;
                                            extractingFile = false;
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.rename),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.rename),
                                        a -> {
                                            final AlertDialog b = new AlertDialog(this);
                                            b.setTitle(R.string.rename);
                                            final AppCompatEditText c = new AppCompatEditText(this);
                                            c.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                            c.setHint(R.string.file_name);
                                            c.setText(f.getName());
                                            b.setView(c);
                                            b.setButtons(new DialogButton(DialogButton.POSITIVE,
                                                    getResources().getString(R.string.selection_confirm),
                                                    d -> {
                                                        CharSequence s = c.getText();
                                                        if (s != null) {
                                                            if (s.length() > 0) {
                                                                try {
                                                                    f.moveTo(new Filex(currentFile.toString(), s.toString()));
                                                                } catch (Exception e) {
                                                                    E.print(this, e);
                                                                }
                                                                handler.postDelayed(this::refreshContent, 200);
                                                            } else
                                                                Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                        } else
                                                            Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                                    }
                                            ), AlertDialog.cancelButton);
                                            b.show();
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.compress),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.compress),
                                        a -> showCompressDialog(f)
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.share),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.share),
                                        a -> {
                                            final Intent i = new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            i.setDataAndType(FileProvider.getUriForFile(this, getPackageName() + ".provider", f.toFile()), "*/*");
                                            startActivity(Intent.createChooser(i, f.getName()));
                                        }
                                ),
                                new SelectDialogItem(
                                        getResources().getString(R.string.properties),
                                        null,
                                        ContextCompat.getDrawable(this, R.drawable.properties),
                                        a -> showPropertyDialog(f)
                                )
                        }, AlertDialog.cancelButton);
                    }
                }
            } else {
                final Filex[] f = new Filex[selection.length];
                for (int i = 0; i < f.length; i++)
                    if (selection[i]) f[i] = fileList[i];
                if (f.length > 0) {
                    SelectDialog.show(this, getResources().getString(R.string.menu), new SelectDialogItem[]{
                            new SelectDialogItem(
                                    getResources().getString(R.string.move),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.move),
                                    a -> {
                                        for (int i = 0; i < selection.length; i++)
                                            if (selection[i]) origins.add(fileList[i].getPath());
                                        cancelButton.callOnClick();
                                        mainNavigationBar.setVisibility(View.GONE);
                                        copyFileNavigationBar.setVisibility(View.VISIBLE);
                                        movingFile = true;
                                        extractingFile = false;
                                    }
                            ),
                            new SelectDialogItem(
                                    getResources().getString(R.string.compress),
                                    null,
                                    ContextCompat.getDrawable(this, R.drawable.compress),
                                    a -> showCompressDialog(f)
                            )
                    }, AlertDialog.cancelButton);
                }
            }
        });
        cancelButton.setOnLongClickListener(Utils::toastContentDescription);
        selectAllButton.setOnLongClickListener(Utils::toastContentDescription);
        copyFileButton.setOnLongClickListener(Utils::toastContentDescription);
        deleteFileButton.setOnLongClickListener(Utils::toastContentDescription);
        moreItemButton.setOnLongClickListener(Utils::toastContentDescription);
        cancelCopyButton.setOnClickListener(view -> {
            if (origins.size() > 0) origins.clear();
            copyFileNavigationBar.setVisibility(View.GONE);
            mainNavigationBar.setVisibility(View.VISIBLE);
        });
        pasteButton.setOnClickListener(view -> {
            if (extractingFile) {
                dialog = ProgressDialog.show(this, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                this.dialog.show();
                handler.post(() -> {
                    // Looper.prepare();
                    try {
                        final Filex a = new Filex(origins.get(0));
                        final String s = a.getName();
                        if (s.toLowerCase().endsWith(".7z")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".7z", ""));
                            final SevenZInput c = new SevenZInput(new FileInputStream(a.getPath()));
                            c.extract(b.getPath());
                            c.close();
                        } else if (s.toLowerCase().endsWith(".tar")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".tar", ""));
                            final TarInput c = new TarInput(new FileInputStream(a.getPath()));
                            c.extract(b.getPath());
                            c.close();
                        } else if (s.toLowerCase().endsWith(".zip")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".zip", ""));
                            final ZipInput c = new ZipInput(a.getPath());
                            c.extract(b.getPath());
                            c.close();
                        } else throw new Exception("Invalid extension");
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                    handler.sendEmptyMessage(0);
                });
            } else {
                dialog = ProgressDialog.show(this, getResources().getString(R.string.copy_file_notice), getResources().getString(R.string.please_wait));
                dialog.show();
                stopping = false;
                isPaused = false;
                handler.post(copyFile);
            }
        });
        cancelCopyButton.setOnLongClickListener(Utils::toastContentDescription);
        pasteButton.setOnLongClickListener(Utils::toastContentDescription);
        toolbar.setOnClickListener(view -> {
            final AlertDialog a = new AlertDialog(this);
            a.setTitle(R.string.jump_to_path);
            final AppCompatEditText b = new AppCompatEditText(this);
            b.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            b.setHint(R.string.path);
            b.setText(currentFile.toString());
            a.setView(b);
            a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                currentFile.setValue(Objects.requireNonNull(b.getText()).toString());
                refreshContent();
            });
            a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.show();
        });
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        navigationView.setNavigationItemSelectedListener(a -> {
            switch (a.getItemId()) {
                case R.id.codeFormatter:
                    startActivity(new Intent(this, CodeFormatter.class));
                    drawerLayout.closeDrawer(navigationView);
                    return true;
                case R.id.uuidGenerator:
                    startActivity(new Intent(this, UuidGenerator.class));
                    drawerLayout.closeDrawer(navigationView);
                    return true;
                case R.id.apkExtractor:
                    startActivity(new Intent(this, ApkExtractor.class));
                    drawerLayout.closeDrawer(navigationView);
                    return true;
                case R.id.rootFeatures:
                    drawerLayout.closeDrawer(navigationView);
                    GmSystem.crs();
                    return true;
                default:
                    drawerLayout.closeDrawer(navigationView);
                    return false;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this::refreshContent);
        handler.post(this);
        fileManagerInitialized = true;
    }

    final void showSignatureDialog(Filex f) {
        final AlertDialog a = new AlertDialog(this);
        a.setTitle(R.string.sign);
        final View v = getLayoutInflater().inflate(R.layout.signature_dialog, null, false);
        final AppCompatEditText b = v.findViewById(R.id.key);
        final AppCompatEditText c = v.findViewById(R.id.scheme);
        final AppCompatEditText d = v.findViewById(R.id.createdBy);
        b.setText(new SpannableStringBuilder("\ud800default"));
        c.setText(new SpannableStringBuilder("\uf8ffV1+V2+V3"));
        d.setText(new SpannableStringBuilder("\ufeffJarSigner"));
        a.setView(v);
        a.setButtons(new DialogButton(DialogButton.POSITIVE, getResources().getString(R.string.selection_confirm), e -> {
            final File s = new File(getFilesDir(), "key");
            assert s.exists() || s.mkdir();
            try {
                String g = Sfx.nonnull(b.getText()).toString();
                if (g.equalsIgnoreCase("\ud800default"))
                    g = null;
                else g = s + File.separator + g;
                new SignJar(f.getPath(), f.removeExtension().getPath() + "_signed" + f.getExtension(), "androi", KeyUtils.loadPrivateKey(g == null ? null : new FileInputStream(g + ".pk8")), KeyUtils.generateCertificate(g == null ? null : new FileInputStream(g + ".x509.pem")))
                        .setCreatedBy(Sfx.nonnull(d.getText()).toString())
                        .setMinSdkVersion(16)
                        .setSignatureScheme(Sfx.nonnull(c.getText()).toString())
                        .sign();
            } catch (Exception g) {
                E.print(this, g);
            }
        }), new DialogButton(DialogButton.NEUTRAL, getResources().getString(R.string._import), e -> {
        }), AlertDialog.cancelButton);
        a.show();
        refreshContent();
    }

    final void showCompressDialog(Filex... f) {
        final AlertDialog a = new AlertDialog(this);
        a.setTitle(R.string.compress);
        final View v = getLayoutInflater().inflate(R.layout.compress_file_dialog, null, false);
        final AppCompatEditText b = v.findViewById(R.id.fileName);
        final AppCompatSpinner c = v.findViewById(R.id.archiveFormat);
        final AppCompatSpinner d = v.findViewById(R.id.compressionLevel);
        final AppCompatSpinner e = v.findViewById(R.id.compressionMethod);
        if (f.length > 1 || !f[0].isDirectory())
            c.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LUtil.supportedArchiveFormats));
        else
            c.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LUtil.supportedPrimaryArchiveFormats));
        d.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CompressLevel.names()));
        e.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Deflated", "Stored", ""}));
        e.setEnabled(false);
        c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LUtil.index = (byte) position;
                try {
                    if (f.length > 1)
                        b.setText(f[0].getParent().getName() + LUtil.archiveFormatExtensions[LUtil.index]);
                    else
                        b.setText(f[0].getName() + LUtil.archiveFormatExtensions[LUtil.index]);
                } catch (Exception l) {
                    // ignore
                }
                if (LUtil.index == 2) {
                    d.setSelection(3);
                    d.setEnabled(true);
                    e.setSelection(0);
                } else if (LUtil.index == 1) {
                    d.setSelection(0);
                    d.setEnabled(false);
                    e.setSelection(1);
                } else {
                    d.setSelection(3);
                    d.setEnabled(false);
                    e.setSelection(2);
                }
            }

            @Override
            public final void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });
        d.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LUtil.index1 = (byte) position;
            }

            @Override
            public final void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });
        e.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LUtil.index2 = (byte) position;
            }

            @Override
            public final void onNothingSelected(AdapterView<?> parent) {
                parent.setSelection(0);
            }
        });
        a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
            final CharSequence s = b.getText();
            if (s != null && s.length() > 0) {
                this.dialog = ProgressDialog.show(this, getResources().getString(R.string.compress_file_notice), getResources().getString(R.string.please_wait));
                this.dialog.show();
                handler.post(() -> {
                    final String h = currentFile.toString() + File.separatorChar + s;
                    try {
                        if (LUtil.index == 0) { // 7zip
                            final SevenZOutput i = new SevenZOutput(new FileOutputStream(h));
                            i.compress(new ArchiveEntry[-1]);
                            i.close();
                        } else if (LUtil.index == 1) { // tar
                            final TarOutput i = new TarOutput(new FileOutputStream(h));
                            i.compress(new ArchiveEntry[-1]);
                            i.close();
                        } else if (LUtil.index == 2) { // zip
                            final ZipOutput i = new ZipOutput(new FileOutputStream(h));
                            final ZipEntry[] j = new ZipEntry[f.length];
                            for (int k = 0; k < j.length; k++) {
                                final String l = f[k].getPath();
                                final ZipEntry m = new ZipEntry(l, l.replace(currentFile.toString(), ""));
                                j[k] = m;
                            }
                            i.level = CompressLevel.values()[LUtil.index1].value;
                            i.method = LUtil.index2 == 0 ? CompressMethod.DEFLATED : CompressMethod.STORED;
                            i.compress(j);
                            i.close();
                        } else if (LUtil.index == 3) { // bzip2
                            CompressUtils.compressBz2(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 4) { // gzip
                            CompressUtils.compressGz(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 5) { // lz4
                            CompressUtils.compressLz4(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 6) { // pack200
                            CompressUtils.compressPack200(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 7) { // snappy
                            CompressUtils.compressSnappy(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 8) { // xz
                            CompressUtils.compressXz(f[0].getInputStream(), new FileOutputStream(h));
                        } else if (LUtil.index == 9) { // zstd
                            CompressUtils.compressZst(f[0].getInputStream(), new FileOutputStream(h));
                        } else {
                            throw new IllegalArgumentException("Unsupported type: " + String.valueOf(LUtil.index));
                        }
                    } catch (Exception r) {
                        E.print(this, r);
                    }
                    handler.sendEmptyMessage(1);
                });
            } else
                Toast.makeText(this, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
        });
        a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
        a.setView(v);
        a.show();
    }

    final void showPropertyDialog(Filex f) {
        AlertDialog a = new AlertDialog(this);
        a.setTitle(R.string.properties);
        final View b = getLayoutInflater().inflate(R.layout.file_property_dialog, null, false);
        ((AppCompatTextView) b.findViewById(R.id.name)).setText(f.getName());
        ((AppCompatTextView) b.findViewById(R.id.parent)).setText(f.getParentPath());
        String c;
        try {
            if (f.isDirectory())
                c = getResources().getString(R.string.directory);
            else if (FileSystem.nativeFs.isSymlink(f.getPath()))
                c = getResources().getString(R.string.symbolic_link);
            else if (FileSystem.nativeFs.isBlockFile(f.getPath()))
                c = getResources().getString(R.string.block_file);
            else if (FileSystem.nativeFs.isSocketFile(f.getPath()))
                c = getResources().getString(R.string.socket_file);
            else if (FileSystem.nativeFs.isFile(f.getPath()))
                c = getResources().getString(R.string.file);
            else c = getResources().getString(R.string.unknown);
        } catch (Exception e) {
            c = getResources().getString(R.string.unknown);
        }
        ((AppCompatTextView) b.findViewById(R.id.type)).setText(c);
        ((AppCompatTextView) b.findViewById(R.id.size)).setText(new FileSizeTransform(f.size()).toString());
        ((AppCompatTextView) b.findViewById(R.id.modifiedTime)).setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", GmSystem.defaultLocale).format(f.lastModified()));
        try {
            c = FilePermission.init(FileSystem.nativeFs.getFilePermission(f.getPath())).toString();
        } catch (Exception e) {
            c = getResources().getString(R.string.unknown);
        }
        ((AppCompatTextView) b.findViewById(R.id.permissions)).setText(c);
        try {
            c = String.valueOf(FileSystem.nativeFs.getFileOwner(f.getPath()));
        } catch (Exception e) {
            c = getResources().getString(R.string.unknown);
        }
        ((AppCompatTextView) b.findViewById(R.id.uid)).setText(c);
        try {
            c = String.valueOf(FileSystem.nativeFs.getFileGroup(f.getPath()));
        } catch (Exception e) {
            c = getResources().getString(R.string.unknown);
        }
        ((AppCompatTextView) b.findViewById(R.id.gid)).setText(c);
        a.setView(b);
        a.setButtons(AlertDialog.dismissButton);
        a.show();
    }

    final void initializeStoragePartitionContents() {
        storagePartitionInternalProgressBar.setMax(100);
        storagePartitionExternalProgressBar.setMax(100);
        storagePartitionRootProgressBar.setMax(100);
        storagePartitionDataProgressBar.setMax(100);
        storagePartitionCacheProgressBar.setMax(100);
        storagePartitionSystemProgressBar.setMax(100);

        storagePartitionInternal.setOnClickListener(view -> {
            currentFile.setValue(storagePath);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
        storagePartitionExternal.setOnClickListener(view -> {
            currentFile.setValue(externalSdPath);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
        storagePartitionRoot.setOnClickListener(view -> {
            currentFile.setValue(rootDirectory);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
        storagePartitionData.setOnClickListener(view -> {
            currentFile.setValue(dataDirectory);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
        storagePartitionCache.setOnClickListener(view -> {
            currentFile.setValue(cacheDirectory);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
        storagePartitionSystem.setOnClickListener(view -> {
            currentFile.setValue(systemDirectory);
            drawerLayout.closeDrawer(navigationView);
            refreshContent();
        });
    }

    final void refreshStoragePartitionContents() {
        final String s = getResources().getString(R.string.storage_usage_display);
        if (new Filex(storagePath).exists()) {
            final MemoryStatus a = new MemoryStatus(storagePath);
            storagePartitionInternalInfo.setText(s.replace("$available_memory", a.getFinalAvailableMemorySize()).replace("$total_memory", a.getFinalTotalMemorySize()));
            storagePartitionInternalProgressBar.setProgress((100 - a.getMemoryUsagePercentage()));
        } else storagePartitionInternal.setVisibility(View.GONE);
        if (externalSdPath != null) {
            final Filex f = new Filex(externalSdPath);
            if (f.exists()) {
                final MemoryStatus a = new MemoryStatus(f.getPath());
                storagePartitionExternalInfo.setText(s.replace("$available_memory", a.getFinalAvailableMemorySize()).replace("$total_memory", a.getFinalTotalMemorySize()));
                storagePartitionExternalProgressBar.setProgress((100 - a.getMemoryUsagePercentage()));
            } else storagePartitionExternal.setVisibility(View.GONE);
        } else storagePartitionExternal.setVisibility(View.GONE);
        if (new Filex(rootDirectory).exists()) {
            final MemoryStatus c = new MemoryStatus(rootDirectory);
            storagePartitionRootInfo.setText(s.replace("$available_memory", c.getFinalAvailableMemorySize()).replace("$total_memory", c.getFinalTotalMemorySize()));
            storagePartitionRootProgressBar.setProgress((100 - c.getMemoryUsagePercentage()));
        } else storagePartitionRoot.setVisibility(LinearLayout.GONE);
        if (new Filex(dataDirectory).exists()) {
            final MemoryStatus d = new MemoryStatus(dataDirectory);
            storagePartitionDataInfo.setText(s.replace("$available_memory", d.getFinalAvailableMemorySize()).replace("$total_memory", d.getFinalTotalMemorySize()));
            storagePartitionDataProgressBar.setProgress((100 - d.getMemoryUsagePercentage()));
        } else storagePartitionData.setVisibility(View.GONE);
        if (new Filex(cacheDirectory).exists()) {
            final MemoryStatus e = new MemoryStatus(cacheDirectory);
            storagePartitionCacheInfo.setText(s.replace("$available_memory", e.getFinalAvailableMemorySize()).replace("$total_memory", e.getFinalTotalMemorySize()));
            storagePartitionCacheProgressBar.setProgress((100 - e.getMemoryUsagePercentage()));
        } else storagePartitionCache.setVisibility(View.GONE);
        if (new Filex(systemDirectory).exists()) {
            final MemoryStatus f = new MemoryStatus(systemDirectory);
            storagePartitionSystemInfo.setText(s.replace("$available_memory", f.getFinalAvailableMemorySize()).replace("$total_memory", f.getFinalTotalMemorySize()));
            storagePartitionSystemProgressBar.setProgress((100 - f.getMemoryUsagePercentage()));
        } else storagePartitionSystem.setVisibility(View.GONE);
    }

    final void goBack() {
        try {
            currentFile.restorePreviousValue();
            refreshContent();
        } catch (Exception e) {
            Toast.makeText(this, R.string.cannot_go_back_message, Toast.LENGTH_SHORT).show();
        }
    }

    final void setSelection(int i) {
        selectedCount = 0;
        selection[i] = !selection[i];
        for (boolean b : selection) {
            if (b) selectedCount++;
        }
        adaptor.setSelection(selection);
        adaptor.notifyDataSetChanged();
        if (selectedCount > 0) {
            mainNavigationBar.setVisibility(View.GONE);
            selectionNavigationBar.setVisibility(View.VISIBLE);
        } else {
            selectionNavigationBar.setVisibility(View.GONE);
            mainNavigationBar.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NonConstantResourceId")
    final void showFileExistsDialog(final Filex a, final Filex b) {
        isPaused = true;
        final AlertDialog c = new AlertDialog(this);
        c.setTitle(R.string.file_exists_message);
        c.setMessage(b.getName());
        final RadioGroup d = new RadioGroup(this, null);
        d.setOrientation(RadioGroup.VERTICAL);
        final AppCompatRadioButton e = new AppCompatRadioButton(this);
        e.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        e.setText(R.string.copy_and_replace);
        e.setId(R.id.a);
        d.addView(e);
        final AppCompatRadioButton f = new AppCompatRadioButton(this);
        f.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        f.setText(R.string.skip_this_file);
        f.setId(R.id.b);
        d.addView(f);
        final AppCompatRadioButton g = new AppCompatRadioButton(this);
        g.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        g.setText(R.string.copy_and_keep_both);
        g.setId(R.id.c);
        d.addView(g);
        d.check(R.id.a);
        c.setView(d);
        c.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
            dialog.dismiss();
            switch (d.getCheckedRadioButtonId()) {
                case R.id.a:
                    if (b.exists()) {
                        try {
                            b.delete();
                        } catch (Exception ex) {
                            E.print(this, ex);
                        }
                    }
                    if (movingFile) try {
                        a.moveTo(b);
                    } catch (Exception ex) {
                        E.print(this, ex);
                    }
                    else try {
                        a.copyTo(b);
                    } catch (Exception ex) {
                        E.print(this, ex);
                    }
                    break;
                case R.id.b:
                    break;
                case R.id.c:
                    Filex t = b;
                    for (int l = 1; t.exists(); l++) {
                        t = new Filex(t.getPath() + " (" + l + ")");
                    }
                    if (movingFile) try {
                        a.moveTo(t);
                    } catch (Exception ex) {
                        E.print(this, ex);
                    }
                    else try {
                        a.copyTo(t);
                    } catch (Exception ex) {
                        E.print(this, ex);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal button ID: " + d.getCheckedRadioButtonId());
            }
            isPaused = false;
        });
        c.setNegativeButton(R.string.selection_cancel, (dialog, which) -> {
            dialog.cancel();
            isPaused = false;
            stopping = true;
        });
        c.show();
    }

    @TestOnly
    final void verifyDexFile() throws Exception {
        final ZipInput a = new ZipInput(getPackageManager().getApplicationInfo(getPackageName(), 0).sourceDir);
        final Filex b = new Filex(Utils.getTempDir(), UUID.randomUUID().toString());
        a.extract(b.getPath());
        final ArrayList<Filex> c = new ArrayList<>();
        for (Filex f : Sfx.nonnull(b.list())) {
            final String s = f.getName();
            if (s.startsWith("classes") && s.endsWith(".dex")) c.add(f);
        }
        if (c.isEmpty()) throw new Exception("No dex files found");
        else {
            final TextWriter d = new TextWriter(new File(getExternalFilesDir(null), ".registered_classes"));
            final StringBuilder h = new StringBuilder();
            for (Filex f : c) {
                final DexFile g = new DexFile(f.getPath());
                for (String s : Collections.list(g.entries())) h.append(s).append("\n");
            }
            d.write(h.toString());
            d.flush();
            d.close();
        }
    }

    private static final class FileData {
        private String name = "";
        private String description = "";
        private Drawable icon = new ColorDrawable(Color.TRANSPARENT.toInt());
        private boolean selected = false;

        private FileData() {
        }
    }

    private static final class FileAdaptor extends BaseAdapter {
        private FileData[] data = new FileData[0];
        private final Drawable defaultDrawable;
        private final Drawable selectedDrawable;

        private FileAdaptor() {
            final StateListDrawable a = new StateListDrawable();
            a.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_focused}, new ColorDrawable(Color.GRAY.toInt()));
            a.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT.toInt()));
            this.defaultDrawable = a;
            this.selectedDrawable = new ColorDrawable(Color.GRAY.toInt());
        }

        @Override
        public final int getCount() {
            return data.length;
        }

        @Override
        public final FileData getItem(int position) {
            return data[position];
        }

        public final void setData(FileData[] d) {
            data = d == null ? new FileData[0] : d;
            notifyDataSetChanged();
        }

        public final void setSelection(boolean[] d) {
            for (int i = 0; i < d.length; i++)
                data[i].selected = d[i];
        }

        @Override
        public final long getItemId(int position) {
            return 0;
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_manager_items, parent, false);
            ViewHolder h = (ViewHolder) convertView.getTag();
            if (h == null) {
                h = new ViewHolder(convertView.findViewById(R.id.itemIcon), convertView.findViewById(R.id.fileName), convertView.findViewById(R.id.fileInfo));
                convertView.setTag(h);
            }
            h.fileIcon.setImageDrawable(data[position].icon);
            h.fileName.setText(data[position].name);
            h.fileInfo.setText(data[position].description);
            convertView.setBackground(data[position].selected ? selectedDrawable : defaultDrawable);
            return convertView;
        }

        private static final class ViewHolder {
            public final AppCompatImageView fileIcon;
            public final AppCompatTextView fileName;
            public final AppCompatTextView fileInfo;

            public ViewHolder(AppCompatImageView fileIcon, AppCompatTextView fileName, AppCompatTextView fileInfo) {
                this.fileIcon = fileIcon;
                this.fileName = fileName;
                this.fileInfo = fileInfo;
            }
        }
    }

    private enum FileExtension {
        DEFAULT,
        UNKNOWN,
        IMAGE,
        MUSIC,
        TEXT,
        CODE,
        ARCHIVE,
        VIDEO,
        ZIP_ARCHIVE,
        APK,
        XAPK,
        HTML,
        DISK_IMAGE,
        PDF,
        COMPRESSED;

        FileExtension() {
        }

        public static FileExtension CheckFileExtension(Filex f) {
            final String a = f.getExtension();
            if (isImageFile(a)) return IMAGE;
            else if (isMusicFile(a)) return MUSIC;
            else if (isTextFile(a)) return TEXT;
            else if (isCodeFile(a)) return CODE;
            else if (isArchiveFile(a)) return ARCHIVE;
            else if (isVideoFile(a)) return VIDEO;
            else if (isZipFile(a)) return ZIP_ARCHIVE;
            else if (isApkFile(a)) return APK;
            else if (isXApkFile(a)) return XAPK;
            else if (isHtmlFile(a)) return HTML;
            else if (isDiskImageFile(a)) return DISK_IMAGE;
            else if (isPdfFile(a)) return PDF;
            else if (isCompressedFile(a)) return COMPRESSED;
            else return UNKNOWN;
        }

        private static boolean isImageFile(String s) {
            return s.equals(".png") || s.equals(".jpg") || s.equals(".jpeg") || s.equals(".bmp") || s.equals(".webp") || s.equals(".heif");
        }

        private static boolean isMusicFile(String s) {
            return s.equals(".aac") || s.equals(".mkv") || s.equals(".mp3") || s.equals(".m4a") || s.equals(".ogg") || s.equals(".amr") || s.equals(".flac") || s.equals(".mid") || s.equals(".wav");
        }

        private static boolean isTextFile(String s) {
            return s.equals(".txt") || s.equals(".ini") || s.equals(".log") || s.equals(".properties");
        }

        private static boolean isCodeFile(String s) {
            return s.equals(".java") || s.equals(".cpp") || s.equals(".c") || s.equals(".h") || s.equals(".cs") || s.equals(".xml") || s.equals(".css") || s.equals(".kt") || s.equals(".js") || s.equals(".json") || s.equals(".py");
        }

        private static boolean isArchiveFile(String s) {
            return s.equals(".7z") || s.equals(".tar");
        }

        private static boolean isVideoFile(String s) {
            return s.equals(".mp4") || s.equals(".3gp") || s.equals(".webm") || s.equals(".mpeg");
        }

        private static boolean isZipFile(String s) {
            return s.equals(".zip") || s.equals(".pkg") || s.equals(".jar") || s.equals(".obb");
        }

        private static boolean isApkFile(String s) {
            return s.equals(".apk");
        }

        private static boolean isXApkFile(String s) {
            return s.equals(".xapk") || s.equals(".apks");
        }

        private static boolean isHtmlFile(String s) {
            return s.equals(".htm") || s.equals(".html");
        }

        private static boolean isDiskImageFile(String s) {
            return s.equals(".img") || s.equals(".iso");
        }

        private static boolean isPdfFile(String s) {
            return s.equals(".pdf");
        }

        private static boolean isCompressedFile(String s) {
            return s.equals(".bz2") || s.equals(".gz") || s.equals(".lz4") || s.equals(".pack") || s.equals(".sz") || s.equals(".xz") || s.equals(".zst");
        }
    }

    private static final class FileSizeTransform {
        public final long bytes;
        public final double kilobytes;
        public final double megabytes;
        public final double gigabytes;
        public final double terabytes;
        public final double petabytes;
        public final double exabytes;
        public final double zettabytes;
        public final double yottabytes;

        private FileSizeTransform(long l) {
            bytes = l;
            kilobytes = bytes >= 1024 ? bytes / 1024d : 0;
            megabytes = kilobytes >= 1024 ? kilobytes / 1024d : 0;
            gigabytes = megabytes >= 1024 ? megabytes / 1024d : 0;
            terabytes = gigabytes >= 1024 ? gigabytes / 1024d : 0;
            petabytes = terabytes >= 1024 ? terabytes / 1024d : 0;
            exabytes = petabytes >= 1024 ? petabytes / 1024d : 0;
            zettabytes = exabytes >= 1024 ? exabytes / 1024d : 0;
            yottabytes = zettabytes >= 1024 ? zettabytes / 1024d : 0;
        }

        @Override
        public final String toString() {
            final DecimalFormat a = new DecimalFormat("0.00");
            if (bytes >= 1) {
                if (kilobytes >= 1) {
                    if (megabytes >= 1) {
                        if (gigabytes >= 1) {
                            if (terabytes >= 1) {
                                if (petabytes >= 1) {
                                    if (exabytes >= 1) {
                                        if (zettabytes >= 1) {
                                            if (yottabytes >= 1) {
                                                return (a.format(yottabytes) + "YB");
                                            }
                                            return (a.format(zettabytes) + "ZB");
                                        }
                                        return (a.format(exabytes) + "EB");
                                    }
                                    return (a.format(petabytes) + "PB");
                                }
                                return (a.format(terabytes) + "TB");
                            }
                            return (a.format(gigabytes) + "GB");
                        }
                        return (a.format(megabytes) + "MB");
                    }
                    return (a.format(kilobytes) + "KB");
                }
                return (bytes + "B");
            }
            return "0B";
        }
    }

    private static final class MemoryStatus {
        private final String path;
        private final long availableMemorySize;
        private final long totalMemorySize;

        private MemoryStatus(String s) {
            this.path = s;
            this.availableMemorySize = getAvailableMemorySize();
            this.totalMemorySize = getTotalMemorySize();
        }

        public final byte getMemoryUsagePercentage() {
            return (byte) ((double) availableMemorySize / (double) totalMemorySize * 100);
        }

        public final String getFinalAvailableMemorySize() {
            return new FileSizeTransform(availableMemorySize).toString();
        }

        public final String getFinalTotalMemorySize() {
            return new FileSizeTransform(totalMemorySize).toString();
        }

        private long getAvailableMemorySize() {
            try {
                final StatFs stat = new StatFs(path);
                final long blockSize = stat.getBlockSize();
                final long availableBlocks = stat.getAvailableBlocks();
                return availableBlocks * blockSize;
            } catch (Exception e) {
                return -1;
            }
        }

        private long getTotalMemorySize() {
            try {
                final StatFs stat = new StatFs(path);
                final long blockSize = stat.getBlockSize();
                final long totalBlocks = stat.getBlockCount();
                return totalBlocks * blockSize;
            } catch (Exception e) {
                return -1;
            }
        }
    }

    private static final class LUtil {
        private static final String[] supportedArchiveFormats = a(ArchiveType.AC, false);
        private static final String[] supportedPrimaryArchiveFormats = a(ArchiveType.SC, false);
        private static final String[] archiveFormatExtensions = a(ArchiveType.AC, true);
        private static byte index = 0;
        private static byte index1 = 0;
        private static byte index2 = 0;

        private static String[] a(ArchiveType[] a, boolean b) {
            final String[] c = new String[a.length];
            for (int i = 0; i < c.length; i++)
                c[i] = b ? a[i].extension : a[i].name;
            return c;
        }

        private LUtil() {
        }
    }
}