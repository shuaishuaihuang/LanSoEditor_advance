package com.example.advanceDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;



import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.advanceDemo.view.PaintConstants;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.ViewLayerRelativeLayout;
import com.lansosdk.box.onDrawPadPreviewProgressListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.box.onDrawPadSnapShotListener;
import com.lansosdk.videoeditor.DrawPadView;
import com.lansosdk.videoeditor.LanSongUtil;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 *  演示: 视频涂鸦
 *  
 */
public class ViewLayerDemoActivity extends Activity{
    private static final String TAG = "ViewLayerDemoActivity";

    private String mVideoPath;

    private DrawPadView mDrawPadView;
    
    private MediaPlayer mplayer=null;
    
    private VideoLayer  mainVideoLayer=null;
    private ViewLayer mViewLayer=null;
    
//    
    private String editTmpPath=null;  //用来保存画板录制的目标文件路径.
    private String dstPath=null;

    private ViewLayerRelativeLayout mLayerRelativeLayout;
    private MediaInfo  mInfo=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vview_drawimage_demo_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mInfo=new MediaInfo(mVideoPath,false);
        if(mInfo.prepare()==false){
            Log.e(TAG, " video path is error.finish\n");
            finish();
        }
        
        mDrawPadView = (DrawPadView) findViewById(R.id.id_vview_realtime_drawpadview);
        
      
        mLayerRelativeLayout=(ViewLayerRelativeLayout)findViewById(R.id.id_vview_realtime_gllayout);
      
    	
        initView();
        
        //在手机的默认路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
	    
	    //演示例子用到的.
		PaintConstants.SELECTOR.COLORING = true;
		PaintConstants.SELECTOR.KEEP_IMAGE = true;
		
        new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		},500);
    }
    private void startPlayVideo()
    {
        	  mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
				
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					initDrawPad();
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					stopDrawPad();
				}
			});
        	  mplayer.prepareAsync();
    }
    
    long lastTimeUs=0;
    /**
     * Step1: 设置DrawPad 画板的尺寸.
     * 并设置是否实时录制画板上的内容.
     */
    private void initDrawPad()
    {
    	MediaInfo info=new MediaInfo(mVideoPath,false);
    	if(info.prepare())
    	{
    		mDrawPadView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH,25);
    		mDrawPadView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    		
    		mDrawPadView.setOnDrawPadSnapShotListener(new onDrawPadSnapShotListener() {
				
				@Override
				public void onSnapShot(DrawPad v, Bitmap bmp) {
					// TODO Auto-generated method stub
					Log.i(TAG,"drawPad snap shot!!!!!"+ bmp.getWidth()+" x " +bmp.getHeight());
//					LanSongUtil.savePng(bmp);
				}
			});
    		mDrawPadView.setOnDrawPadProgressListener(new onDrawPadProgressListener() {
				
				@Override
				public void onProgress(DrawPad v, long currentTimeUs) {
					// TODO Auto-generated method stub
					if(currentTimeUs>7000*1000)  //在第7秒的时候, 不再显示.
		  			{
		  				hideWord();
		  			}else if(currentTimeUs>3*1000*1000)  //在第三秒的时候, 显示tvWord
		  			{
		  				showWord();
		  			}
				}
			});
        	mDrawPadView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
    			
    			@Override
    			public void onSizeChanged(int viewWidth, int viewHeight) {
    				// TODO Auto-generated method stub
    				startDrawPad();
    			}
    		});
    	}
    }
    /**
     * Step2: Drawpad设置好后, 开始画板线程运行,
     * 并增加一个视频图层和 view图层.
     */
    private void startDrawPad()
    {
    	if(mDrawPadView.startDrawPad())
    	{
    		mainVideoLayer=mDrawPadView.addMainVideoLayer(mplayer.getVideoWidth(),mplayer.getVideoHeight(),null);
    		if(mainVideoLayer!=null){
    			mplayer.setSurface(new Surface(mainVideoLayer.getVideoTexture()));
    		}
    		mplayer.start();
    		addViewLayer();	
    	}
    }
    /**
     * Step3: 做好后, 停止画板, 因为画板里没有声音, 这里增加上原来的声音.
     */
    private void stopDrawPad()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning()){
			mDrawPadView.stopDrawPad();
			
			toastStop();
			
			if(SDKFileUtils.fileExist(editTmpPath))
			{
				boolean ret=VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR, dstPath);
				if(!ret){
					dstPath=editTmpPath;
				}else
					SDKFileUtils.deleteFile(editTmpPath);
		    	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.VISIBLE);
			}
			
		}
    }
    /**
     * 增加一个UI图层: ViewLayer 
     */
    private void addViewLayer()
    {
    	if(mDrawPadView!=null && mDrawPadView.isRunning())
    	{
    		mViewLayer=mDrawPadView.addViewLayer();
            
    		//把这个图层绑定到LayerRelativeLayout中.从而LayerRelativeLayout中的各种UI界面会被绘制到Drawpad上.
    		mLayerRelativeLayout.bindViewLayer(mViewLayer);
    		
            mLayerRelativeLayout.invalidate();//刷新一下.
            
            ViewGroup.LayoutParams  params=mLayerRelativeLayout.getLayoutParams();
            params.height=mViewLayer.getPadHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
            mLayerRelativeLayout.setLayoutParams(params);
            
            //UI图层的移动缩放旋转.
//            mViewLayer.setScale(0.5f);
//            mViewLayer.setRotate(60);
//            mViewLayer.setPosition(mViewLayer.getPadWidth()-mViewLayer.getLayerWidth()/4,mViewLayer.getPositionY()/4);
    	}
    }
	  
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(mplayer!=null){
    		mplayer.stop();
    		mplayer.release();
    		mplayer=null;
    	}
    	
    	
    	if(mDrawPadView!=null){
    		mDrawPadView.stopDrawPad();
    		mDrawPadView=null;        		   
    	}
    	  if(SDKFileUtils.fileExist(dstPath)){
    		  SDKFileUtils.deleteFile(dstPath);
          }
          if(SDKFileUtils.fileExist(editTmpPath)){
        	  SDKFileUtils.deleteFile(editTmpPath);
          } 
    }
    //--------------------------------------一下为UI界面-----------------------------------------------------------
    int  snapShotW=480;
    int  snapShotH=480;
    private void initView()
    {
    	  tvWord=(TextView)findViewById(R.id.id_vview_tvtest);
          
          findViewById(R.id.id_vview_realtime_saveplay).setOnClickListener(new OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  				 if(SDKFileUtils.fileExist(dstPath)){
  		   			 	Intent intent=new Intent(ViewLayerDemoActivity.this,VideoPlayerActivity.class);
  			    	    	intent.putExtra("videopath", dstPath);
  			    	    	startActivity(intent);
  		   		 }else{
  		   			 Toast.makeText(ViewLayerDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
  		   		 }
  			}
  		});
      	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.GONE);
    }
    private TextView tvWord; 
    private void showWord()
    {
    	 if(tvWord!=null&& tvWord.getVisibility()!=View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(ViewLayerDemoActivity.this, R.anim.slide_right_in));
				 tvWord.setVisibility(View.VISIBLE); 
			 }
    }
    private void hideWord()
    {
    	 if(tvWord!=null&& tvWord.getVisibility()==View.VISIBLE){
				 tvWord.startAnimation(AnimationUtils.loadAnimation(ViewLayerDemoActivity.this, R.anim.slide_right_out));
  			 tvWord.setVisibility(View.GONE); 
			 }
    }
    
}
