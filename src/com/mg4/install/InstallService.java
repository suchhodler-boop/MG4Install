// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.io.*;
import java.net.*;
import java.security.*;

/** PackageInstaller flow adapted from MG4Dash RadioDashUpdater (GPL-3.0).
 * Retains INSTALL_INTERNAL for SAIC firmware. Android still verifies APK signatures.
 */
public class InstallService extends Service {
    static volatile boolean busy=false;
    volatile boolean cancelled=false;
    static final String CHANNEL="installs";
    public IBinder onBind(Intent i) { return null; }
    public void onCreate() { super.onCreate(); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel(CHANNEL,"App installation",NotificationManager.IMPORTANCE_LOW)); }
    Notification notification(String text) {
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("MG4 Install").setContentText(text).setContentIntent(open).setOngoing(busy).build();
    }
    static void report(Context c,String text) {
        c.getSharedPreferences("state",0).edit().putString("status",text).apply();
        c.sendBroadcast(new Intent(c.getPackageName()+".STATUS").setPackage(c.getPackageName()));
    }
    void progress(String s) { report(this,s); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,notification(s)); }
    public int onStartCommand(Intent intent,int flags,int id) {
        startForeground(1,notification("Preparing download…"));
        if(intent==null) { stopSelf(); return START_NOT_STICKY; }
        if("cancel".equals(intent.getAction())) { cancelled=true; if(!busy) { stopForeground(true);stopSelf(); } return START_NOT_STICKY; }
        if(busy) return START_NOT_STICKY;
        busy=true; cancelled=false;
        new Thread(()->runInstall(intent),"apk-download").start();
        return START_NOT_STICKY;
    }
    void runInstall(Intent intent) {
        File apk=new File(getCacheDir(),"pending.apk"); int sessionId=-1; boolean committed=false;
        try {
            if(!intent.getBooleanExtra("confirmedInstall",false))throw new IOException("Confirm installation first");
            long updateCode=intent.getLongExtra("selfUpdateCode",0);
            if(updateCode>0){String expectedDigest=intent.getStringExtra("digest");if(expectedDigest==null||!expectedDigest.startsWith("sha256:")||!UpdatePolicy.digest(expectedDigest.substring(7)))throw new IOException("Self-update checksum is required");}
            String title=intent.getStringExtra("name"); long expected=intent.getLongExtra("size",0);
            if(expected>1024L*1024*1024) throw new IOException("APK exceeds the 1 GB download limit");
            if(expected>0&&getCacheDir().getUsableSpace()<expected*2+32*1024*1024) throw new IOException("Not enough free storage");
            HttpURLConnection c=Network.connect(intent.getStringExtra("url"));
            MessageDigest hash=MessageDigest.getInstance("SHA-256"); long total=0; long last=0;
            try(InputStream in=c.getInputStream(); OutputStream out=new FileOutputStream(apk)) {
                byte[] b=new byte[65536]; int n;
                while((n=in.read(b))!=-1) {
                    if(cancelled) throw new IOException("Download cancelled");
                    total+=n; if(total>1024L*1024*1024) throw new IOException("APK exceeds the 1 GB download limit");
                    out.write(b,0,n); hash.update(b,0,n);
                    if(System.currentTimeMillis()-last>400) { progress("Downloading "+title+" · "+(expected>0?(total*100/expected)+"%":(total/1048576)+" MB"));last=System.currentTimeMillis(); }
                }
            } finally { c.disconnect(); }
            if(cancelled) throw new IOException("Download cancelled");
            if(expected>0&&total!=expected) throw new IOException("Incomplete download. Please retry.");
            String digest=intent.getStringExtra("digest");
            if(digest!=null&&digest.startsWith("sha256:")) {
                StringBuilder actual=new StringBuilder(); for(byte b:hash.digest()) actual.append(String.format("%02x",b&255));
                if(!actual.toString().equalsIgnoreCase(digest.substring(7))) throw new IOException("APK checksum mismatch. Installation blocked.");
            }
            PackageInfo info=getPackageManager().getPackageArchiveInfo(apk.getPath(),0);
            if(info==null) throw new IOException("This download is not a valid Android APK");
            if(info.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT) throw new IOException("This APK requires a newer Android version");
            if(updateCode>0)SelfUpdater.validate(this,apk,updateCode);
            PackageInstaller pi=getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(info.packageName); params.setSize(apk.length());
            params.setInstallLocation(PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY);
            try { java.lang.reflect.Field f=params.getClass().getField("installFlags"); f.setInt(params,f.getInt(params)|0x10); } catch(Exception ignored) { /* ordinary Android uses standard installer */ }
            sessionId=pi.createSession(params);
            try(PackageInstaller.Session session=pi.openSession(sessionId)) {
                try(InputStream in=new FileInputStream(apk);OutputStream out=session.openWrite("base.apk",0,apk.length())) {
                    byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1) out.write(b,0,n);session.fsync(out);
                }
                int mutable=Build.VERSION.SDK_INT>=31?0x02000000:0;
                PendingIntent result=PendingIntent.getBroadcast(this,sessionId,new Intent(this,InstallResult.class),PendingIntent.FLAG_UPDATE_CURRENT|mutable);
                progress("Installing "+title+"…");
                session.commit(result.getIntentSender()); committed=true;
            }
        } catch(Exception e) { report(this,"Installation failed · "+(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage())); }
        finally {
            if(sessionId>=0&&!committed) try { getPackageManager().getPackageInstaller().abandonSession(sessionId); } catch(Exception ignored) {}
            apk.delete(); busy=false;stopForeground(true);stopSelf();
        }
    }
}
