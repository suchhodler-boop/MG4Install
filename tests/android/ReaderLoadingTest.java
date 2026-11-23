package com.mg4.install.tests;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import com.mg4.install.*;

public class ReaderLoadingTest extends Instrumentation {
 Activity activity;int checks;
 Object field(String name)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(activity);}
 Object invoke(String name,Class<?>[] types,Object... args)throws Exception{Method m=MainActivity.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(activity,args);}
 void read(boolean releases,String slug)throws Exception{invoke("readProject",new Class[]{FeedParser.Repo.class,boolean.class,boolean.class},new FeedParser.Repo(slug,"Test"),releases,false);}
 void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 void ui(Runnable work){runOnMainSync(work);}
 String text(View v){String out=v instanceof TextView?((TextView)v).getText().toString():"";if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)out+="\n"+text(((ViewGroup)v).getChildAt(i));return out;}
 View find(View v,String label){if(v instanceof TextView&&((TextView)v).getText().toString().equals(label))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),label);if(found!=null)return found;}return null;}
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){Bundle result=new Bundle();CountDownLatch hold=new CountDownLatch(1);try{
  activity=startActivitySync(new Intent().setClassName(getTargetContext().getPackageName(),"com.mg4.install.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  ExecutorService worker=(ExecutorService)field("worker");CountDownLatch entered=new CountDownLatch(1);
  worker.submit(()->{entered.countDown();try{hold.await(45,TimeUnit.SECONDS);}catch(InterruptedException ignored){}});
  check(entered.await(45,TimeUnit.SECONDS),"Worker became available");
  final Dialog[] original=new Dialog[1];final int[] generation=new int[1];final Future<?>[] task=new Future<?>[1];final Throwable[] error=new Throwable[1];
  ui(()->{try{
   read(false,"suchhodler-boop/MG4Install");original[0]=(Dialog)field("projectReader");generation[0]=(Integer)field("projectReadGeneration");task[0]=(Future<?>)field("projectReadTask");
   check(original[0].isShowing(),"Reader immediately visible");check(text(original[0].getWindow().getDecorView()).contains("Loading README from GitHub"),"Loading feedback before network completes");
   read(false,"suchhodler-boop/MG4Install");read(true,"suchhodler-boop/MG4Install");check(field("projectReader")==original[0],"Rapid taps reuse one window");check(field("projectReadTask")==task[0],"Rapid taps do not queue more fetches");
  }catch(Throwable e){error[0]=e;}});
  waitForIdleSync();if(error[0]!=null)throw new Exception(error[0]);
  android.graphics.Bitmap screenshot=getUiAutomation().takeScreenshot();if(screenshot!=null){try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),"reader-loading.png"))){screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}screenshot.recycle();}
  ui(()->original[0].dismiss());
  waitForIdleSync();if(error[0]!=null)throw new Exception(error[0]);
  ui(()->{try{
   check(field("projectReader")==null,"Close clears reader");check(task[0].isCancelled(),"Close cancels queued fetch");
   read(true,"suchhodler-boop/MG4Install");check(text(((Dialog)field("projectReader")).getWindow().getDecorView()).contains("Loading release notes"),"Release notes have loading feedback");
   check(!(Boolean)invoke("currentProjectRead",new Class[]{Dialog.class,int.class},original[0],generation[0]),"Late response from previous reader rejected");
  }catch(Throwable e){error[0]=e;}});
  if(error[0]!=null)throw new Exception(error[0]);hold.countDown();
  boolean complete=false;for(int i=0;i<40;i++){Thread.sleep(1000);final boolean[] done={false};ui(()->{try{Dialog d=(Dialog)field("projectReader");String body=text(d.getWindow().getDecorView());done[0]=!body.contains("Loading release notes from GitHub");}catch(Exception e){error[0]=e;}});if(error[0]!=null)throw new Exception(error[0]);if(done[0]){complete=true;break;}}
  check(complete,"Loading resolves to text or recoverable error");
  ui(()->{try{Dialog d=(Dialog)field("projectReader");check(d!=original[0]&&d.isShowing(),"Completion stays in active reader");d.dismiss();}catch(Throwable e){error[0]=e;}});
  waitForIdleSync();if(error[0]!=null)throw new Exception(error[0]);check(field("projectReader")==null,"One dismissal closes completed reader");
  ui(()->{try{read(false,"suchhodler-boop/mg4-install-reader-test-missing");}catch(Throwable e){error[0]=e;}});
  boolean failed=false;for(int i=0;i<40;i++){Thread.sleep(1000);final boolean[] ready={false};ui(()->{try{Dialog d=(Dialog)field("projectReader");ready[0]=text(d.getWindow().getDecorView()).contains("Couldn’t load README");}catch(Throwable e){error[0]=e;}});if(error[0]!=null)throw new Exception(error[0]);if(ready[0]){failed=true;break;}}
  check(failed,"Fetch error replaces loading view");
  ui(()->{try{Dialog d=(Dialog)field("projectReader");View retry=find(d.getWindow().getDecorView(),"Retry");check(retry!=null,"Error has Retry action");retry.performClick();Dialog next=(Dialog)field("projectReader");check(next!=d&&next.isShowing(),"Retry opens exactly one active reader");check(text(next.getWindow().getDecorView()).contains("Loading README"),"Retry shows loading again");next.dismiss();}catch(Throwable e){error[0]=e;}});
  waitForIdleSync();if(error[0]!=null)throw new Exception(error[0]);check(field("projectReader")==null,"Retry can be cancelled");
  result.putString("stream","PASS: "+checks+" reader loading, repeated-tap, cancellation, stale-result and retry checks.\n");finish(-1,result);
 }catch(Throwable e){result.putString("stream","FAIL: "+e.toString());finish(1,result);}finally{hold.countDown();}}
}
