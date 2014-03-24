package org.zeus.arena;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.zeus.arena.controls.ChangeWeaponControl;
import org.zeus.arena.controls.ControlScheme;
import org.zeus.arena.controls.DisplayOnlyFireControl;
import org.zeus.arena.controls.ImprovedFireControl;
import org.zeus.arena.controls.ImprovedJumpControl;
import org.zeus.arena.controls.JumpControl;
import org.zeus.arena.controls.LeftAnalogControl;
import org.zeus.arena.controls.StationaryFire;
import org.zeus.arena.controls.UseItemControl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ControlLayout extends Activity{

	private RelativeLayout controlsLayout;
	private boolean arrangingControls;
	private Game game;
	private Control selected;
	//private Map<Control, ImageView> controls;
	private List<Control> controls;
	private List<Control> updatedControls;
	private int screenWidth;
	private int screenHeight;
	private enum GestureType {NONE, DRAG, PINCH, LONGPRESS};
	private GestureType gesture = GestureType.NONE;
	private Point firstTouchDown = null;
	private Point subsequentTouchDown = null;
	private final int minSpacing = 20;
	private final int minRadius = 30;
	private final int scalingFactor = 2;	//reduces size increase of a control by a factor
	private float oldDistance = -1;
	private long touchDown = 0;
	private Point startTouchDown = null;
	private long interval = 500;
	private Dialog dialog;
	private Vibrator vibrator;
	private Control addControl = null;
	
	public void onCreate(Bundle savedInstanceState) {
		arrangingControls = false;
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    	
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		dialog = new Dialog(this);
		setContentView(R.layout.controlsinfo);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenWidth = displaymetrics.widthPixels;
		screenHeight = displaymetrics.heightPixels;
		
		Button done = (Button)findViewById(R.id.arrangeOk);
		done.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				arrangeControls();
			}
		});
		Button cancel = (Button)findViewById(R.id.arrangeCancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	public void arrangeControls(){
		game = Persistence.getPersistence().getGame();
		setupControls();
		arrangingControls = true;
		controlsLayout = new RelativeLayout(this);
		setContentView(controlsLayout, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		updatedControls = new Vector<Control>();
		updatedControls.addAll(controls);
		refreash();
	}
	
	public void refreash(){
		addViews();
		game.refreash();
	}
	
	public void addViews(){
		final Context context = this;
		controlsLayout.post(new Runnable(){ 
			public void run(){
				for (Control control : updatedControls){
					if (control.isOn()){
						RelativeLayout.LayoutParams params = control.getLayoutParams(screenWidth, screenHeight);
						if (params != null){
							View view = control.getImageView(context);
							controlsLayout.removeView(view);
							controlsLayout.addView(view, params);
						}
					}
					else if (control.isVisable()){
						View view = control.getImageView(context);
						controlsLayout.removeView(view);
					}
				}
				updatedControls.removeAll(updatedControls);
			}
		});
	}
	
	private void findSelectedControl(MotionEvent event){
		List<Control> touchedControls = touchedControls(new MyMotionEvent(event));
		if (!touchedControls.isEmpty()){
			Control smallest = touchedControls.get(0);
			for (Control control : touchedControls){
				if (control.getRadius() < smallest.getRadius()){
					smallest = control;
				}
			}
			selected = smallest;
		}
	}
	
	public boolean onFirstTouch(MyMotionEvent event){
		boolean result = false;
		if (event.actionApplies(MotionEvent.ACTION_DOWN)){
			touchDown = System.currentTimeMillis();
			startTouchDown = event.getPoint();
		}
		else if (event.actionApplies(MotionEvent.ACTION_MOVE)){
			if (startTouchDown != null){
				if (event.getPoint().substract(startTouchDown).length > minSpacing){
					startTouchDown = null;
				}
				firstTouchDown = event.getPoint();
			}
			else{
				firstTouchDown = event.getPoint();
			}
		}
		else if(event.actionApplies(MotionEvent.ACTION_UP)){
			firstTouchDown = null;
			selected = null;
			startTouchDown = null;
		}
		return result;
	}
	
	public boolean onSubsequentTouch(MyMotionEvent event){
		boolean result = false;
		if (event.getState() == 1){	//down
			if (event.getPoint().substract(firstTouchDown).length > minSpacing){
				subsequentTouchDown = event.getPoint();
			}
		}
		else{
			subsequentTouchDown = null;
			selected = null;
		}
		return result;
	}
	
	public void findGesture(){
		if (selected == null && startTouchDown == null){
			gesture = GestureType.NONE;
		}
		else if (gesture != GestureType.PINCH){	//if no pinch gesture in progress
			//find gesture type
			if (startTouchDown != null){
				if (System.currentTimeMillis() - touchDown > interval){
					gesture = GestureType.LONGPRESS;
				}
				//else{
				//	gesture = GestureType.NONE;
				//}
			}
			else{
				if (firstTouchDown == null){
					gesture = GestureType.NONE;
					selected = null;
				}
				else{
					gesture = GestureType.DRAG;
					if (subsequentTouchDown != null){
						gesture = GestureType.PINCH;
					}
				}
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = false;
		if (arrangingControls){
			if (selected == null){
				findSelectedControl(event);
			}
			onFirstTouch(new MyMotionEvent(event));
			if (event.getPointerCount() > 1){
				for (int p = 1; p < event.getPointerCount(); p++){
					result = onSubsequentTouch(new MultiMotionEvent(event, p));
				}
			}
			else{
				subsequentTouchDown = null;
			}
			findGesture();
			if (gesture == GestureType.DRAG){
				selected.setPosition(new Point(event.getX(), event.getY()));
				updatedControls.add(selected);
		    	refreash();
			}
			else if (gesture == GestureType.PINCH){
				float newDistance = subsequentTouchDown.substract(firstTouchDown).length;
				if (oldDistance >= -1){
					float ratio = newDistance/oldDistance;
					ratio = ((ratio-1)/scalingFactor)+1;
					int newRadius = (int) (selected.getRadius()*ratio);
					if (newRadius > minRadius){
						if (newRadius == selected.getRadius()){
							if (ratio < 1 && newRadius > (minRadius+1)){
								newRadius--;
							}
							else if (ratio > 1){
								newRadius++;
							}
						}
						selected.setRadius(newRadius);
					}
					updatedControls.add(selected);
					refreash();
				}
				oldDistance = newDistance;
			}
			else if (gesture == GestureType.LONGPRESS){
				if (!dialog.isShowing()){
					if (game.vibrationOn()){
						vibrator.vibrate(100);
					}
					if (selected == null){
						showAddControlOptions();
					}
					else{
						showControlOptions();
					}
				}
			}
		}
		return result;
	}
	
	public List<Control> touchedControls(MyMotionEvent event){
		List<Control> result = new Vector<Control>();
		for (Control control : controls){
			if (control.isVisable() && control.isOn()){
				if (control.touched(event)){
					result.add(control);
				}
			}
		}
		return result;
	}
	
	public ControlScheme getControlScheme(){
		ControlScheme result = ControlScheme.NONE;
		List<String> values = PlayerPreferences.getThePlayerPreferences().getPreference("Scheme");
		if (values.get(0).equals("Improved")){
			result = ControlScheme.IMPROVED;
		}
		else if(values.get(0).equals("Traditional")){
			result = ControlScheme.TRADITIONAL;
		}
		return result;
	}
	
	public void setupControls(){
		controls = new Vector<Control>();
		ControlScheme scheme = getControlScheme();
		if (scheme != ControlScheme.NONE){
			if (scheme == ControlScheme.IMPROVED){
				controls.add(new ImprovedFireControl(null));
				controls.add(new ImprovedJumpControl());
				controls.add(new StationaryFire());
			}
			else if (scheme == ControlScheme.TRADITIONAL){
				controls.add(new LeftAnalogControl());
				controls.add(new DisplayOnlyFireControl());
				controls.add(new JumpControl());
			}
			controls.add(new ChangeWeaponControl());
			controls.add(new UseItemControl());
		}
	}
	
	private void showControlOptions()
	{
		final Control selectedControl = selected;
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.controloptions);
		dialog.setTitle("Control Options");
		SeekBar transparency = (SeekBar) dialog.findViewById(R.id.seekBar1);
		transparency.setMax(255);
		transparency.setProgress(selectedControl.getAlpha());
		transparency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				selectedControl.setAlpha(progress);
				updatedControls.add(selectedControl);
				refreash();
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		Button remove = (Button) dialog.findViewById(R.id.removecontrol);
		remove.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				selectedControl.setOn(false);
				updatedControls.add(selectedControl);
				refreash();
				dialog.dismiss();
			}
		});
		Button done = (Button) dialog.findViewById(R.id.controloptionsdone);
		done.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	private void showAddControlOptions()
	{
		dialog = new Dialog(this);
		
		dialog.setContentView(R.layout.addcontroloptions);
		dialog.setTitle("Control Options");
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner1);
		List<String> spinnerItems = new Vector<String>();
		for (Control control : controls){
			if (!control.isOn() && control.isVisable()){
				spinnerItems.add(control.getReadableName());
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems);
		spinner.setAdapter(adapter);
		//findAddControl();
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				findAddControl();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				addControl = null;
			}
		});
		Button done = (Button) dialog.findViewById(R.id.addcontrolbutton);
		done.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (addControl != null){
					addControl.setOn(true);
					updatedControls.add(addControl);
					refreash();
				}
				dialog.dismiss();
			}
		});
		Button cancel = (Button) dialog.findViewById(R.id.cancelcontrolbutton);
		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				addControl = null;
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void findAddControl(){
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner1);
		String selectedItem = spinner.getSelectedItem().toString();
		for (Control control : controls){
			if (control.getReadableName().equals(selectedItem)){
				addControl = control;
			}
		}
	}
}
