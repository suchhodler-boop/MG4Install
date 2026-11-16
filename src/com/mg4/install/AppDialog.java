package com.mg4.install;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
/** Explicit colors and sizing: OEM alert-dialog themes are not consistent. */
public final class AppDialog {
 final Activity activity;String title="",message;View content;String[] items;int selected=-1;
 DialogInterface.OnClickListener itemAction;final List<String> labels=new ArrayList<>();final List<DialogInterface.OnClickListener> actions=new ArrayList<>();
 public AppDialog(Activity a){activity=a;}
 public AppDialog setTitle(String s){title=s;return this;}
 public AppDialog setMessage(String s){message=s;return this;}
 public AppDialog setView(View v){content=v;return this;}
 public AppDialog setItems(String[] s,DialogInterface.OnClickListener action){items=s;itemAction=action;return this;}
 public AppDialog setSingleChoiceItems(String[] s,int n,DialogInterface.OnClickListener action){selected=n;return setItems(s,action);}
 public AppDialog setPositiveButton(String s,DialogInterface.OnClickListener action){return add(s,action);}
 public AppDialog setNegativeButton(String s,DialogInterface.OnClickListener action){return add(s,action);}
 public AppDialog setNeutralButton(String s,DialogInterface.OnClickListener action){return add(s,action);}
 AppDialog add(String s,DialogInterface.OnClickListener action){labels.add(s);actions.add(action);return this;}
 int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
 TextView text(String s,int size){TextView t=new TextView(activity);t.setText(s);t.setTextColor(0xfff0f4f6);t.setTextSize(size);t.setPadding(dp(18),dp(14),dp(18),dp(14));return t;}
 public Dialog show(){
  Dialog d=new Dialog(activity,android.R.style.Theme_Material_NoActionBar);d.requestWindowFeature(Window.FEATURE_NO_TITLE);
  LinearLayout box=new LinearLayout(activity);box.setOrientation(1);box.setPadding(dp(14),dp(10),dp(14),dp(10));box.setBackgroundColor(0xff16191f);
  TextView heading=text(title,28);heading.setTypeface(Typeface.DEFAULT_BOLD);box.addView(heading);
  View body=content;
  if(body==null){LinearLayout list=new LinearLayout(activity);list.setOrientation(1);if(message!=null){TextView m=text(message,22);m.setTextIsSelectable(true);list.addView(m);}if(items!=null)for(int i=0;i<items.length;i++){final int index=i;TextView row=text((selected==i?"✓  ":"")+items[i],22);row.setMinHeight(dp(70));row.setBackgroundColor(0xff252c34);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);list.addView(row,lp);row.setOnClickListener(v->{d.dismiss();if(itemAction!=null)itemAction.onClick(d,index);});}body=list;}
  if(body instanceof ScrollView)box.addView(body,new LinearLayout.LayoutParams(-1,0,1));else{ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(false);scroll.addView(body);box.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));}
  LinearLayout buttons=new LinearLayout(activity);buttons.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
  for(int i=labels.size()-1;i>=0;i--){final int index=i;TextView b=text(labels.get(i),22);b.setTextColor(i==0?0xff45d5a6:0xfff0f4f6);b.setTypeface(Typeface.DEFAULT_BOLD);b.setMinHeight(dp(64));b.setGravity(Gravity.CENTER);buttons.addView(b);b.setOnClickListener(v->{d.dismiss();if(actions.get(index)!=null)actions.get(index).onClick(d,index);});}
  box.addView(buttons);d.setContentView(box);d.setOnDismissListener(v->{if(activity instanceof MainActivity)((MainActivity)activity).applyFullscreen();});
  Window w=d.getWindow();w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);WindowManager.LayoutParams a=w.getAttributes();a.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;w.setAttributes(a);w.getDecorView().setSystemUiVisibility(5894);d.show();
  android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);w.setLayout(Math.min(metrics.widthPixels-dp(48),dp(1400)),metrics.heightPixels-dp(48));return d;
 }
}
