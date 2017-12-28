package com.filemanager;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.utils.Common;
/**
 * 設置介面
 * Author: James.
 * */
public class SettingsView {


	private JamesFileManager fileManager;
	/** 設置主介面layout*/
	RelativeLayout settingsLayout = null;
	/** 備份目錄輸入框*/
	private EditText backupEdit = null;
	/** 備份layout*/
	private LinearLayout apkText = null;
	/** 備份目錄顯示*/
	private TextView backupDir = null;

	private CheckBox hideFileCheckBox = null;
	private CheckBox rootCheckBox = null;
	private boolean preHideFileState = false;
	//private String preBackupDir = "";	//Marked by James. 拿掉"APK備份目錄"
	private TextView helptv = null;


	private final static String SP_BUACKUPDIR = "backupdir";
	private final static String SP_HIDEFILE = "hidefile";
	private final static String SP_ROOT = "sproot";
	private Animation settingsViewShowAnimation = null;
	private Animation viewHideAnimation = null;

	public SettingsView(JamesFileManager fd) {
		init(fd);
		// TODO Auto-generated constructor stub
	}

	private void init(JamesFileManager fd){
		fileManager = fd;

		settingsLayout = (RelativeLayout) fileManager.findViewById(R.id.settingslayout);
		settingsLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		initButton();
		initCheckBox();
		initHelpView();
	}

	/**
	 * 顯示設定介面時,保存當前設置
	 * */
	public void saveState() {
		preHideFileState = fileManager.isHideFile();
	}

	/** 取消設置時,還原顯示設定介面前的狀態*/
	public void restoreState() {
		hideFileCheckBox.setChecked(preHideFileState);
		//backupDir.setText(preBackupDir);	//Marked by James. 拿掉"APK備份目錄"
	}

	/** 顯示設定介面*/
	public void show(Bundle savedInstanceState) {

		settingsLayout.setVisibility(View.VISIBLE);
		saveState();
		if (savedInstanceState != null)
			restoreInstanceState(savedInstanceState);
		if (settingsViewShowAnimation == null) {
			settingsViewShowAnimation = AnimationUtils
					.loadAnimation(fileManager, R.anim.scalesize);
		}
		settingsLayout.setVisibility(View.VISIBLE);
		settingsLayout.startAnimation(settingsViewShowAnimation);

		refreshSDView();
		refreshDataView();
	}

	/** 隱藏設置介面*/
	public void hide() {

		if (viewHideAnimation == null)
			viewHideAnimation = AnimationUtils.loadAnimation(
					fileManager, R.anim.settingshide);
		settingsLayout.startAnimation(viewHideAnimation);
		settingsLayout.setVisibility(View.GONE);
	}

	/** 轉屏或被android回收時*/
	public void saveInstanceState(Bundle outState){
		outState.putBoolean(SP_HIDEFILE, hideFileCheckBox.isChecked());
		outState.putString(SP_BUACKUPDIR, backupDir.getText().toString());
		outState.putBoolean(SP_ROOT, rootCheckBox.isChecked());
	}

	public void restoreInstanceState(Bundle state){
		rootCheckBox.setChecked(state.getBoolean(SP_ROOT));
		hideFileCheckBox.setChecked(state.getBoolean(SP_HIDEFILE));
		backupDir.setText(state.getString(SP_BUACKUPDIR));
		//adapter.notifyDataSetChanged();
	}



	public void initButton(){

		Button okButton = (Button) fileManager.findViewById(R.id.settingok);
		Button cancelButton = (Button) fileManager.findViewById(R.id.settingcancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				restoreState();
				fileManager.hideSettingsView();
			}
		});

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				fileManager.hideSettingsView();
				if (isChagnedHideFileState())
					fileManager.setHideFile(hideFileCheckBox.isChecked());
			}
		});
	}


	private boolean isChagnedHideFileState() {
		return hideFileCheckBox.isChecked() != preHideFileState;
	}

	private void initCheckBox() {
		if (hideFileCheckBox == null){
			hideFileCheckBox = (CheckBox) settingsLayout.findViewById(R.id.settinghidefilebox);
			/**
			 ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(
			 ViewGroup.LayoutParams.FILL_PARENT,
			 ViewGroup.LayoutParams.WRAP_CONTENT);
			 hideFileCheckBox.setLayoutParams(p);
			 /***/
			hideFileCheckBox.setGravity(Gravity.FILL);
			hideFileCheckBox.setFocusable(false);
			hideFileCheckBox.setChecked(fileManager.isHideFile());
		}
		/*if (rootCheckBox == null) {	//Marked by James.
			rootCheckBox = (CheckBox) settingsLayout.findViewById(R.id.settingrootbox);
			rootCheckBox.setChecked(fileManager.isRoot());
			rootCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
											 boolean isChecked) {
					// TODO Auto-generated method stub
					rootCheckBox.setChecked(fileManager.changedRoot(isChecked));
				}

			});
		}*/

	}
	private TextView sdText = null,
			dataText = null;
	private String sdPath = null,
			dataPath = null;
	private void refreshSDView(){
		if (sdText == null) {
			sdText = (TextView) settingsLayout.findViewById(R.id.sdtext);
			sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		if (sdPath == null) {
			sdText.setText("SD卡不存在");
			return;
		}
		StatFs sf = new StatFs(sdPath);
		long bs = sf.getBlockSize();
		long total = sf.getBlockCount() * bs;
		long free = sf.getAvailableBlocks() * bs;
		sdText.setText("總容量: " + Common.formatFromSize(total)
				+ "  空閒: " + Common.formatFromSize(free));
	}

	private void refreshDataView(){
		if (dataPath == null) {
			dataText = (TextView) settingsLayout.findViewById(R.id.datatext);
			dataPath = Environment.getDataDirectory().getAbsolutePath();
		}
		StatFs sf = new StatFs(dataPath);
		long bs = sf.getBlockSize();
		long total = sf.getBlockCount() * bs;
		long free = sf.getAvailableBlocks() * bs;
		dataText.setText("總容量: " + Common.formatFromSize(total)
				+ "  空閒: " + Common.formatFromSize(free));
	}

	private void initHelpView(){
		if (helptv == null){
			helptv = (TextView) settingsLayout.findViewById(R.id.settingabouthelp);
			helptv.setGravity(Gravity.FILL);
			//helptv.setBackgroundResource(R.drawable.pressed_background);
			helptv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
					InputStream ips = fileManager.getResources().openRawResource(R.raw.readme);
					//BufferedReader br = new BufferedReader(new InputStreamReader(ips));
					DataInputStream dis = new DataInputStream(ips);
					try {
						byte[] bytes = new byte[dis.available()];
						String str = "";
						while (ips.read(bytes) != -1)
							str = str + new String(bytes, "GBK");
						//StringBuffer str = new StringBuffer();
						//while(br.ready())
						//	str.append(br.readLine());
						b.setTitle("關於").setMessage(str);
						b.setPositiveButton(fileManager.ok, null);
						b.create().show();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							dis.close();
							ips.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

}
