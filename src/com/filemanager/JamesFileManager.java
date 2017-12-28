package com.filemanager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.utils.Common;
import com.android.utils.LinuxFileCommand;
import com.android.utils.LinuxShell;
import com.android.utils.XDialog;
import com.filemanager.DDListView.DropListener;
import com.filemanager.DDListView.DropOutListener;
import com.filemanager.DDListView.StartDragListener;
import com.filemanager.FileAdapter.FileInfo;

/**
 * Main Activity: App的主要進入點。
 * Author: James.
 * Email: jamesshiakimo@yahoo.com.tw
 */
public class JamesFileManager extends Activity {
    public final static String tag = "FileDialog";
    public final static String PREFERENCE = "filedilaog";
    public final static String SDCARD_PATH = "/sdcard";
    public final static String BACKUPUP_DIR = SDCARD_PATH + "/";
    public final static String PRE_BACKUP_DIR = "backup";
    public final static String SAVE_SETTINGS_HIDE = "settingshide";
    public final static int MENU_SEARCH = 3;
    public final static int HANDLER_SHOW_COPY_WARNING_DIALOG = 11;
    public final static int HANDLER_SET_LISTVIEW_SELECTED = 12;
    public final static int HANDLER_REFRESH_LISTVIEW = 13;
    public final static int HANDLER_CLIP_BOARD_EMPTY = 14;
    public final static int HANDLER_COPY_FILE_ERROR = 15;
    public final static int HANDLER_FILE_CLICK = 16;
    public final static int HANDLER_LIST_ADPATER_CHANGED = 17;
    public final static int HANDLER_SEARCHBAR_HIDE = 18;
    public final static int HANDLER_SEARCHSTOP = 19;
    public final static int HANDLER_SET_SEARCHDIR = 20;
    public final static int HANDLER_SET_SEARCH_VISIBLE = 21;
    public final static int RESULT_GET_FILE_SIZE = 0;
    public final static String paramSNc = "nc";
    public final static int paramINc = 0x999;
    static final int MAX_PATH_TEMP = 10;
    static final int SINGLFILE = 91;
    static final int MULTFILE = 92;
    static final int MENU_ITEM_OPEN = 0;
    static final int MENU_ITEM_OPEN_IN_OTHER = 1;
    static final int MENU_ITEM_COPY = 2;
    static final int MENU_ITEM_CUT = 3;
    static final int MENU_ITEM_PASTE = 4;
    static final int MENU_ITEM_DELETE = 5;
    static final int MENU_ITEM_RENAME = 6;
    static final int MENU_ITEM_SELECT_ALL = 7;
    static final int MENU_ITEM_ZIP = 8;
    static final int MENU_ITEM_UNZIP = 9;
    //static final int MENU_ITEM_ADD_LIB = 10;	//Marked by James. 拿掉檔案庫。
    static final int MENU_ITEM_PROPERTIES = 10;    //Modified by James. 拿掉檔案庫。
    static final int MENU_ITEM_COPY_SELECTED = 12;
    static final int MENU_ITEM_CUT_SELECTED = 13;
    static final int MENU_ITEM_DELETE_SELECTED = 14;
    static final int MENU_ITEM_CREATE_DIRECTORY = 15;
    static final int MENU_ITEM_CREATE_FILE = 16;
    private final static String PRE_ISROOT = "root";
    private final static String PRE_VIEWSTYLE = "viewstyle";
    private final static String PRE_DRAGABLE = "dragable";
    private final static String PRE_HIDEFILE = "hidefile";
    private final static String PRE_HIDETAG = "hidetag";
    private final static String PRE_CUR_PATH = "currentPath";
    private final static String PRE_FILE_LIB = "filelib";
    private final static String PRE_LIB_CHILD = "libchild";
    /**
     * root操作可能會改變檔案許可權, 修改後如果root還沒完, 程式出錯退出,
     * 則可能之前的檔案的許可權變成修改後的, 所以用這個記錄有無修改, 無則為 "nc"
     */
    private final static String PRE_FILE_PERM = "fileperm";
    private final static String PRE_FILE_PATH = "filepath";
    private final static int MENU_CREATE_DIRECTORY = 0;
    private final static int MENU_CREATE_FILE = 1;
    private final static int MENU_PASTE = 2;
    private final static int MENU_SHOW_COPY_DIALOG = 4;
    private final static int MENU_APK_MANAGER = 5;
    private final static int MENU_SETTING = 6;
    private final static int MENU_SET_VIEW_STYLE = 7;
    private final static int MENU_FILE_LIB = 8;
    private final static int MENU_FINISH_ACTIVITY = 9;
    private final static int ROOT_COPY = 113;
    private final static int DOUBLE_CLICK_DURATION = 180;
    public int screen_height;
    public int screen_width;
    public String ok;
    public String cancel;
    //顯示檔案View
    DDListView fileViewList;
    DDGridView fileViewGrid;
    AbsListView fileView;
    FileAdapter fileAdapterList;
    FileGridAdapter fileAdapterGrid;
    FileAdapter fileAdapter;
    /**
     * 路徑欄
     */
    Gallery pathGallery;
    TextGalleryAdapter pathAdapter;
    LinuxFileCommand linux;
    Handler listViewHandler;
    // 設置
    SettingsView settingsView = null;
    ArrayList<String> historyString = null;
    TextView currentTag;
    /**
     * 是否多檔案操作
     */
    boolean multFile = false;
    float scale;
    int copyButton = -1;
    int copySelection = 2;
    boolean selectionAll = false;
    int pre_ViewStyle = FileAdapter.STYLE_GRID;
    boolean pre_Dragable = true;
    boolean pre_HideFile = false;
    boolean pre_HideTag = false;
    String pre_BackupDir = BACKUPUP_DIR;
    ConditionVariable copyDialogLock;
    FileItemClickListener listListener;
    /**
     * 點擊時的Tag的檔案資料
     */
    FileInfo clickInfo;
    String dragFromFile, dragToPath;
    SearchInputDialog.onSearchListener onSL = new SearchInputDialog.onSearchListener() {
        @Override
        public void onSearch(String expr, boolean allMatch, boolean caseSense) {

            if (expr.contains("/"))
                Toast.makeText(JamesFileManager.this, getString(R.string.key_word_can_not_contain),
                        Toast.LENGTH_SHORT).show();
            else
                new Thread(new SearchFileThread(expr, allMatch, caseSense)).start();
        }
    };
    private Animation menuShowAnimation = null;
    private Animation menuHideAnimation = null;
    private boolean settingsHide = true;
    private FileData currentData = null;
    private FileData dragData = null;
    /**
     * 每個標籤對應的檔案資料
     */
    private ArrayList<FileData> datas;
    private boolean dragging;
    /**
     * preferences
     */
    private boolean pre_IsRoot = false;
    /**
     * 檔案庫資料結構為
     * parentList 放 檔案庫名.
     * childList 放對應檔案庫裡的檔案.
     * 庫名在parentList的位置與其檔案在childList的位置一樣
     */
    private ArrayList<String> parentList = new ArrayList<String>();
    private ArrayList<ArrayList<String>> childList = new ArrayList<ArrayList<String>>();
    private FileLibDialog fileLibDialog;// = new FileLibDialog(this, parentList, childList);
    /**
     * opt menu
     */
    private ImageView optUp;
    private ImageView optTag;
    private ImageView optRefresh;
    //private ImageView optMultfile;	//Marked by James.
    private ImageView optMenu;
    private ImageView addTagButton;
    //Search view
    private LinearLayout searchLayout;
    private TextView searchText;
    private ImageButton searchBtn;
    private ProgressBar searchBar;
    private TableRow tagRow;
    private RelativeLayout tagLayout;
    private LinearLayout appMenu;
    private SharedPreferences pre;
    private Button mountBtn;
    private Mounts mounts = new Mounts();
    OnClickListener toolbarListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.optmenu:
                    if (appMenu.getVisibility() == View.VISIBLE) {
                        hideAppMenu();
                    } else {
                        showAppMenu();
                    }
                    break;
				/*case R.id.optmultfile:	//Marked by James.
					multOrSingle(!multFile);
					break;*/
                case R.id.optrefresh:
                    clearFileSlected();
                    refreshPath(currentPath(), 1);
                    break;
                case R.id.opttag:


