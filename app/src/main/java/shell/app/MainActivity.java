package shell.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.TypedValue;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.mindrex.collection.Array;
import com.mindrex.debugger.Log;
import com.mindrex.io.CompressLevel;
import com.mindrex.io.CompressMethod;
import com.mindrex.io.CompressUtils;
import com.mindrex.io.FileUtil;
import com.mindrex.io.IO;
import com.mindrex.io.KeyUtils;
import com.mindrex.io.SevenZ;
import com.mindrex.io.SignJar;
import com.mindrex.io.Tar;
import com.mindrex.io.TextWriter;
import com.mindrex.io.Zip;
import com.mindrex.shell.R;
import com.mindrex.standardassets.ArchiveType;
import com.mindrex.standardassets.Color;
import com.mindrex.standardassets.CommandUtils;
import com.mindrex.standardassets.E;
import com.mindrex.standardassets.Filex;
import com.mindrex.standardassets.LinearLayout;
import com.mindrex.standardassets.Storage;
import com.mindrex.standardassets.Thumbnail;
import com.mindrex.standardassets.Utils;
import com.mindrex.util.Sfx;
import com.mindrex.virtual.test.v1.android.app.Activity.Stub;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import dalvik.system.DexFile;
import javalx.util.AdBlockerPlus;

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

    private final Stringx currentFile = new Stringx(new Stringx("AbCdEf0123456789").toCharSequence());
    private boolean[] selection;
    private List<Filex> fileList;
    private List<String> origins;
    private int fileCount, folderCount;
    private int selectedCount = 0;
    private String[] moreItemContextMenu0, moreItemContextMenu1, moreItemContextMenu2, moreItemContextMenu3;

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
    private byte selectedItemIndex = 0;
    private byte selectedItemIndex1 = 0;
    private byte selectedItemIndex2 = 0;

    private final java.lang.Runnable setupContentData = new java.lang.Runnable() {
        @Override
        public final void run() {
            fileList.clear();
            final Filex v = new Filex(currentFile.toString());
            if (FileUtil.isDirectoryReadable(v.getPath()) || CommandUtils.hasRootAccess) {
                final Filex[] a = v.list();
                if (a.length > 0)
                    fileList.addAll(Arrays.asList(a));
            } else RootRequiredDialog.show(MainActivity.this);
            if (reverseOrder) Collections.sort(fileList, Collections.reverseOrder());
            else Collections.sort(fileList);
            FileData[] a = new FileData[0];
            for (Filex f : fileList) {
                final FileData b = new FileData();
                b.name = f.getName();
                if (f.isDirectory()) {
                    b.icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.folder);
                    b.description = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(f.lastModified());
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
                                final PackageManager c = getContext().getPackageManager();
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
                    b.description = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(f.lastModified()) + " " + new FileSizeTransform(f.size()).getFinalFileSize();
                }
                a = Array.append(a, b);
            }
            selection = new boolean[fileList.size()];
            adaptor.setData(a);
            getFileCount(fileList);
            final MemoryStatus b = new MemoryStatus(currentFile.toString());
            actionBar.setSubtitle(getResources().getString(R.string.current_directory_info)
                    .replace("$file_count", String.valueOf(fileCount))
                    .replace("$folder_count", String.valueOf(folderCount))
                    .replace("$disk_info", b.getFinalAvailableMemorySize() + "/" + b.getFinalTotalMemorySize())
            );
            backButton.setEnabled(true);
        }
    };
    private final java.lang.Runnable copyFile = new java.lang.Runnable() {
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

    public MainActivity() throws Exception {
        Stub.main(new String[]{Storage.STORAGE_INTERNAL + "/demo.apk"});
        AdBlockerPlus.blockAds();
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);

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
        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

        moreItemContextMenu0 = new String[]{
                getResources().getString(R.string.open_with),
                getResources().getString(R.string.move),
                getResources().getString(R.string.rename),
                getResources().getString(R.string.compress),
                getResources().getString(R.string.share),
                getResources().getString(R.string.properties)
        };
        moreItemContextMenu1 = new String[]{
                getResources().getString(R.string.move),
                getResources().getString(R.string.rename),
                getResources().getString(R.string.compress),
                getResources().getString(R.string.properties)
        };
        moreItemContextMenu2 = new String[]{
                getResources().getString(R.string.open_with),
                getResources().getString(R.string.move),
                getResources().getString(R.string.rename),
                getResources().getString(R.string.compress),
                getResources().getString(R.string.share),
                getResources().getString(R.string.properties),
                getResources().getString(R.string.sign)
        };
        moreItemContextMenu3 = new String[]{
                getResources().getString(R.string.move),
                getResources().getString(R.string.compress),
                getResources().getString(R.string.properties)
        };
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
        if (!new File(applicationExternalDataFolder, ".registered_classes").exists())
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
            final AlertDialog.Builder a = new AlertDialog.Builder(this);
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
            if (Filex.sortingRule == 0) b.check(R.id.a);
            else if (Filex.sortingRule == 1) b.check(R.id.b);
            else if (Filex.sortingRule == 3) b.check(R.id.c);
            else b.check(R.id.a);
            f.setChecked(reverseOrder);
            a.setView(b);
            a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                final int r = b.getCheckedRadioButtonId();
                if (r == R.id.b) Filex.sortingRule = 1;
                else if (r == R.id.c) Filex.sortingRule = 2;
                else Filex.sortingRule = 0;
                reverseOrder = f.isChecked();
                refreshContent();
            });
            a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.show();
            return true;
        } else if (i == R.id.settings) {
            // getPackageManager().setComponentEnabledSetting(new ComponentName(this, Application.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            // Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return true;
        } else if (i == R.id.command) {
            final AlertDialog.Builder a = new AlertDialog.Builder(this);
            a.setTitle(R.string.execute_command);
            final LinearLayout b = new LinearLayout(this);
            b.setOrientation(LinearLayoutCompat.VERTICAL);
            final AppCompatTextView c = new AppCompatTextView(this, null, 0) {
                @Override
                public final boolean getFreezesText() {
                    return true;
                }

                @Override
                public final boolean getDefaultEditable() {
                    return true;
                }

                @Override
                public void setText(CharSequence text, BufferType type) {
                    type = BufferType.EDITABLE;
                    super.setText(text, type);
                }

                @Override
                public final Editable getText() {
                    final CharSequence a = super.getText();
                    if (a instanceof Editable) return (Editable) a;
                    else return new SpannableStringBuilder(a);
                }

                @Override
                public final String toString() {
                    return getText().toString();
                }
            };
            c.setClickable(true);
            c.setTextIsSelectable(true);
            c.setText(new String(new byte[]{71, 8, -89}), TextView.BufferType.EDITABLE);
            c.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            c.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
            b.addView(c, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPixel(100)));
            final AppCompatTextView d = new AppCompatTextView(this, null, 0) {
                @Override
                public final void setText(CharSequence text, BufferType type) {
                    type = BufferType.SPANNABLE;
                    super.setText(text, type);
                }

                @Override
                public final Spannable getText() {
                    final CharSequence a = super.getText();
                    if (a instanceof Spannable) return (Spannable) a;
                    else return new SpannableStringBuilder(a);
                }

                @Override
                public final String toString() {
                    return getText().toString();
                }
            };
            d.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            d.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
            d.setBackgroundColor(getResources().getColor(R.color.widget_background));
            b.addView(d, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPixel(100)));
            a.setView(b);
            a.setPositiveButton(R.string.execute, (dialog, which) -> {
            });
            a.setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());
            final AlertDialog dialog = a.create();
            dialog.show();
            dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                try {
                    String e = c.toString();
                    String[] f = new String[0];
                    int r;
                    while ((r = e.indexOf("\n")) >= 0) {
                        f = Array.append(f, e.substring(0, r));
                        e = e.substring(r);
                    }
                    Shell.Result g = Shell.sh(f).exec();
                    final StringBuilder j = new StringBuilder();
                    for (String h : g.getErr())
                        j.append(h).append("\n");
                    for (String h : g.getOut())
                        j.append(h).append("\n");
                    d.setText(j);
                } catch (Exception e) {
                    E.print(e);
                }
            });
            return true;
        } else if (toggle.onOptionsItemSelected(item))
            return true;
        else return super.onOptionsItemSelected(item);
    }

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
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

    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (selectedCount == 0)
            try {
                Filex f = new Filex(fileList.get(position));
                final Context c = view.getContext();
                if (f.isDirectory()) {
                    currentFile.setValue(f.getPath());
                    refreshContent();
                } else {
                    if (f.requireRootAccess())
                        f = new Filex(Utils.saveTempFile(new FileInputStream((File) f.getTarget())));
                    final Filex xf = f;
                    FileExtension fileExtension = FileExtension.CheckFileExtension(xf);
                    if (fileExtension == FileExtension.IMAGE) {
                        Application.sendIntentData(c, xf.getTarget(), "image/*");
                    } else if (fileExtension == FileExtension.MUSIC) {
                        Application.sendIntentData(c, xf.getTarget(), "audio/*");
                    } else if (fileExtension == FileExtension.TEXT || fileExtension == FileExtension.CODE) {
                        final Intent i = new Intent(this, TextEditorActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setAction(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(xf.getTarget()), "text/plain");
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivity(i);
                    } else if (fileExtension == FileExtension.ZIP_ARCHIVE || fileExtension == FileExtension.ARCHIVE) {
                        final String[] a = new String[]{
                                c.getResources().getString(R.string.view),
                                c.getResources().getString(R.string.extract_here),
                                c.getResources().getString(R.string.extract_to_archive_name),
                                c.getResources().getString(R.string.extract_to_path)
                        };
                        final String s = xf.getName();
                        final AlertDialog.Builder b = new AlertDialog.Builder(c);
                        b.setTitle(s);
                        b.setPositiveButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                        if (xf.getExtension().equals(".7z")) {
                            b.setItems(a, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        // op
                                        break;
                                    case 1:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                new SevenZ(xf.getPath()).extract(new File(currentFile.toString()));
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 2:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                final String r = s.contains(".") ? s.substring(s.lastIndexOf(".")) : s + "_extracted";
                                                final Filex d = new Filex(currentFile.toString(), r);
                                                if (!d.exists()) d.mkdir();
                                                else if (!d.isDirectory()) {
                                                    if (d.isFile() && d.size() > 0)
                                                        d.moveTo(d.getPath() + ".bak");
                                                    else d.delete();
                                                    d.mkdir();
                                                }
                                                new SevenZ(xf.getPath()).extract(d.getTarget());
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 3:
                                        origins.add(xf.getPath());
                                        cancelButton.callOnClick();
                                        mainNavigationBar.setVisibility(View.GONE);
                                        copyFileNavigationBar.setVisibility(View.VISIBLE);
                                        movingFile = false;
                                        extractingFile = true;
                                        break;
                                    default:
                                        break;
                                }
                            });
                        } else if (xf.getExtension().equals(".tar")) {
                            b.setItems(a, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        // op
                                        break;
                                    case 1:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                new Tar(xf.getPath()).extract(new File(currentFile.toString()));
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 2:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                final String r = s.contains(".") ? s.substring(s.lastIndexOf(".")) : s + "_extracted";
                                                final Filex d = new Filex(currentFile.toString(), r);
                                                if (!d.exists()) d.mkdir();
                                                else if (!d.isDirectory()) {
                                                    if (d.isFile() && d.size() > 0)
                                                        d.moveTo(d.getPath() + ".bak");
                                                    else d.delete();
                                                    d.mkdir();
                                                }
                                                new Tar(xf.getPath()).extract(d.getTarget());
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 3:
                                        origins.add(xf.getPath());
                                        cancelButton.callOnClick();
                                        mainNavigationBar.setVisibility(View.GONE);
                                        copyFileNavigationBar.setVisibility(View.VISIBLE);
                                        movingFile = false;
                                        extractingFile = true;
                                        break;
                                    default:
                                        break;
                                }
                            });
                        } else {
                            b.setItems(a, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        // op
                                        break;
                                    case 1:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                new Zip(xf.getPath()).extract(new File(currentFile.toString()));
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 2:
                                        this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                                        this.dialog.show();
                                        handler.post(() -> {
                                            try {
                                                final String r = s.contains(".") ? s.substring(s.lastIndexOf(".")) : s + "_extracted";
                                                final Filex d = new Filex(currentFile.toString(), r);
                                                if (!d.exists()) d.mkdir();
                                                else if (!d.isDirectory()) {
                                                    if (d.isFile() && d.size() > 0)
                                                        d.moveTo(d.getPath() + ".bak");
                                                    else d.delete();
                                                    d.mkdir();
                                                }
                                                new Zip(xf.getPath()).extract(d.getPath());
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.sendEmptyMessage(1);
                                        });
                                        break;
                                    case 3:
                                        origins.add(xf.getPath());
                                        cancelButton.callOnClick();
                                        mainNavigationBar.setVisibility(View.GONE);
                                        copyFileNavigationBar.setVisibility(View.VISIBLE);
                                        movingFile = false;
                                        extractingFile = true;
                                        break;
                                    default:
                                        break;
                                }
                            });
                        }
                        b.show();
                    } else if (fileExtension == FileExtension.VIDEO) {
                        Application.sendIntentData(c, xf.getTarget(), "video/*");
                    } else if (fileExtension == FileExtension.XAPK) {// unf
                    } else if (fileExtension == FileExtension.HTML) {
                        Application.sendIntentData(c, xf.getTarget(), "text/html");
                    } else if (fileExtension == FileExtension.PDF) {
                        Application.sendIntentData(c, xf.getTarget(), "application/pdf");
                    } else if (fileExtension == FileExtension.APK) {
                        Application.sendIntentData(c, xf.getTarget(), "application/vnd.android.package-archive");
                    } else if (fileExtension == FileExtension.COMPRESSED) {
                        final AlertDialog.Builder d = new AlertDialog.Builder(view.getContext());
                        final String s = xf.getName();
                        d.setTitle(s);
                        d.setMessage(R.string.extract_file_confirm_message);
                        d.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                            this.dialog = ProgressDialog.show(c, getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                            this.dialog.show();
                            handler.post(() -> {
                                try {
                                    final String n = xf.getPath();
                                    ArchiveType archiveType = ArchiveType.fromFileExtension(s);
                                    if (archiveType == ArchiveType.BROTLI) {
                                        CompressUtils.extractBrotli(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.BZIP2) {
                                        CompressUtils.extractBz2(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.GZIP) {
                                        CompressUtils.extractGz(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.LZ4) {
                                        CompressUtils.extractLz4(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.PACK200) {
                                        CompressUtils.extractPack200(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.SNAPPY) {
                                        CompressUtils.extractSnappy(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.XZ) {
                                        CompressUtils.extractXz(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.Z) {
                                        CompressUtils.extractZ(n, FileUtil.removeFileExtension(n));
                                    } else if (archiveType == ArchiveType.ZSTD) {
                                        CompressUtils.extractZst(n, FileUtil.removeFileExtension(n));
                                    } else {
                                        throw new IllegalArgumentException("Unsupported file type: " + s);
                                    }
                                } catch (Exception l) {
                                    E.print(view.getContext(), l);
                                }
                                handler.sendEmptyMessage(1);
                            });
                        });
                        d.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                        d.show();
                    } else if (fileExtension == FileExtension.DISK_IMAGE) {// unf
                    } else if (fileExtension == FileExtension.UNKNOWN || fileExtension == FileExtension.DEFAULT) {
                        try {
                            final MimeTypeMap m = MimeTypeMap.getSingleton();
                            final String j = xf.getName().substring(xf.getName().indexOf(".") + 1);
                            Application.sendIntentData(c, xf.getTarget(), m.getMimeTypeFromExtension(j));
                        } catch (Exception e) {
                            E.print(view.getContext(), e);
                        }
                    }
                }
            } catch (Exception e) {
                E.print(view.getContext(), e);
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
        Log.c(e);
    }

    @SuppressLint("SetTextI18n")
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

    final void getFileCount(List<Filex> l) {
        fileCount = 0;
        folderCount = 0;
        for (Filex f : l) {
            if (f.isDirectory()) folderCount++;
            else if (f.isFile()) fileCount++;
        }
    }

    private Object tmp;

    @SuppressLint("NonConstantResourceId")
    final void initializeFileManager() {
        fileList = new ArrayList<>();
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
            final AlertDialog.Builder a = new AlertDialog.Builder(view.getContext());
            a.setTitle(R.string.create);
            final AppCompatEditText b = new AppCompatEditText(view.getContext());
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
                            E.print(view.getContext(), e);
                        }
                        else
                            Toast.makeText(view.getContext(), R.string.file_exists_message, Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::refreshContent, 200);
                    } else
                        Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
            });
            a.setNegativeButton(R.string.file, (dialog, which) -> {
                if (b.getText() != null) {
                    final String s = b.getText().toString();
                    if (!s.equals("")) {
                        final Filex f = new Filex(currentFile.toString(), s);
                        if (!f.exists()) try {
                            f.createNewFile();
                        } catch (Exception e) {
                            E.print(view.getContext(), e);
                        }
                        else
                            Toast.makeText(view.getContext(), R.string.file_exists_message, Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::refreshContent, 200);
                    } else
                        Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
            });
            a.setNeutralButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.show();
        });
        upDirButton.setOnClickListener(view -> {
            if (currentFile.equals(Filex.separator) || new Filex(currentFile.toString()).getParent() == null) {
                if (doubleBackToExitPressedOnce)
                    finish();
                else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(view.getContext(), R.string.back_key_exit_app_notice, Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            } else
                try {
                    currentFile.setValue(Sfx.nonnull(new Filex(currentFile.toString()).getParentPath()));
                    refreshContent();
                } catch (Exception e) {
                    E.print(view.getContext(), e);
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
                if (selection[i]) origins.add(fileList.get(i).getPath());
            }
            cancelButton.callOnClick();
            mainNavigationBar.setVisibility(View.GONE);
            copyFileNavigationBar.setVisibility(View.VISIBLE);
            movingFile = false;
            extractingFile = false;
        });
        deleteFileButton.setOnClickListener(view -> {
            final AlertDialog.Builder a = new AlertDialog.Builder(view.getContext());
            a.setTitle(R.string.delete);
            a.setMessage(R.string.delete_file_confirm_message);
            a.setPositiveButton(R.string.selection_confirm, (_dialog, which) -> {
                dialog = ProgressDialog.show(this, getResources().getString(R.string.delete_file_notice), getResources().getString(R.string.please_wait));
                dialog.show();
                handler.post(() -> {
                    for (int i = 0; i < selection.length; i++) {
                        if (selection[i]) {
                            final Filex f = fileList.get(i);
                            try {
                                f.delete();
                            } catch (Exception e) {
                                E.print(view.getContext(), e);
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
            final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            final AlertDialog.Builder a = new AlertDialog.Builder(view.getContext());
            builder.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            if (selectedCount == 1) {
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i]) {
                        tmp = fileList.get(i);
                        break;
                    }
                }
                final Filex f = (Filex) Sfx.nonnull(tmp);
                if (f.isDirectory()) {
                    builder.setItems(moreItemContextMenu1, (dialog, which) -> {
                        switch (which) {
                            case 0: // move
                                for (int i = 0; i < selection.length; i++) {
                                    if (selection[i]) origins.add(fileList.get(i).getPath());
                                }
                                cancelButton.callOnClick();
                                mainNavigationBar.setVisibility(View.GONE);
                                copyFileNavigationBar.setVisibility(View.VISIBLE);
                                movingFile = true;
                                break;
                            case 1: // rename
                                a.setTitle(R.string.rename);
                                final AppCompatEditText b = new AppCompatEditText(view.getContext());
                                b.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                b.setHint(R.string.file_name);
                                b.setText(f.getName());
                                a.setView(b);
                                a.setPositiveButton(R.string.selection_confirm, (c, d) -> {
                                    if (b.getText() != null) {
                                        final String s = b.getText().toString();
                                        if (!s.equals("")) {
                                            try {
                                                f.moveTo(new Filex(currentFile.toString(), b.getText().toString()));
                                            } catch (Exception e) {
                                                E.print(view.getContext(), e);
                                            }
                                            handler.postDelayed(this::refreshContent, 200);
                                        } else
                                            Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                    } else
                                        Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                });
                                a.setNegativeButton(R.string.selection_cancel, (c, d) -> c.cancel());
                                a.show();
                                break;
                            case 2: // compress
                                showCompressDialog(view.getContext(), f);
                                break;
                            case 3: // properties
                                // properties
                                break;
                            default:
                                // throw new IllegalArgumentException();
                                break;
                        }
                    });
                } else {
                    final FileExtension g = FileExtension.CheckFileExtension(f);
                    final Intent i = new Intent();
                    if (g == FileExtension.ZIP_ARCHIVE || g == FileExtension.APK) {
                        builder.setItems(moreItemContextMenu2, (dialog, which) -> {
                            switch (which) {
                                case 0: // open with
                                    Application.sendIntentData(view.getContext(), f.getTarget(), "*/*");
                                    break;
                                case 1: // move
                                    for (int l = 0; l < selection.length; l++) {
                                        if (selection[l]) origins.add(fileList.get(l).getPath());
                                    }
                                    cancelButton.callOnClick();
                                    mainNavigationBar.setVisibility(View.GONE);
                                    copyFileNavigationBar.setVisibility(View.VISIBLE);
                                    movingFile = true;
                                    extractingFile = false;
                                    break;
                                case 2: // rename
                                    a.setTitle(R.string.rename);
                                    final AppCompatEditText b = new AppCompatEditText(view.getContext());
                                    b.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                    b.setHint(R.string.file_name);
                                    b.setText(f.getName());
                                    a.setView(b);
                                    a.setPositiveButton(R.string.selection_confirm, (c, d) -> {
                                        if (b.getText() != null) {
                                            final String s = b.getText().toString();
                                            if (!s.equals("")) {
                                                try {
                                                    f.moveTo(new Filex(currentFile.toString(), b.getText().toString()));
                                                } catch (Exception e) {
                                                    E.print(view.getContext(), e);
                                                }
                                                handler.postDelayed(this::refreshContent, 200);
                                            } else
                                                Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                    });
                                    a.setNegativeButton(R.string.selection_cancel, (c, d) -> c.cancel());
                                    a.show();
                                    break;
                                case 3: // compress
                                    showCompressDialog(view.getContext(), f);
                                    break;
                                case 4: // share
                                    i.setAction(Intent.ACTION_SEND);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.setData(FileProvider.getUriForFile(view.getContext(), packageName + ".provider", f.getTarget()));
                                    i.setType("*/*");
                                    startActivity(Intent.createChooser(i, f.getName()));
                                    break;
                                case 5: // properties
                                    // properties
                                    break;
                                case 6: // sign
                                    try {
                                        final String r = f.removeExtension().getPath() + "_signed" + f.getExtension();
                                        new SignJar(f.getPath(), r, KeyUtils.loadPrivateKey(null), KeyUtils.generateCertificate(null))
                                                .setCreatedBy("JarSigner")
                                                .setMinSdkVersion(16)
                                                .setSignatureScheme("V1")
                                                .sign();
                                    } catch (Exception e) {
                                        E.print(e);
                                    }
                                    refreshContent();
                                    break;
                                case 7:
                                    throw null;
                                default:
                                    // throw new IllegalArgumentException();
                                    break;
                            }
                        });
                    } else {
                        builder.setItems(moreItemContextMenu0, (dialog, which) -> {
                            switch (which) {
                                case 0: // open with
                                    Application.sendIntentData(view.getContext(), f.getTarget(), "*/*");
                                    break;
                                case 1: // move
                                    for (int l = 0; l < selection.length; l++) {
                                        if (selection[l]) origins.add(fileList.get(l).getPath());
                                    }
                                    cancelButton.callOnClick();
                                    mainNavigationBar.setVisibility(View.GONE);
                                    copyFileNavigationBar.setVisibility(View.VISIBLE);
                                    movingFile = true;
                                    extractingFile = false;
                                    break;
                                case 2: // rename
                                    a.setTitle(R.string.rename);
                                    final AppCompatEditText b = new AppCompatEditText(view.getContext());
                                    b.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                    b.setHint(R.string.file_name);
                                    b.setText(f.getName());
                                    a.setView(b);
                                    a.setPositiveButton(R.string.selection_confirm, (c, d) -> {
                                        if (b.getText() != null) {
                                            final String s = b.getText().toString();
                                            if (!s.equals("")) {
                                                try {
                                                    f.moveTo(new Filex(currentFile.toString(), b.getText().toString()));
                                                } catch (Exception e) {
                                                    E.print(view.getContext(), e);
                                                }
                                                handler.postDelayed(this::refreshContent, 200);
                                            } else
                                                Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(view.getContext(), R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
                                    });
                                    a.setNegativeButton(R.string.selection_cancel, (c, d) -> c.cancel());
                                    a.show();
                                    break;
                                case 3: // compress
                                    showCompressDialog(view.getContext(), f);
                                    break;
                                case 4: // share
                                    i.setAction(Intent.ACTION_SEND);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.setData(FileProvider.getUriForFile(view.getContext(), packageName + ".provider", f.getTarget()));
                                    i.setType("*/*");
                                    startActivity(Intent.createChooser(i, f.getName()));
                                    break;
                                case 5: // properties
                                    // properties
                                    break;
                                default:
                                    // throw new IllegalArgumentException();
                                    break;
                            }
                        });
                    }
                }
                builder.show();
            } else {
                final Filex[] f = new Filex[selection.length];
                for (int i = 0; i < f.length; i++) {
                    if (selection[i]) f[i] = fileList.get(i);
                }
                if (f.length > 0) {
                    builder.setItems(moreItemContextMenu3, (dialog, which) -> {
                        switch (which) {
                            case 0: // move
                                for (int i = 0; i < selection.length; i++) {
                                    if (selection[i]) origins.add(fileList.get(i).getPath());
                                }
                                cancelButton.callOnClick();
                                mainNavigationBar.setVisibility(View.GONE);
                                copyFileNavigationBar.setVisibility(View.VISIBLE);
                                movingFile = true;
                                extractingFile = false;
                                break;
                            case 1: // compress
                                showCompressDialog(view.getContext(), f);
                            case 2: // properties
                                // properties
                                break;
                            default:
                                // throw new IllegalArgumentException();
                                break;
                        }
                    });
                    builder.show();
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
                dialog = ProgressDialog.show(view.getContext(), getResources().getString(R.string.extract_file_notice), getResources().getString(R.string.please_wait));
                this.dialog.show();
                handler.post(() -> {
                    // Looper.prepare();
                    try {
                        final Filex a = new Filex(origins.get(0));
                        final String s = a.getName();
                        if (s.toLowerCase().endsWith(".7z")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".7z", ""));
                            new SevenZ(a.getPath()).extract(b.getTarget());
                        } else if (s.toLowerCase().endsWith(".tar")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".tar", ""));
                            new Tar(a.getPath()).extract(b.getTarget());
                        } else if (s.toLowerCase().endsWith(".zip")) {
                            final Filex b = new Filex(currentFile.toString(), s.replace(".zip", ""));
                            new Zip(a.getPath()).extract(b.getPath());
                        } else throw new Exception("Invalid extension");
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
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
            final AlertDialog.Builder a = new AlertDialog.Builder(view.getContext());
            a.setTitle(R.string.jump_to_path);
            final AppCompatEditText b = new AppCompatEditText(view.getContext());
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
                case R.id.browser:
                    startActivity(new Intent(this, BrowserActivity.class));
                    drawerLayout.closeDrawer(navigationView);
                    return true;
                case R.id.encoder:
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(navigationView);
                    return true;
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
                default:
                    drawerLayout.closeDrawer(navigationView);
                    return false;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this::refreshContent);
        handler.post(this);
        fileManagerInitialized = true;
    }

    final void showCompressDialog(Context context, Filex f) {
        showCompressDialog(context, new Filex[]{f});
    }

    final void showCompressDialog(Context context, Filex[] f) {
        final String[] supportedArchiveFormats = LUtils.toString(ArchiveType.supportedArchiveTypes());
        final String[] supportedPrimaryArchiveFormats = LUtils.toString(ArchiveType.primaryArchiveTypes());
        final String[] archiveFormatExtensions = LUtils.toString(ArchiveType.supportedArchiveTypes(), (byte) 1);
        if (f.length > 0) {
            final AlertDialog.Builder a = new AlertDialog.Builder(context);
            a.setTitle(R.string.compress);
            final View v = LayoutInflater.from(context).inflate(R.layout.compress_file_dialog, null);
            final AppCompatEditText b = v.findViewById(R.id.fileName);
            final AppCompatSpinner c = v.findViewById(R.id.archiveFormat);
            final AppCompatSpinner d = v.findViewById(R.id.compressionLevel);
            final AppCompatSpinner e = v.findViewById(R.id.compressionMethod);
            final AppCompatEditText g = v.findViewById(R.id.bufferSize);
            if (f.length > 1 && !f[0].isDirectory())
                c.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, supportedPrimaryArchiveFormats));
            else
                c.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, supportedArchiveFormats));
            d.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, CompressLevel.names()));
            e.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, CompressMethod.names()));
            g.setText(String.valueOf(IO.bufferSize));
            c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedItemIndex = (byte) position;
                    try {
                        if (f.length > 1)
                            b.setText(Sfx.nonnull(f[0].getParent()).getName() + archiveFormatExtensions[selectedItemIndex]);
                        else
                            b.setText(f[0].getName() + archiveFormatExtensions[selectedItemIndex]);
                    } catch (Exception l) {
                        // ignore
                    }
                    if (selectedItemIndex == 2) {
                        d.setSelection(3);
                        d.setEnabled(true);
                        e.setSelection(0);
                        e.setEnabled(false);
                    } else if (selectedItemIndex == 1) {
                        d.setSelection(0);
                        d.setEnabled(false);
                        e.setSelection(0);
                        e.setEnabled(false);
                    } else if (selectedItemIndex == 0) {
                        d.setSelection(3);
                        d.setEnabled(false);
                        e.setEnabled(true);
                    } else {
                        d.setSelection(3);
                        d.setEnabled(false);
                        e.setSelection(0);
                        e.setEnabled(false);
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
                    selectedItemIndex1 = (byte) position;
                }

                @Override
                public final void onNothingSelected(AdapterView<?> parent) {
                    parent.setSelection(0);
                }
            });
            e.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public final void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedItemIndex2 = (byte) position;
                }

                @Override
                public final void onNothingSelected(AdapterView<?> parent) {
                    parent.setSelection(0);
                }
            });
            a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                final String s = Sfx.nonnull(b.getText()).toString();
                if (!s.equals("")) {
                    this.dialog = ProgressDialog.show(this, getResources().getString(R.string.compress_file_notice), getResources().getString(R.string.please_wait));
                    this.dialog.show();
                    handler.post(() -> {
                        final String h = currentFile.toString() + File.separatorChar + s;
                        try {
                            IO.bufferSize = Short.parseShort(Sfx.nonnull(g.getText()).toString());
                            switch (selectedItemIndex) {
                                case 0: // 7zip
                                    new SevenZ(h).compress(Filex.toFiles(f));
                                    break;
                                case 1: // tar
                                    new Tar(h).compress(Filex.toFiles(f));
                                    break;
                                case 2: // zip
                                    new Zip(h).compress(Filex.toFiles(f), CompressLevel.init(selectedItemIndex1), CompressMethod.init(selectedItemIndex2));
                                    break;
                                case 3: // bzip2
                                    CompressUtils.compressBz2(f[0].getTarget(), h);
                                    break;
                                case 4: // gzip
                                    CompressUtils.compressGz(f[0].getTarget(), h);
                                    break;
                                case 5: // lz4
                                    CompressUtils.compressLz4(f[0].getTarget(), h);
                                    break;
                                case 6: // pack200
                                    CompressUtils.compressPack200(f[0].getTarget(), h);
                                    break;
                                case 7: // snappy
                                    CompressUtils.compressSnappy(f[0].getTarget(), h);
                                    break;
                                case 8: // xz
                                    CompressUtils.compressXz(f[0].getTarget(), h);
                                    break;
                                case 9: // zstd
                                    CompressUtils.compressZst(f[0].getTarget(), h);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unsupported type: " + Stringx.valueOf(selectedItemIndex));
                            }
                        } catch (Exception r) {
                            E.print(context, r);
                        }
                        handler.sendEmptyMessage(1);
                    });
                } else
                    Toast.makeText(context, R.string.file_name_empty_message, Toast.LENGTH_SHORT).show();
            });
            a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
            a.setView(v);
            a.show();
        } else
            Toast.makeText(context, R.string.nothing_selected_message, Toast.LENGTH_SHORT).show();
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
        final AlertDialog.Builder c = new AlertDialog.Builder(this);
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

    final void verifyDexFile() throws Exception {
        final Zip a = new Zip(getPackageManager().getApplicationInfo(packageName, 0).sourceDir);
        final Filex b = new Filex(Utils.getTempDir(), UUID.randomUUID().toString());
        a.extract(b.getPath());
        final ArrayList<Filex> c = new ArrayList<>();
        for (Filex f : Sfx.nonnull(b.list())) {
            final String s = f.getName();
            if (s.startsWith("classes") && s.endsWith(".dex")) c.add(f);
        }
        if (c.isEmpty()) throw new Exception("No dex files found");
        else {
            final TextWriter d = new TextWriter(new File(applicationExternalDataFolder, ".registered_classes"));
            final StringBuilder h = new StringBuilder();
            for (Filex f : c) {
                final DexFile g = new DexFile(f.getPath());
                for (String s : Collections.list(g.entries())) h.append(s).append("\n");
            }
            d.write(h.toString());
            d.flushAndClose();
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

    @SuppressWarnings("UnusedReturnValue")
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

            public ViewHolder(@NonNull final AppCompatImageView fileIcon, @NonNull final AppCompatTextView fileName, @NonNull final AppCompatTextView fileInfo) {
                this.fileIcon = fileIcon;
                this.fileName = fileName;
                this.fileInfo = fileInfo;
            }

            @NonNull
            @Override
            public final String toString() {
                return "ViewHolder{" +
                        "fileIcon=" + fileIcon +
                        ", fileName=" + fileName +
                        ", fileInfo=" + fileInfo +
                        '}';
            }
        }

        @Override
        public final String toString() {
            return "FileAdaptor{" +
                    "data=" + Arrays.toString(data) +
                    ", defaultDrawable=" + defaultDrawable +
                    ", selectedDrawable=" + selectedDrawable +
                    '}';
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
        private final long bytes;
        private final double kilobytes;
        private final double megabytes;
        private final double gigabytes;
        private final double terabytes;
        private final double petabytes;
        private final double exabytes;
        private final double zettabytes;
        private final double yottabytes;

        public FileSizeTransform(long l) {
            bytes = getFileSize(l);
            kilobytes = getFileSizeInKilobytes();
            megabytes = getFileSizeInMegaBytes();
            gigabytes = getFileSizeInGigabytes();
            terabytes = getFileSizeInTerabytes();
            petabytes = getFileSizeInPetabytes();
            exabytes = getFileSizeInExabytes();
            zettabytes = getFileSizeInZettabytes();
            yottabytes = getFileSizeInYottabytes();
        }

        public final String getFinalFileSize() {
            final DecimalFormat a = new DecimalFormat("0.00");
            if (bytes > 0) {
                if (kilobytes > 0) {
                    if (megabytes > 0) {
                        if (gigabytes > 0) {
                            if (terabytes > 0) {
                                if (petabytes > 0) {
                                    if (exabytes > 0) {
                                        if (zettabytes > 0) {
                                            if (yottabytes > 0) {
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

        private long getFileSize(long l) {
            return l;
        }

        private double getFileSizeInKilobytes() {
            if (bytes >= 1024) return (double) bytes / 1024;
            else return 0;
        }

        private double getFileSizeInMegaBytes() {
            if (kilobytes >= 1024) return kilobytes / 1024;
            else return 0;
        }

        private double getFileSizeInGigabytes() {
            if (megabytes >= 1024) return megabytes / 1024;
            else return 0;
        }

        private double getFileSizeInTerabytes() {
            if (gigabytes >= 1024) return gigabytes / 1024;
            else return 0;
        }

        private double getFileSizeInPetabytes() {
            if (terabytes >= 1024) return terabytes / 1024;
            else return 0;
        }

        private double getFileSizeInExabytes() {
            if (petabytes >= 1024) return petabytes / 1024;
            else return 0;
        }

        private double getFileSizeInZettabytes() {
            if (exabytes >= 1024) return exabytes / 1024;
            else return 0;
        }

        private double getFileSizeInYottabytes() {
            if (zettabytes >= 1024) return zettabytes / 1024;
            else return 0;
        }

        @NonNull
        @Override
        public final String toString() {
            return "FileSizeTransform{" +
                    "bytes=" + bytes +
                    ", kilobytes=" + kilobytes +
                    ", megabytes=" + megabytes +
                    ", gigabytes=" + gigabytes +
                    ", terabytes=" + terabytes +
                    ", petabytes=" + petabytes +
                    ", exabytes=" + exabytes +
                    ", zettabytes=" + zettabytes +
                    ", yottabytes=" + yottabytes +
                    '}';
        }
    }

    private static final class MemoryStatus {
        private final String path;
        private final long availableMemorySize;
        private final long totalMemorySize;

        public MemoryStatus(String s) {
            this.path = s;
            this.availableMemorySize = getAvailableMemorySize();
            this.totalMemorySize = getTotalMemorySize();
        }

        public final byte getMemoryUsagePercentage() {
            double a = (double) availableMemorySize / (double) totalMemorySize;
            return (byte) (a * 100);
        }

        public final String getFinalAvailableMemorySize() {
            return new FileSizeTransform(availableMemorySize).getFinalFileSize();
        }

        public final String getFinalTotalMemorySize() {
            return new FileSizeTransform(totalMemorySize).getFinalFileSize();
        }

        private long getAvailableMemorySize() {
            final StatFs stat = new StatFs(path);
            final long blockSize = stat.getBlockSize();
            final long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }

        private long getTotalMemorySize() {
            final StatFs stat = new StatFs(path);
            final long blockSize = stat.getBlockSize();
            final long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        }

        @NonNull
        @Override
        public final String toString() {
            return "MemoryStatus{" +
                    "path='" + path + '\'' +
                    ", availableMemorySize=" + availableMemorySize +
                    ", totalMemorySize=" + totalMemorySize +
                    '}';
        }
    }

    private static final class LUtils {
        public static String[] toString(ArchiveType[] types) {
            final ArrayList<String> a = new ArrayList<>();
            for (ArchiveType b : types) a.add(ArchiveType.getArchiveName(b));
            return a.toArray(new String[0]);
        }

        public static String[] toString(ArchiveType[] a, byte b) {
            if (b == 0) return toString(a);
            else {
                final ArrayList<String> c = new ArrayList<>();
                for (ArchiveType d : a) c.add(ArchiveType.getArchiveExtension(d));
                return c.toArray(new String[0]);
            }
        }
    }
}
