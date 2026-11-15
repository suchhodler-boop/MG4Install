package com.mg4.install;
import android.os.Build;
public final class DeviceFirmware {
 public static String raw() {
  String v=prop("ro.build.mt2712.version");
  if(v.isEmpty())v=prop("ro.build.version.incremental");
  return v.isEmpty()?Build.VERSION.INCREMENTAL:v;
 }
 private static String prop(String key) {try {String v=(String)Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class).invoke(null,key,"");return v==null||v.equals("0")?"":v.trim();}catch(Exception e){return "";}}
}
