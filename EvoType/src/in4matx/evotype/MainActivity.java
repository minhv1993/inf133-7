package in4matx.evotype;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String[][] dictionary = {	{"tshwb","vkjqxz","fcpgy","nrmdl","aeiou"},
										{"TSHWB","VKJQXZ","FCPGY","NRMDL","AEIOU"},
										{".,?!@-_","*\\/=+~","()#$%^&","56789","01234"}};
	
	private SensorManager mSensorManager;
	private SensorEventListener sensorEventListener;
	private Sensor mOrientation;

	private Lock lock = new ReentrantLock();
	private TextView mTypeField;
	private TextView medTopText;
	private TextView medBotText;
	private TextView medLeftText;
	private TextView medRightText;
	private TextView smlLeftText;
	private TextView smlRightText;
	private TextView currText;
	private Switch mSwitch;
	private float mPitch = 0;
	private float mRoll = 0;
	private float initPitch = 0;
	private float initRoll = 0;
	private int currType = 0;
	private int currGroup = 0;
	private int stringSize = 0;
	private int currCharPos = 0;
	private MediaPlayer mMediaPlayer;
	

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        // INITIATE STRING SIZE
		stringSize = dictionary[currType][currGroup].length();
        
        // GET ALL THE TEXT VIEW
        mTypeField = (TextView) findViewById(R.id.typingField);
    	medTopText = (TextView) findViewById(R.id.medTop);
    	medTopText.setTextColor(Color.GRAY);
    	medBotText = (TextView) findViewById(R.id.medBot);
    	medBotText.setTextColor(Color.GRAY);
    	medLeftText = (TextView) findViewById(R.id.medLeft);
    	medRightText = (TextView) findViewById(R.id.medRight);
    	smlLeftText = (TextView) findViewById(R.id.smlLeft);
    	smlLeftText.setTextColor(Color.GRAY);
    	smlRightText = (TextView) findViewById(R.id.smlRight);
    	smlRightText.setTextColor(Color.GRAY);
    	currText = (TextView) findViewById(R.id.curr);
    	currText.setTextColor(Color.BLUE);
    	
    	// GET THE SWITCH
        mSwitch = (Switch) findViewById(R.id.evoTypeSwitch);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	@Override
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        		if(!isChecked){
        			mTypeField.setText("");
        		}
        	}
        });
        
        // GET THE SENSOR
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mOrientation = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
        // GET AUDIO MANAGER
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.facebook_ringtone_pop);
                
        setText();
        
        sensorEventListener = new SensorEventListener(){        	        	
        	@TargetApi(Build.VERSION_CODES.GINGERBREAD) @Override
        	public void onSensorChanged(SensorEvent event){
        		mPitch = event.values[1];
        		mRoll = event.values[2];
        		if(!mSwitch.isChecked()){
        			initPitch = mPitch;
        			initRoll = mRoll;
        		}
        		updateUI();
        	}

			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void updateUI(){
    	if(lock.tryLock()){
    		float pitchDiff = initPitch-mPitch;
    		float rollDiff = initRoll-mRoll;
    		boolean changed = false;
    		
    		// Tilt Left To Select Letter
    		if(rollDiff >= 15){
    			changed = true;
    			currCharPos = (currCharPos == 0)?stringSize-1:currCharPos-1;
    		}
    		// Tilt Right To Change Type
    		else if(rollDiff <= -15){
    			changed = true;
    			currType = (currType == 2)?0:currType+1;
        		stringSize = dictionary[currType][currGroup].length();
    			currCharPos = 0;
    		}
    		// Tilt Up To Change Group
    		if(pitchDiff >= 15){
    			changed = true;
    			currGroup = (currGroup == 0)?4:currGroup-1;
        		stringSize = dictionary[currType][currGroup].length();
    			currCharPos = 0;
    		}
    		// Tilt Down To Input
    		else if(pitchDiff <= -10){
    			String prevText = mTypeField.getText().toString();
    			mTypeField.setText(String.format("%s%s", prevText,dictionary[currType][currGroup].subSequence(currCharPos, currCharPos+1)));
    			mMediaPlayer.start();
    		}
    		
    		if(changed){
    			setText();
    			mMediaPlayer.start();
    		}
    		lock.unlock();
    	}
    }
    
    public void setText(){
		medTopText.setText(dictionary[currType][(currGroup == 0)?4:currGroup-1]);
    	medBotText.setText(dictionary[currType][(currGroup == 4)?0:currGroup+1]);
    	currText.setText(dictionary[currType][currGroup].subSequence(currCharPos, currCharPos+1));
    	int medLeftPos = (currCharPos == 0)?stringSize-1:currCharPos-1;
    	medLeftText.setText(dictionary[currType][currGroup].subSequence(medLeftPos, medLeftPos+1));
    	int medRightPos = (currCharPos == stringSize-1)?0:currCharPos+1;
    	medRightText.setText(dictionary[currType][currGroup].subSequence(medRightPos, medRightPos+1));
    	int smlLeftPos = 0;
    	if(currCharPos == 0){
    		smlLeftPos = stringSize-2;
    	} else if(currCharPos == 1){
    		smlLeftPos = stringSize-1;
    	} else {
    		smlLeftPos = currCharPos-2;
    	}
    	smlLeftText.setText(dictionary[currType][currGroup].subSequence(smlLeftPos, smlLeftPos+1));
    	int smlRightPos = 0;
    	if(currCharPos == stringSize-1){
    		smlRightPos = 1;
    	} else if(currCharPos == stringSize-2){
    		smlRightPos = 0;
    	} else {
    		smlRightPos = currCharPos+2;
    	}
    	smlRightText.setText(dictionary[currType][currGroup].subSequence(smlRightPos, smlRightPos+1));
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mSensorManager.registerListener(sensorEventListener, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    public void onPause(){
  	  	mSensorManager.unregisterListener(sensorEventListener);
    	super.onPause();
    }
    
    @Override
    public void onStop(){
    	mSensorManager.unregisterListener(sensorEventListener);
    	super.onStop();
    }
}
