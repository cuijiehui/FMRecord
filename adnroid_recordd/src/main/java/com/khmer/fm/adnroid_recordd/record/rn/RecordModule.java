package com.khmer.fm.adnroid_recordd.record.rn;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.khmer.fm.adnroid_recordd.eventbus.BgMusicBackEvent;
import com.khmer.fm.adnroid_recordd.record.RecordActivity;
import com.khmer.fm.adnroid_recordd.record.bean.BgMusicBean;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * author : created by cui on 2019/10/10 11:46
 * 原生activity与react交互
 */
public class RecordModule extends ReactContextBaseJavaModule {
    private ReactContext mReactContext;
    public RecordModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mReactContext = reactContext;

    }

    @Nonnull
    @Override
    public String getName() {
        return "RecordModule";
    }

    @ReactMethod
    public void startActivityFromJS(String name, ReadableMap map){
        Log.d("startAcitivityFormJs","Name = "+ name);
        double value=0;
//        for (Map.Entry<String, Object> entry : map.toHashMap().entrySet()) {
//            String key = entry.getKey();
//            value = (double)entry.getValue();
//        }
        try{
            value =  map.getDouble("language");
        }catch (Exception e){
            throw new JSApplicationIllegalArgumentException(
                    "解析map失败 : "+e.getMessage());
        }finally {
            try{
                Activity currentActivity = getCurrentActivity();
                Log.d("startAcitivityFormJs","Name = "+ name);
                if(null!=currentActivity){
                    Class toActivity = Class.forName(name);
                    Intent intent = new Intent(currentActivity,toActivity);
                    intent.putExtra("language",value);
                    currentActivity.startActivity(intent);
                }
            }catch(Exception e){
                throw new JSApplicationIllegalArgumentException(
                        "不能打开Activity : "+e.getMessage());
            }
        }

    }
    @ReactMethod
    public void goBackToNative(ReadableMap map){
        BgMusicBean bgMusicBean = new BgMusicBean();
        BgMusicBackEvent bgMusicBackEvent=  new BgMusicBackEvent();
            try{
                bgMusicBean.setAuthor(map.getString("author"));
                bgMusicBean.setCover(map.getString("cover"));
                bgMusicBean.setEnglishTitle(map.getString("englishTitle"));
                bgMusicBean.setLocalUrl(map.getString("localUrl"));
                bgMusicBean.setDuration((float) map.getDouble("duration"));
                bgMusicBean.setPath( map.getString("path"));
                bgMusicBean.setId( map.getString("id"));
                bgMusicBean.setTitle( map.getString("title"));
                bgMusicBean.setKhmerTitle( map.getString("khmerTitle"));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                bgMusicBackEvent.setBgMusicBean(bgMusicBean);
                EventBus.getDefault().post(bgMusicBackEvent);
                getCurrentActivity().finish();
            }


    }

    @ReactMethod
    public void openNativeVC() {
        Intent intent = new Intent();
        intent.setClass(mReactContext, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mReactContext.startActivity(intent);
    }

    @ReactMethod
    public void dataToRN(Promise promise){
        try{
            Activity currentActivity = getCurrentActivity();
            String result = currentActivity.getIntent().getStringExtra("type");
            if (TextUtils.isEmpty(result)){
                result = "没有数据";
            }
            promise.resolve(result);
        }catch (Exception e){
            promise.resolve(e.getMessage());
        }
    }

    @ReactMethod
    public void dataToJS(Callback successBack, Callback errorBack){
        try{
            Activity currentActivity = getCurrentActivity();
            String result = currentActivity.getIntent().getStringExtra("data");
            if (TextUtils.isEmpty(result)){
                result = "没有数据";
            }
            successBack.invoke(result);
        }catch (Exception e){
            errorBack.invoke(e.getMessage());
        }
    }
}