                    if (tagLayout.getVisibility() == View.VISIBLE) {
                        tagLayout.setVisibility(View.GONE);
                        pre_HideTag = true;
                        optTag.setImageDrawable(getResources()
                                .getDrawable(R.drawable.tag_hide));
                    } else {
                        tagLayout.setVisibility(View.VISIBLE);
                        pre_HideTag = false;
                        optTag.setImageDrawable(getResources()
                                .getDrawable(R.drawable.tag_show));
                    }
                    break;
                case R.id.optup:
                    File file = new File(currentPath());
                    String pa = file.getParent();
                    if (pa == null)
                        pa = "/";

                    if (pre_IsRoot == false && "/".equals(pa)) { //Modified by James. 修正某些手機沒有root權限到根目錄，進入app畫面後，直接按[上一頁]，會回傳null
                        break;
                    } else {
                        refreshPath(pa, 1);
                    }

                    break;
                default:
                    break;
            }
        }
    };
    private int clickTime = 0;
    /**
     * 檔案item 長按 listener
     */
    private OnItemLongClickListener fileViewLongClickListener
            = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {

            String file = currentFileInfo().get(position).path();

            listListener.setFile(file);
            listListener.setPosition(position);
            listListener.setName(currentFileInfo().get(position).name());
            ItemMenuDialog dialog = new ItemMenuDialog(JamesFileManager.this);
            dialog.selectedFile(file);
            dialog.show();
            return true;
        }
    };
    /**
     * 檔案 item 點擊 listener
     */
    private OnItemClickListener fileViewClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            clickTime++;
            if (clickTime >= 2)
                return;
            clickInfo = currentFileInfo().get(position);
            if (pre_ViewStyle == FileAdapter.STYLE_GRID) {
                fileViewGrid.setDragable(true);
                int time = 0;
                if (pre_Dragable)
                    time = DOUBLE_CLICK_DURATION;
                new Timer().schedule(new FileClickTimerTask(position), time);
            } else {
                new Timer().schedule(new FileClickTimerTask(position), 0);
            }
        }
    };
    private boolean sensorDoing = false;
    private Sensor accSensor;
    private SensorManager sensorMgr;
    private float sensorLastX = 0;
    private long sensorLastTime = 0;
    private SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            long currentTime = event.timestamp;
            float x = event.values[SensorManager.DATA_X];
            if ((currentTime > (sensorLastTime + 250000000))) {

                if (Math.abs(x) < 3) {
                    sensorLastTime = event.timestamp;
                    sensorLastX = x;
                }
                if (Math.abs(sensorLastX) > 3) {
                    return;
                }
                if (Math.abs(x) > 3.7) {
                    if (x > 0) {
                        nextTag(KeyEvent.KEYCODE_DPAD_LEFT);
                    } else {
                        nextTag(KeyEvent.KEYCODE_DPAD_RIGHT);
                    }
                    sensorLastX = x;
                }
            }
            //event.
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    /**
     * 檔案拖拉,放手時調用
     */
    DropListener dropListener = new DropListener() {

        @Override
        public void drop(int from, int to) {

            boolean sameItem = false;
            if (sensorDoing) {
                sensorDoing = false;
                sensorMgr.unregisterListener(sensorListener);
            }

            dragging = false;
            dragFromFile = dragData.fileInfos.get(from).path();
            if (to == -1) {
                dragToPath = currentPath();
            } else {
                dragToPath = currentFileInfo().get(to).path();
            }
            //是否為同一檔案
            sameItem = dragToPath.equals(dragFromFile);
            File file = new File(dragToPath);
            if (file.isDirectory() && !sameItem) {

            } else {
                if (!sameItem)
                    return;
                //放手時,指向的位置的檔案為拖拉的物件,則表示刪除檔案
                listListener.setName(Common.getPathName(dragToPath));
                listListener.setFile(dragToPath);
                listListener.setPosition(from);
                listListener.onClick(null, MENU_ITEM_DELETE);
                return;
            }
            AlertDialog.OnClickListener li = new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ArrayList<String> f = null;
                    switch (which) {
                        case AlertDialog.BUTTON_POSITIVE:
                            //複製
                            if (multFile && (f = getDragFiles(dragData)) != null) {
                                ;
                            } else {
                                f = new ArrayList<String>();
                                f.add(dragFromFile);
                            }
                            listListener.startCopyService(f,
                                    dragToPath,
                                    false);
                            break;
                        case AlertDialog.BUTTON_NEUTRAL:
                            //剪切
                            if (multFile && (f = getDragFiles(dragData)) != null) {
                                ;
                            } else {
                                f = new ArrayList<String>();
                                f.add(dragFromFile);
                            }
                            listListener.startCopyService(f,
                                    dragToPath,
                                    true);
                            break;
                        case AlertDialog.BUTTON_NEGATIVE:
                            break;
                    }
                    dragData = null;
                }

            };
            AlertDialog.Builder b = new AlertDialog.Builder(JamesFileManager.this);
            b.setTitle("拷貝粘貼").setMessage("從: " + dragFromFile + "\n到: " + dragToPath);
            b.setPositiveButton("拷貝", li).setNeutralButton("剪切", li)
                    .setCancelable(false)
                    .setNegativeButton(cancel, li).create().show();
        }
    };
    private StartDragListener stargDragListener =
            new StartDragListener() {

                @Override
                public void startDrag(int from) {

                    dragging = true;
                    dragData = currentData;
                    if (tagRow.getChildCount() != 1) {
                        sensorDoing = true;
                        sensorMgr.registerListener(sensorListener,
                                accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                        sensorLastX = 0;
                    }
                }
            };
    /**
     * Add tag
     */
    private OnClickListener tagOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            TextView tv = (TextView) v;
            if (tv == null || tv == currentTag)
                return;
            FileData data = (FileData) tv.getTag();
            currentTag.setBackgroundResource(R.drawable.tag2);
            currentTag = tv;
            currentTag.setBackgroundResource(R.drawable.tag1);
            setCurrentData(data, true);
            if (currentData.searchingTag) {
                searchLayout.setVisibility(View.VISIBLE);
            } else if (searchLayout.getVisibility() == View.VISIBLE) {
                searchLayout.setVisibility(View.INVISIBLE);
            }
            showOrHideMount();
        }
    };
    private OnLongClickListener tagLongClickListener =
            new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    if (tagRow.getChildCount() > 1) {
                        TextView tv = (TextView) v;
                        if (tv == null)
                            return true;
                        if (tv == currentTag) {
                            nextTag(KeyEvent.KEYCODE_DPAD_RIGHT);
                        }
                        datas.remove(tv.getTag());
                        tagRow.removeView(v);
                    }
                    return true;
                }
            };
    private ImageButton viewStyleButton;
    private TextView viewStyleTextView;
    private CharSequence searchDir = "";
    private boolean searching = false;

    /**
     * 初始化App畫面
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        screen_width = getResources().getDisplayMetrics().widthPixels;
        screen_height = getResources().getDisplayMetrics().heightPixels;
        ok = getString(R.string.ok);
        cancel = getString(R.string.cancel);
        pre = getSharedPreferences(PREFERENCE, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
        newObject();
        fileLibDialog = new FileLibDialog(this, parentList, childList);

        findView();
        setupToolbar();

        initFileAdapter();

        initFileViewList();
        initFileViewGrid();
        initViewHandler();
        initAppMenu();
        initMountBtn();

        loadPreferences();
        initFilePathGallery();
        initTag();
        initSearchStop();

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        loadPerm("", 0, true);
    }

    protected void onStop() {
        super.onStop();
        storePreferences();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        if (sensorDoing)
            sensorMgr.unregisterListener(sensorListener);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PRE_CUR_PATH, currentPath());
        outState.putBoolean(SAVE_SETTINGS_HIDE, settingsHide);

        if (!settingsHide)
            settingsView.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // 如果保存狀態前是打開 設置視窗, 則還原之前狀態
            if (!(settingsHide = savedInstanceState.getBoolean(SAVE_SETTINGS_HIDE, true))) {
                if (settingsView == null) {
                    settingsView = new SettingsView(this);
                }
                settingsView.show(savedInstanceState);
            }


            currentData.path = savedInstanceState.getString(PRE_CUR_PATH);
            if (currentData.path == null)
                currentData.path = SDCARD_PATH;
            refreshPath(currentPath(), 1);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void loadPerm(String file, int perm, boolean getFromPre) {
        if (getFromPre) {
            perm = pre.getInt(PRE_FILE_PERM, paramINc);
            if (perm == paramINc)
                return;
            file = pre.getString(PRE_FILE_PATH, paramSNc);
            if (file.equals(paramSNc))
                return;
        }
        int ret = 0;
        if (file == null) {
            return;
        }
        DataOutputStream out = null;
        BufferedReader br = null;
        Process p = null;
        try {
            p = linux.shell.exec("su\n");
            out = new DataOutputStream(p.getOutputStream());
            String cmd = String.format("chmod %x %s\nexit\n", perm, file);

            out.writeBytes(cmd);
            out.flush();
            ret = p.waitFor();
            if (ret < 0) {
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                Log.d(tag, "chmod error");
                if (br.ready()) {
                    Toast.makeText(this, br.readLine(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "chmod 出錯", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 載入基本設置
     */
    private void loadPreferences() {
        int viewStyle = pre_ViewStyle;
        pre_HideTag = pre.getBoolean(PRE_HIDETAG, false);
        pre_Dragable = pre.getBoolean(PRE_DRAGABLE, true);
        pre_HideFile = pre.getBoolean(PRE_HIDEFILE, false);
        pre_IsRoot = pre.getBoolean(PRE_ISROOT, false);
        pre_ViewStyle = pre.getInt(PRE_VIEWSTYLE, FileAdapter.STYLE_GRID);
        pre_BackupDir = pre.getString(PRE_BACKUP_DIR, BACKUPUP_DIR);
        if (pre_HideTag) {
            tagLayout.setVisibility(View.GONE);
            optTag.setImageDrawable(getResources()
                    .getDrawable(R.drawable.tag_hide));

        } else {
            tagLayout.setVisibility(View.VISIBLE);
            optTag.setImageDrawable(getResources()
                    .getDrawable(R.drawable.tag_show));
        }
        if (pre_ViewStyle == FileAdapter.STYLE_GRID)
            fileView = fileViewGrid;
        else
            fileView = fileViewList;
        if (viewStyle != pre_ViewStyle)
            setFileViewStyle(pre_ViewStyle);

        String libs = pre.getString(PRE_FILE_LIB, null);
        if (libs != null && !libs.equals("[]")) {
            libs = libs.substring(1, libs.length() - 1);
            String[] s = libs.split(", ");
            String c = null;
            ArrayList<String> child;
            for (int i = 0; i < s.length; i++) {
                parentList.add(s[i]);
                c = pre.getString(PRE_LIB_CHILD + i, null);
                if (c == null)
                    continue;
                child = new ArrayList<String>();
                string2ArrayList(c, child);
                childList.add(child);
            }
        }
    }

    /**
     * 保存設置
     */
    private void storePreferences() {
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean(PRE_DRAGABLE, pre_Dragable);
        editor.putBoolean(PRE_HIDEFILE, pre_HideFile);
        editor.putBoolean(PRE_HIDETAG, pre_HideTag);
        editor.putBoolean(PRE_ISROOT, pre_IsRoot);
        editor.putInt(PRE_VIEWSTYLE, pre_ViewStyle);
        editor.putString(PRE_BACKUP_DIR, pre_BackupDir);
        editor.putString(PRE_FILE_LIB, parentList.toString());
        int size = parentList.size();
        ArrayList<String> child;
        for (int i = 0; i < size; i++) {
            child = childList.get(i);
            editor.putString(PRE_LIB_CHILD + i, child.toString());
        }

        editor.commit();
    }

    public void storePerm(String file, int perm) {
        SharedPreferences.Editor editor = pre.edit();
        editor.putInt(PRE_FILE_PERM, perm);
        editor.putString(PRE_FILE_PATH, file);
        editor.commit();
    }

    /**
     * 從ArrayList的toString() 得到的字串轉加到ArrayList
     */
    private final void string2ArrayList(String src, ArrayList<String> dst) {
        src = src.substring(1, src.length() - 1);
        String[] s = src.split(", ");
        if (s.length == 1 && s[0].equals(""))
            return;
        for (int i = 0; i < s.length; i++) {
            dst.add(s[i]);
        }
    }

    public boolean isHideFile() {
        return pre_HideFile;
    }

    public void setHideFile(boolean b) {
        if (b == pre_HideFile) return;
        pre_HideFile = b;
        if (pre_HideFile == false) {
            refreshPath(currentPath(), 0);
        } else {
            ArrayList<FileInfo> infos = currentFileInfo();
            for (int i = 0; i < infos.size(); ) {
                if (infos.get(i).name().startsWith("."))
                    infos.remove(i);
                else
                    i++;
            }
            fileAdapter.notifyDataSetChanged();
        }
    }

    ;

    public boolean isRoot() {
        return pre_IsRoot;
    }

    public boolean isMultFile() {
        return multFile;
    }

    public float getDensity() {
        return scale;
    }

    public ArrayList<FileInfo> currentFileInfo() {
        return currentData.fileInfos;
    }

    public ArrayList<Integer> selectedItem() {
        return currentData.selectedId;
    }

    /**
     * 返回當前tag的目錄路徑
     */
    public String currentPath() {
        return currentData.path;
    }

    public void clearFileSlected() {
        currentData.selectedId.clear();
    }

    public int getCurrentSelectedCount() {
        return currentData.selectedId.size();
    }

    /**
     *
     * */
    private void initViewHandler() {
        listViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case JamesFileManager.HANDLER_SHOW_COPY_WARNING_DIALOG:
                        break;
                    /**
                     * 更新當前選擇項,如果arg1 == FileDialog.REFRESH_LISTVIEW
                     * 則先刷新當前列表.
                     * */
                    case JamesFileManager.HANDLER_SET_LISTVIEW_SELECTED:
                        int p = fileView.getFirstVisiblePosition();
                        if (msg.arg2 == JamesFileManager.HANDLER_REFRESH_LISTVIEW)
                            refreshPath(currentPath(), 0);
                        fileView.setSelection(p);
                        break;
                    /**
                     * 刷新當前列表,如果arg1 == FileDialog.SET_LISTVIEW_SELECTED
                     * 則把當前選擇到arg2處
                     * */
                    case JamesFileManager.HANDLER_REFRESH_LISTVIEW:
                        p = fileView.getFirstVisiblePosition();
                        refreshPath(currentPath(), 0);
                        if (msg.arg1 == JamesFileManager.HANDLER_SET_LISTVIEW_SELECTED)
                            fileView.setSelection(p);
                        break;
                    case JamesFileManager.HANDLER_CLIP_BOARD_EMPTY:
                        Toast.makeText(JamesFileManager.this, getString(R.string.clipboard_is_empty),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case JamesFileManager.HANDLER_COPY_FILE_ERROR:
                        Toast.makeText(JamesFileManager.this, listListener.bTmp, Toast.LENGTH_LONG).show();
                        listListener.bTmp = "";
                        break;

                    case JamesFileManager.HANDLER_FILE_CLICK:
                        itemClick(msg.arg1);
                        break;

                    case HANDLER_LIST_ADPATER_CHANGED:
                        fileAdapter.notifyDataSetChanged();
                        break;
                    case HANDLER_SEARCHBAR_HIDE:
                        searchBar.setVisibility(View.GONE);
                        break;
                    case HANDLER_SEARCHSTOP:
                        searchBar.setVisibility(View.GONE);
                        searchBtn.setVisibility(View.GONE);
                        break;
                    case HANDLER_SET_SEARCHDIR:
                        searchText.setText(searchDir);
                        break;
                    case HANDLER_SET_SEARCH_VISIBLE:
                        searchBar.setVisibility(View.VISIBLE);
                        searchLayout.setVisibility(View.VISIBLE);
                        break;
                }

            }
        };
    }

    private void itemClick(int position) {
        String path = clickInfo.path();
        if (multFile) {
            //fileInfos.get(position).invertSelected();
            if (currentData.selectedId.contains(position))
                currentData.selectedId.remove(new Integer(position));
            else
                currentData.selectedId.add(new Integer(position));
            fileAdapter.notifyDataSetChanged();
            return;
        }

        // 點擊目錄
        if (clickInfo.directory) {
            if (currentData.searchingTag) {
                addTag(new FileData(new ArrayList<FileInfo>(),
                        null, SDCARD_PATH));
                return;
            }
            int pa = pathAdapter.getAbsolutePath().indexOf(path);
            refreshPath(path, pa);
            if (pa == 0) {
                pathAdapter.setCurrentPosition(path.split("/").length - 1);
                pathAdapter.notifyDataSetChanged();
            }
        } else {
            // 點擊檔案
            listListener.doOpenFile(path);
        }
    }

    private void initFileAdapter() {
        fileAdapterList = new FileAdapter(this, currentData, FileAdapter.STYLE_LIST);
        fileAdapterGrid = new FileGridAdapter(this, currentData, FileAdapter.STYLE_GRID);
        if (pre_ViewStyle == FileAdapter.STYLE_LIST) {
            fileAdapter = fileAdapterList;
        } else {
            fileAdapter = fileAdapterGrid;
        }
        listListener = new FileItemClickListener(this);
    }

    private void setFileViewStyle(int style) {
        pre_ViewStyle = style;
        if (pre_ViewStyle == FileAdapter.STYLE_LIST) {
            fileAdapter = fileAdapterList;
            fileViewList.setVisibility(View.VISIBLE);
            fileViewGrid.setVisibility(View.GONE);
            fileView = fileViewList;
            viewStyleTextView.setText(getString(R.string.icon));
            viewStyleButton.setBackgroundResource(R.drawable.multicon);
        } else {
            fileAdapter = fileAdapterGrid;
            fileViewGrid.setVisibility(View.VISIBLE);
            fileViewList.setVisibility(View.GONE);
            fileView = fileViewGrid;
            viewStyleTextView.setText(getString(R.string.list));
            viewStyleButton.setBackgroundResource(R.drawable.multlist);
        }
        fileAdapter.setData(currentData);
        fileAdapter.notifyDataSetChanged();
    }

    private void initFileViewList() {
        /*files lists view*/
        fileViewList.setItemsCanFocus(true);
        fileViewList.setAdapter(fileAdapterList);
        fileViewList.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_INSET);
        fileViewList.setHeaderDividersEnabled(true);
        fileViewList.setOnItemLongClickListener(fileViewLongClickListener);
        fileViewList.setOnItemClickListener(fileViewClickListener);
        fileViewList.dragMaxX = 50;
        fileViewList.dragMinX = 0;
        fileViewList.setDropListener(dropListener);
        fileViewList.setStartDragListener(stargDragListener);
        fileViewList.setDropOutListener(new DropOutListener() {

            @Override
            public void dropOut(int from, int x, int y) {
                dropListener.drop(from, -1);
            }
        });

    }

    public void showAddFileLibDialog() {
        fileLibDialog.doWhat = FileLibDialog.FILE_LIB_ADD;
        fileLibDialog.setPath(listListener.getFile());
        fileLibDialog.show();
    }

    public void clearClickTime() {
        clickTime = 0;
        fileViewGrid.setDragable(false);
    }

    private void initFileViewGrid() {
        fileViewGrid.setGravity(Gravity.FILL);
        fileViewGrid.setAdapter(fileAdapterGrid);
        fileViewGrid.setOnItemClickListener(fileViewClickListener);
        fileViewGrid.setOnItemLongClickListener(fileViewLongClickListener);
        fileViewGrid.setStartDragListener(stargDragListener);
        fileViewGrid.setDropListener(dropListener);
        fileViewGrid.setDropOutListener(new DropOutListener() {
            @Override
            public void dropOut(int from, int x, int y) {
                if (y > 0)
                    dropListener.drop(from, -1);
            }
        });
    }

    private ArrayList<String> getDragFiles(FileData d) {
        ArrayList<String> files = null;
        if (d == null) {
            return files;
        }
        ArrayList<FileInfo> infos = d.fileInfos;
        ArrayList<Integer> sels = d.selectedId;
        if (sels != null && infos != null && !sels.isEmpty()) {
            int size = sels.size();
            files = new ArrayList<String>();
            for (int i = 0; i < size; i++)
                files.add(infos.get(sels.get(i)).path);
        }
        return files;
    }

    /**
     * @Override public boolean onTouchEvent(MotionEvent event){
     * Log.d(tag, "a ont");
     * return  onTouchEvent(event);
     * }
     * /
     **/
    private void initFilePathGallery() {
        pathAdapter = new TextGalleryAdapter(this, "");
        pathGallery.setAdapter(pathAdapter);
        pathGallery.setSpacing(2);
        pathGallery.setSelection(pathAdapter.getCount() - 1);
        pathGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                pathAdapter.setCurrentPosition(position);
                pathAdapter.notifyDataSetChanged();
                String path = pathAdapter.getPath(position);
                if (currentPath().equals(path))
                    return;
                refreshPath(path, 0);
            }
        });

        pathGallery.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final EditText pEdit = new EditText(JamesFileManager.this);
                pEdit.setText(currentPath());
                XDialog.createInputDialog(JamesFileManager.this, null, pEdit)
                        .setTitle(getString(R.string.path))
                        .setPositiveButton(ok, new AlertDialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                File f = new File(pEdit.getText().toString());
                                if (!f.exists()) {
                                    Toast.makeText(JamesFileManager.this,
                                            getString(R.string.the_input_path_error_or_not_exsit),
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                                refreshPath(pEdit.getText().toString(), 1);
                            }
                        }).setNegativeButton(cancel, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // do nothing
                    }
                }).create().show();
                return true;
            }


        });

        pathGallery.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    pathAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
    }

    /**
     * 標籤欄
     */
    private void initTag() {
        addTag(currentData);
        addTagButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                addTag(new FileData(new ArrayList<FileInfo>(),
                        null, SDCARD_PATH)
                );
            }
        });
    }

    private void addTag(FileData data) {
        TextView tv = new TextView(JamesFileManager.this);
        tv.setTextColor(0xff000000);
        tv.setSingleLine();
        tv.setGravity(Gravity.CENTER);
        tv.setEllipsize(TruncateAt.MARQUEE);
        tv.setPadding(5, 5, 5, 5);
        tv.setBackgroundResource(R.drawable.tag1);

        tv.setOnClickListener(tagOnClickListener);
        tv.setOnLongClickListener(tagLongClickListener);


        if (currentTag != null)
            currentTag.setBackgroundResource(R.drawable.tag2);
        currentTag = tv;

        currentTag.setText(SDCARD_PATH);
        currentTag.setTag(data);
        setCurrentData(data, false);

        refreshPath(data.path, 1);

        datas.add(data);
        tagRow.addView(tv);

        if (searchLayout.getVisibility() == View.VISIBLE) {
            searchLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * w --> KeyEvent.KEYCODE_DPAD_RIGHT
     * or
     * KeyEvent.KEYCODE_DPAD_LEFT
     */
    private void nextTag(int w) {
        int count = tagRow.getChildCount();
        if (count == 1)
            return;
        int index = datas.indexOf(currentData);
        if (w == KeyEvent.KEYCODE_DPAD_RIGHT) {
            index++;
            if (index >= count)
                index = 0;
        } else if (w == KeyEvent.KEYCODE_DPAD_LEFT) {
            index--;
            if (index < 0)
                index = count - 1;
        }

        currentTag.setBackgroundResource(R.drawable.tag2);
        currentTag = (TextView) tagRow.getChildAt(index);
        currentTag.setBackgroundResource(R.drawable.tag1);

        setCurrentData(datas.get(index), true);
        currentData = datas.get(index);
        if (pre_ViewStyle == FileAdapter.STYLE_GRID) {
            fileViewGrid.clearDragBG();
        } else {
            fileViewList.clearDragBG();
        }

        if (currentData.searchingTag) {
            searchLayout.setVisibility(View.VISIBLE);
        } else if (searchLayout.getVisibility() == View.VISIBLE) {
            searchLayout.setVisibility(View.INVISIBLE);
        }
        showOrHideMount();
    }

    /**
     * 是否顯示mount 按鍵
     */
    private void showOrHideMount() {
        if (currentPath().equals("/")) {
            showMount(0);
        } else if (currentPath().contains(mounts.fs[1])) {
            showMount(1);
        } else if (mountBtn.getVisibility() == View.VISIBLE) {
            hideMount();
        }
    }

    /**
     * 設置當前檔案資料,更新ListView 和 路徑欄
     */
    private void setCurrentData(FileData d, boolean refreshView) {
        fileAdapter.setData(d);
        currentData = d;
        if (refreshView)
            fileAdapter.notifyDataSetChanged();
        refreshTextPath();
    }

    private void setupToolbar() {
        optUp.setOnClickListener(toolbarListener);
        optRefresh.setOnClickListener(toolbarListener);
        //optMultfile.setOnClickListener(toolbarListener);	//Marked by James.
        optMenu.setOnClickListener(toolbarListener);
        optTag.setOnClickListener(toolbarListener);
    }

    private void showHistory() {

        int l = historyString.size();
        String[] str = historyString.toArray(new String[l]);
        CharSequence[] ch = new CharSequence[l];
        for (int i = 0; i < l; i++) {
            if (str[i].length() == 1) {
                ch[i] = "/";
            } else
                ch[i] = str[i].substring(str[i].lastIndexOf("/")
                        + 1, str[i].length());
        }

        new AlertDialog.Builder(JamesFileManager.this)
                .setTitle(getString(R.string.history))
                .setItems(ch, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 同時更新路徑欄
                        String s = (String) historyString.get(which);
                        if (currentPath().equals(s))
                            return;
                        int p = pathAdapter.getAbsolutePath().indexOf(s);
                        refreshPath(s, p);
                        if (p == 0) {
                            pathAdapter.setCurrentPosition(s.split("/").length - 1);
                            pathAdapter.notifyDataSetChanged();
                        }
                    }

                }).setNegativeButton(cancel, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }

        }).create().show();
    }

    public void callMenu() {
        this.openOptionsMenu();
    }

    public void selectedAllEle() {
        //multOrSingle(true);	//Marked by James.
    }

    //public boolean outBound = false;

    /**
     * //Marked by James.
     * true for multiple
     * false for single
     */
	/*private void multOrSingle(boolean ms){

		if (ms){
			optMultfile.setImageDrawable(getResources()
					.getDrawable(R.drawable.singlefile));
			multFile = true;
		} else {
			optMultfile.setImageDrawable(getResources()
					.getDrawable(R.drawable.multfile));
			clearFileSlected();
			fileAdapter.notifyDataSetChanged();
			multFile = false;
		}
	}*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.OnClickListener lsn = new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == AlertDialog.BUTTON_NEGATIVE)
                        return;
                    if (listListener.copying()) {
                        dealCopyingOnExit();
                    } else {
                        JamesFileManager.this.finish();
                    }
                }
            };
            new AlertDialog.Builder(this).setMessage(getString(R.string.sure_exit))
                    .setPositiveButton("\t確定\t", lsn).setNegativeButton("\t取消 \t", lsn)
                    .create().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listListener.stopCopyService();
    }

    private void dealCopyingOnExit() {

        AlertDialog.OnClickListener lsn = new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == AlertDialog.BUTTON_NEGATIVE)
                    return;
                listListener.cancelCopy();
                JamesFileManager.this.finish();
            }
        };

        new AlertDialog.Builder(JamesFileManager.this).setMessage(
                "有複製任務沒完成,要取消複製嗎?")
                .setPositiveButton(ok, lsn)
                .setNegativeButton(cancel, lsn)
                .create().show();
    }

    /***/
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (pre_ViewStyle == FileAdapter.STYLE_GRID) {
            int act = event.getAction();
            if ((act == MotionEvent.ACTION_CANCEL
                    || act == MotionEvent.ACTION_UP) && fileViewGrid.isOutBound()) {
                fileViewGrid.reback();
            }
        }
        if (appMenu.getVisibility() == View.VISIBLE) {
            int y = (int) event.getRawY();
            if (y < screen_height - appMenu.getHeight()) {
                hideAppMenu();
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 顯示功能表列, 重新實現的Option menu.
     */
    private void showAppMenu() {
        if (menuShowAnimation == null) {
            menuShowAnimation = AnimationUtils
                    .loadAnimation(this, R.anim.menuhide);
        }
        appMenu.startAnimation(menuShowAnimation);
        appMenu.setVisibility(View.VISIBLE);
    }

    /**
     * 隱藏功能表列, 重新實現的Option menu.
     */
    private void hideAppMenu() {
        appMenu.setVisibility(View.INVISIBLE);
        if (menuHideAnimation == null)
            menuHideAnimation = AnimationUtils
                    .loadAnimation(this, R.anim.menushow);
        appMenu.startAnimation(menuHideAnimation);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int act = event.getAction();
        int code = event.getKeyCode();

        // 拖拉檔案時, 左右鍵為標籤轉移
        if (dragging) {
            if (act == KeyEvent.ACTION_DOWN) {
                switch (code) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_MENU:
                        nextTag(KeyEvent.KEYCODE_DPAD_LEFT);
                        return true;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_BACK:
                        nextTag(KeyEvent.KEYCODE_DPAD_RIGHT);
                        return true;
                    default:
                        break;
                }
            }
        }
        // app menu like option menu
        if (code == KeyEvent.KEYCODE_MENU) {
            if (act == KeyEvent.ACTION_DOWN) {
                if (appMenu.getVisibility() == View.VISIBLE) {
                    hideAppMenu();
                } else {
                    showAppMenu();
                }
                return true;
            }
        } else if (code == KeyEvent.KEYCODE_BACK) {
            if (appMenu.getVisibility() == View.VISIBLE) {
                hideAppMenu();
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void initMountBtn() {
        refreshMountStatus();
        mountBtn.setClickable(true);
        mountBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mountFsPerm(mounts.perm[mounts.index].equals(Mounts.RW) ?
                        Mounts.RO : Mounts.RW);
            }
        });
    }

    private int mountFsPerm(String perm) {
        if (!isRoot()) {
            Toast.makeText(this, "需要root許可權", Toast.LENGTH_SHORT).show();
            return 0;
        }
        int ret = 0;
        DataOutputStream out = null;
        BufferedReader br = null;
        Process p = null;
        try {
            p = linux.shell.exec("su\n");
            out = new DataOutputStream(p.getOutputStream());
            String cmd = "mount -o " + perm + ",remount "
                    + mounts.rawDev[mounts.index] + " "
                    + mounts.fs[mounts.index] + "\nexit\n";
            out.writeBytes(cmd);
            out.flush();
            ret = p.waitFor();
            if (ret < 0) {
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                Log.d(tag, "remount error");
                if (br.ready()) {
                    Toast.makeText(this, br.readLine(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "重新裝載remount出錯", Toast.LENGTH_SHORT).show();
                }
                return ret;
            }
            mounts.perm[mounts.index] = perm;
            showMount(mounts.index);
        } catch (IOException e) {

            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 刷新mount的屬性
     */
    private void refreshMountStatus() {
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        BufferedReader br = null;
        try {
            p = rt.exec("mount");
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String content;
            String[] lines;
            while ((content = br.readLine()) != null) {
                lines = content.split(" +");
                for (int i = 0; i < mounts.fs.length; i++) {
                    if (mounts.fs[i].equals(lines[1])) {
                        mounts.perm[i] = lines[3];
                        mounts.rawDev[i] = lines[0];
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 刷新目前的目錄的內容,
     *
     * @param path    要顯示的目錄路徑
     * @param gallery 當為0時不刷新顯示當前路徑的gallery, 其它則刷新.
     */
    public final void refreshPath(String path, int gallery) {
        if (currentData.searchingTag)
            return;
        if (!historyString.contains(path))
            historyString.add(path);
        if (historyString.size() > MAX_PATH_TEMP) {
            historyString.remove(0);
        }
        clearFileSlected();
        currentData.path = path;
        currentTag.setText(Common.getPathName(path));

        fileAdapter.setCurrenPath(path);

        findFileInfo(path, currentFileInfo());
        fileAdapter.notifyDataSetChanged();
        fileView.setSelection(0);
        if (gallery != 0) {
            refreshTextPath();
        }
        for (int i = 0; i < mounts.fs.length; i++) {
            if (mounts.fs[i].equals(path)
                    || ((i != 0) && (path.contains(mounts.fs[i])))) {
                showMount(i);
                mounts.index = i;
                return;
            }
        }
        if (mountBtn.getVisibility() == View.VISIBLE)
            hideMount();
    }

    public String getCurrentDirPerm() {
        if (mounts.index == -1)
            return null;
        return mounts.perm[mounts.index];
    }

    private void showMount(int i) {
        if (mountBtn.getVisibility() != View.VISIBLE)
            mountBtn.setVisibility(View.VISIBLE);
        mountBtn.setText(" " + mounts.perm[i] + " ");
        mounts.index = i;
    }

    private void hideMount() {
        mountBtn.setVisibility(View.GONE);
        mounts.index = -1;
    }

    private void refreshTextPath() {
        pathAdapter.refreshPath(currentPath());
        pathGallery.setSelection(pathAdapter.getCount() - 2);
    }

    /**
     * Find all files in <em>path<em>, and set up fit file informations
     *
     * @param path the file path.
     * @param list File information, it will be clear, and set new informaiton.
     * @throws IOException
     */
    private void findFileInfo(String path, List<FileInfo> list) {
        list.clear();
        /***/
        if (pre_IsRoot == false) {
            File base = new File(path);
            File[] files = base.listFiles();
            if (files == null || files.length == 0)
                return;
            String name;
            String suffix;
            for (int i = 0; i < files.length; i++) {
                name = files[i].getName();

                if (pre_HideFile && files[i].isHidden()) {
                    continue;
                }
                // date = new Date(files[i].lastModified());
                int la = name.lastIndexOf('.');
                if (la == -1)
                    suffix = null;
                else
                    suffix = name.substring(la + 1).toLowerCase();
                list.add(new FileInfo(
                        name,
                        files[i].getAbsolutePath(),
                        switchIcon(suffix, files[i]),
                        null, // fileSize(files[i].length()),
                        // //date.toLocaleString(),
                        files[i].isDirectory()));
            }
            Collections.sort(list);
            return;
        }

        /** 帶 root */
        BufferedReader errReader = null, reader = null;
        DataOutputStream in = null;
        Process p = null;
        try {
            p = linux.shell.exec("su");
            errReader = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            in = new DataOutputStream(p.getOutputStream());
            String cmd = new String("ls -a " + "\"" + path + "/\"\nexit\n");
            in.write(cmd.getBytes());
            in.flush();
            in.close();
            reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            if (p.waitFor() != 0) {
                Toast.makeText(JamesFileManager.this, errReader.readLine(),
                        Toast.LENGTH_LONG).show();
                return;
            }
            String sr;
            while ((sr = reader.readLine()) != null) {
                //String[] files = sr.split(" +");
                //int length = files.length
                //for (int i = 0; i < length; i++) {
                String pt;
                if (currentPath().length() != 1)
                    pt = currentPath() + "/" + sr;
                else
                    pt = currentPath() + sr;
                File fl = new File(pt);
                if (pre_HideFile && fl.isHidden())
                    continue;
                int type, ps;
                boolean directory = fl.isDirectory();
                if (directory) {
                    type = FileAdapter.DIRECTORY;
                } else {
                    ps = sr.lastIndexOf('.');
                    if (ps == -1)
                        type = FileAdapter.UNKNOW;
                    else
                        type = switchIcon(sr.substring(ps + 1), fl);
                }
                list.add(new FileInfo(sr, pt, type, null, directory));
            }
            Collections.sort(list);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            p.destroy();
            try {
                if (errReader != null)
                    errReader.close();
                if (reader != null)
                    reader.close();
                if (in != null) {
                    in.close();
                    p.destroy();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
       /**/
    }

    // 對應檔案尾碼的圖示序號
    private final int switchIcon(String name, File file) {
        if (file.isDirectory())
            return FileAdapter.DIRECTORY;
        if (name == null) {
            return FileAdapter.UNKNOW;
        }
        name = name.toLowerCase();
        if (name.equals("txt") || name.equals("doc") || name.equals("pdf")) {
            return FileAdapter.TXT;
        } else if (name.equals("html") || name.equals("htm") ||
                name.equals("chm") || name.equals("xml")) {
            return FileAdapter.HTM;
        } else if (name.equals("jpeg") || name.equals("jpg") ||
                name.equals("bmp") || name.equals("gif") || name.equals("png")) {
            return FileAdapter.PHOTO;
        } else if (name.equals("rmvb") || name.equals("rmb") ||
                name.equals("avi") || name.equals("wmv") || name.equals("mp4")
                || name.equals("3gp") || name.equals("flv")) {
            return FileAdapter.MOVIE;
        } else if (name.equals("mp3") || name.equals("wav") || name.equals("wma")) {
            return FileAdapter.MUSIC;
        } else if (name.equals("apk")) {
            return FileAdapter.PKG;
        } else if (name.equals("zip") || name.equals("tar") ||
                name.equals("bar") || name.equals("bz2") || name.equals("bz")
                || name.equals("gz") || name.equals("rar")) {
            return FileAdapter.ZIP;
        }
        return FileAdapter.UNKNOW;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case JamesFileManager.RESULT_GET_FILE_SIZE:
                // 沒用
                Log.d(tag, "resultcode: " + resultCode);
                break;
        }
    }

    private void initAppMenu() {
        appMenu = (LinearLayout) findViewById(R.id.appmenu);
        LinearLayout row = (LinearLayout) appMenu.findViewById(R.id.approw1);
        LayoutInflater infl = getLayoutInflater();
        OnClickListener ocl = new OnClickListener() {

            @Override
            public void onClick(View v) {

                appMenuClick(v.getId());
                hideAppMenu();
            }
        };

        int[] drRes = {R.drawable.newfolder, R.drawable.newfile, R.drawable.paste,
                R.drawable.search, R.drawable.dialog, R.drawable.apkmanager,
                R.drawable.setting, R.drawable.multicon, R.drawable.filelib,
                R.drawable.close};
        String[] names = getResources().getStringArray(R.array.appnames);
        for (int i = 0; i < 10; i++) {
            if (i != 4 && i != 5 && i != 8) {    //James.
                if (i == 5) {
                    row = (LinearLayout) appMenu.findViewById(R.id.approw2);
                }
                RelativeLayout rl = (RelativeLayout) infl.inflate(R.layout.appmenuitem,
                        null);
                ImageButton iv = (ImageButton) rl.findViewById(R.id.menuicon);
                //iv.setImageResource(drRes[i]);
                iv.setBackgroundResource(drRes[i]);
                TextView tv = (TextView) rl.findViewById(R.id.menuname);
                tv.setText(names[i]);
                iv.setId(i);
                iv.setOnClickListener(ocl);
                row.addView(rl);
                if (i == MENU_SET_VIEW_STYLE) {
                    viewStyleButton = iv;
                    viewStyleTextView = tv;
                }
            }
        }
    }

    private void initSearchStop() {
        searchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (searching) {
                    searching = false;
                } else {
                    currentData.searchingTag = false;
                    searchLayout.setVisibility(View.INVISIBLE);
                    refreshPath(currentPath(), 1);
                }
            }
        });
    }

    private void doSearchFile(ArrayList<FileInfo> list, File dir,
                              String expr, boolean caseSense) {
        CharSequence tmpText = searchText.getText();
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        int length = files.length;
        String name;
        String suffix;
        String lowercaseName = "";
        if (!caseSense)
            expr = expr.toLowerCase();
        searchDir = dir.getAbsolutePath();
        listViewHandler.sendEmptyMessage(HANDLER_SET_SEARCHDIR);
        for (int i = 0; i < length; i++) {
            if (!searching)
                return;
            name = files[i].getName();
            if (pre_HideFile && files[i].isHidden()) {
                continue;
            }

            int la = name.lastIndexOf('.');
            if (la == -1)
                suffix = null;
            else
                suffix = name.substring(la + 1).toLowerCase();
            try {
                if (!caseSense) {
                    lowercaseName = name.toLowerCase();
                } else {
                    lowercaseName = name;
                }
                if (lowercaseName.matches(expr)) {
                    list.add(new FileInfo(
                            name,
                            files[i].getAbsolutePath(),
                            switchIcon(suffix, files[i]),
                            null, // fileSize(files[i].length()),
                            // //date.toLocaleString(),
                            files[i].isDirectory()));
                    listViewHandler.sendEmptyMessage(HANDLER_LIST_ADPATER_CHANGED);
                }
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
            }
            if (files[i].isDirectory())
                doSearchFile(list, files[i], expr, caseSense);
        }
        searchDir = tmpText;
        listViewHandler.sendEmptyMessage(HANDLER_SET_SEARCHDIR);
    }

    /**
     * 選單面板
     */
    private void appMenuClick(int whitch) {
        switch (whitch) {
            case MENU_CREATE_DIRECTORY:
                listListener.onClick(null, MENU_ITEM_CREATE_DIRECTORY);
                break;
            case MENU_CREATE_FILE:
                listListener.onClick(null, MENU_ITEM_CREATE_FILE);
                break;
            case MENU_PASTE:
                listListener.onClick(null, MENU_ITEM_PASTE);
                break;
            case MENU_SEARCH:
                if (searching) {
                    Toast.makeText(this, getString(R.string.searching),
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                SearchInputDialog sid = new SearchInputDialog(this);
                sid.setOnSearchListener(onSL);
                sid.show();
                break;
            case MENU_FINISH_ACTIVITY:
                if (listListener.copying())
                    dealCopyingOnExit();
                else
                    this.finish();
                break;
            case MENU_SHOW_COPY_DIALOG:
                listListener.showHiddenCopyDialog();
                break;

            case MENU_APK_MANAGER:
                Intent intent = new Intent();
                intent.putExtra(PRE_BACKUP_DIR, pre_BackupDir);

                intent.setAction("com.filemanager.apk.EDIT");
                this.startActivity(intent);
                break;
            case ROOT_COPY:
                //rootCopy();
                break;
            case MENU_SETTING:    //Setting.
                if (settingsView == null) {
                    settingsHide = true;
                    settingsView = new SettingsView(this);
                }
                if (settingsHide == false) {
                    settingsHide = true;
                    hideSettingsView();
                } else {
                    settingsHide = false;
                    settingsView.show(null);
                }
                break;
            case MENU_FILE_LIB:
                fileLibDialog.doWhat = FileLibDialog.FILE_LIB_OPEN;
                fileLibDialog.show();
                break;
            case MENU_SET_VIEW_STYLE:
                if (pre_ViewStyle == FileAdapter.STYLE_LIST) {
                    setFileViewStyle(FileAdapter.STYLE_GRID);
                } else {
                    setFileViewStyle(FileAdapter.STYLE_LIST);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 改變ROOT許可權
     */
    public boolean changedRoot(boolean r) {
      /* 嘗試獲取root許可權  */
        if (!r) {
            pre_IsRoot = false;
            return false;
        }
        try {
            if (LinuxShell.isRoot(Runtime.getRuntime(), 50)) {
                pre_IsRoot = true;

                Toast.makeText(JamesFileManager.this, "Root Success", Toast.LENGTH_LONG).show();
            } else {
                pre_IsRoot = false;
                Toast.makeText(JamesFileManager.this, "Root Fail", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pre_IsRoot;
    }

    /**
     * 隱藏設置介面
     */
    void hideSettingsView() {
        settingsHide = true;
        settingsView.hide();
    }

    /**
     * Find child views
     */
    private void findView() {
        fileViewList = (DDListView) findViewById(R.id.filelist);
        fileViewGrid = (DDGridView) findViewById(R.id.filegrid);
        pathGallery = (Gallery) findViewById(R.id.pathgallery);
        optUp = (ImageView) findViewById(R.id.optup);
        optTag = (ImageView) findViewById(R.id.opttag);
        optRefresh = (ImageView) findViewById(R.id.optrefresh);
        //optMultfile = (ImageView)findViewById(R.id.optmultfile);	//Marked by James.
        optMenu = (ImageView) findViewById(R.id.optmenu);

        tagLayout = (RelativeLayout) findViewById(R.id.tab);
        addTagButton = (ImageView) findViewById(R.id.addtag);
        tagRow = (TableRow) findViewById(R.id.tabrow);

        //Search View
        searchLayout = (LinearLayout) findViewById(R.id.searchlayout);
        searchBar = (ProgressBar) searchLayout.findViewById(R.id.searchbar);
        searchText = (TextView) searchLayout.findViewById(R.id.searchpath);
        searchBtn = (ImageButton) searchLayout.findViewById(R.id.searchclose);

        mountBtn = (Button) findViewById(R.id.mount);
    }

    private void newObject() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        scale = dm.densityDpi;
        linux = new LinuxFileCommand(Runtime.getRuntime());
        historyString = new ArrayList<String>();
        copyDialogLock = new ConditionVariable(false);

        datas = new ArrayList<FileData>();
        currentData = new FileData(new ArrayList<FileInfo>(),
                null, SDCARD_PATH);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onConfigurationChanged(newConfig);
    }

    public class Mounts {
        public static final String RO = "ro";
        public static final String RW = "rw";
        public final String[] fs = {
                "/", "/system"
        };
        public String[] perm = new String[fs.length];
        public String[] rawDev = new String[fs.length];
        public int index = -1;
    }

    class FileClickTimerTask extends TimerTask {
        private int position;

        public FileClickTimerTask(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            if (clickTime != 1) {
                return;
            }
            clearClickTime();
            Message msg = listViewHandler.obtainMessage(HANDLER_FILE_CLICK,
                    position, 0);
            listViewHandler.sendMessage(msg);
        }
    }

    class SearchFileThread implements Runnable {
        private String expr;
        private boolean caseSense, allMatch;

        public SearchFileThread(String expr, boolean allMatch, boolean caseSense) {
            this.expr = expr;
            this.allMatch = allMatch;
            this.caseSense = caseSense;
        }

        @Override
        public void run() {
            File parentDir = new File(currentPath());
            ArrayList<FileInfo> list = currentFileInfo();
            list.clear();
            currentData.selectedId.clear();
            listViewHandler.sendEmptyMessage(HANDLER_LIST_ADPATER_CHANGED);
            searchDir = currentPath();
            listViewHandler.sendEmptyMessage(HANDLER_SET_SEARCHDIR);
            searching = true;
            currentData.searchingTag = true;
            listViewHandler.sendEmptyMessage(HANDLER_SET_SEARCH_VISIBLE);
            String exprs;
            if (!allMatch) {
                if (!expr.contains("*"))
                    exprs = ".*" + expr + ".*";
                else
                    exprs = expr.replace("*", ".*");
            } else {
                exprs = expr;
            }
            doSearchFile(list, parentDir, exprs, caseSense);
            listViewHandler.sendEmptyMessage(HANDLER_SEARCHBAR_HIDE);
            searching = false;
            searchDir = getString(R.string.done_search, expr);
            listViewHandler.sendEmptyMessage(HANDLER_SET_SEARCHDIR);
        }
    }
}

/**
 * 當前檔案的資料包
 */
class FileData {
    public ArrayList<FileInfo> fileInfos;
    public ArrayList<Integer> selectedId;
    public String path;
    public boolean searchingTag = false;

    public FileData(ArrayList<FileInfo> fileInfos, ArrayList<Integer> selectedId,
                    String path) {
        if (fileInfos == null)
            this.fileInfos = new ArrayList<FileInfo>();
        else
            this.fileInfos = fileInfos;
        if (selectedId == null)
            this.selectedId = new ArrayList<Integer>();
        else
            this.selectedId = selectedId;
        if (path == null)
            this.path = JamesFileManager.SDCARD_PATH;
        else
            this.path = path;
    }
}