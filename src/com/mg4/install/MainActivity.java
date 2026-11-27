// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final int BG=0xff0a0c0f,PANEL=0xff16191f,PANEL2=0xff1e222a,TEXT=0xfff0f4f6,MUTED=0xff949ea8,ACCENT=0xff45d5a6;
    final ExecutorService worker=Executors.newSingleThreadExecutor();
    final ExecutorService metricsWorker=Executors.newSingleThreadExecutor();
    final Map<String,Ranking.Stats> stats=new HashMap<>();
    boolean metricsLoading=false; int sortMode=0,firmware=0; String detected="";TextView sortButton,firmwareButton,updateButton; boolean checkingUpdate=false,visible=false; SelfUpdater.Release availableUpdate;
    final List<FeedParser.Repo> repos=new ArrayList<>();
    LinearLayout root,rows; TextView sync,status,count; EditText search; boolean loading=false,beta=false; String query="";
    Dialog projectReader; Future<?> projectReadTask; int projectReadGeneration=0;
    Network.Asset pending; FeedParser.Repo pendingRepo;
    BroadcastReceiver updates=new BroadcastReceiver(){ public void onReceive(Context c,Intent i){showStatus();String message=getSharedPreferences("state",0).getString("status","");if(message.startsWith("Installation failed")||message.startsWith("Installed successfully")||message.startsWith("Unable to open"))dialog("Installation result",message);} };
    int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xff303b42);return d;}
    TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(Math.max(20,Math.round(size*1.25f)));v.setTextColor(color);v.setFontFeatureSettings("kern");return v;}
    TextView button(String label,boolean primary,Runnable action){TextView v=text(label,18,primary?BG:TEXT);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.create("sans-serif-medium",0));v.setPadding(dp(17),0,dp(17),0);v.setMinHeight(dp(64));v.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x3345d5a6),shape(primary?ACCENT:PANEL2,9),null));v.setOnClickListener(w->action.run());return v;}
    LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void space(LinearLayout l,int n){l.addView(new View(this),new LinearLayout.LayoutParams(dp(n),1));}
    public void onCreate(Bundle b){super.onCreate(b);beta=getPreferences(0).getBoolean("beta",false);applyFullscreen();detected=DeviceFirmware.raw();firmware=getPreferences(0).getInt("firmwareOverride",Firmware.parse(detected));sortMode=getPreferences(0).getInt("sort",0);readStats();build();registerReceiver(updates,new IntentFilter(getPackageName()+".STATUS"));readCache();refresh();checkForUpdates(false);}
    public void onConfigurationChanged(android.content.res.Configuration c){super.onConfigurationChanged(c);applyFullscreen();render();}
    public void onResume(){super.onResume();visible=true;setDockVisible(false);applyFullscreen();if(status!=null)showStatus();}
    @Override public void onPause(){visible=false;super.onPause();}
    void setDockVisible(boolean visible){
        if(getPackageName().endsWith(".emulator"))return;
        try{sendBroadcast(new Intent("com.saicmotor.hmi.engineermode").setPackage("com.android.systemui").putExtra("isVisibility",visible));}catch(Exception ignored){}
    }
    @Override public void onStop(){setDockVisible(true);super.onStop();}
    void applyFullscreen(){
        Window window=getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Android 9+ otherwise reserves a landscape gutter for a display cutout.
        WindowManager.LayoutParams attributes=window.getAttributes();
        attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        window.setAttributes(attributes);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    @Override public void onWindowFocusChanged(boolean focused){
        super.onWindowFocusChanged(focused);
        if(focused){setDockVisible(false);applyFullscreen();}
    }
    public void onDestroy(){if(projectReader!=null)projectReader.dismiss();unregisterReceiver(updates);worker.shutdownNow();metricsWorker.shutdownNow();super.onDestroy();}
    void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(28),dp(20),dp(28),dp(14));root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=horizontal();
        TextView logo=text("MG4",25,ACCENT);logo.setTypeface(Typeface.create("sans-serif-black",0));header.addView(logo);space(header,10);
        TextView title=text("INSTALL",25,TEXT);title.setLetterSpacing(.12f);title.setTypeface(Typeface.create("sans-serif-light",0));header.addView(title);
        View stretch=new View(this);header.addView(stretch,new LinearLayout.LayoutParams(0,1,1));
        firmwareButton=button(firmwareText(),false,()->chooseFirmware());header.addView(firmwareButton);space(header,10);
        sortButton=button(sortText(),false,()->chooseSort());header.addView(sortButton);space(header,10);
        header.addView(button("↻  Refresh",false,()->refresh()));space(header,10);updateButton=button("Updates",false,()->updateSettings());header.addView(updateButton);space(header,10);header.addView(button("About",false,()->about()));space(header,10);header.addView(button("×",false,()->finish()));
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(64)));
        View line=new View(this);line.setBackgroundColor(0xff303c41);LinearLayout.LayoutParams rule=new LinearLayout.LayoutParams(-1,dp(1));rule.topMargin=dp(16);rule.bottomMargin=dp(18);root.addView(line,rule);
        LinearLayout tools=horizontal();LinearLayout heading=new LinearLayout(this);heading.setOrientation(1);
        TextView h=text("Your MG4. More possibilities.",23,TEXT);h.setTypeface(Typeface.create("sans-serif-medium",0));heading.addView(h);
        sync=text("Connecting to hojnikb / mg4-apps…",12,MUTED);sync.setPadding(0,dp(5),0,0);heading.addView(sync);tools.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(22);search.setTextColor(TEXT);search.setHintTextColor(MUTED);search.setHint("Search apps or developers");search.setPadding(dp(16),0,dp(12),0);search.setBackground(shape(PANEL,9));tools.addView(search,new LinearLayout.LayoutParams(dp(340),dp(64)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){query=s.toString().toLowerCase(Locale.ROOT);render();}public void afterTextChanged(Editable e){}});
        root.addView(tools);count=text("COMMUNITY APPS",11,ACCENT);count.setLetterSpacing(.15f);count.setPadding(0,dp(19),0,dp(11));root.addView(count);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);rows=new LinearLayout(this);rows.setOrientation(1);scroll.addView(rows);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=horizontal();status=text("Tap Install to download an app.",12,MUTED);status.setMaxLines(2);footer.addView(status,new LinearLayout.LayoutParams(0,dp(60),1));status.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(button("Cancel download",false,()->{if(InstallService.busy)startService(new Intent(this,InstallService.class).setAction("cancel"));}));root.addView(footer);
    }
    void showStatus(){status.setText(getSharedPreferences("state",0).getString("status","Tap Install to download an app."));}
    void render(){
        if(rows==null)return;rows.removeAllViews();
        List<FeedParser.Repo> filtered=new ArrayList<>();for(FeedParser.Repo r:repos)if((r.slug+" "+r.description).toLowerCase(Locale.ROOT).contains(query))filtered.add(r);
        Collections.sort(filtered,Ranking.order(stats,sortMode,firmware,System.currentTimeMillis()));
        count.setText("COMMUNITY APPS   /   "+filtered.size());
        int width=getResources().getConfiguration().screenWidthDp;int columns=Math.max(1,Math.min(3,(width-42)/430));
        for(int group=0;group<4;group++){
            List<FeedParser.Repo> members=new ArrayList<>();for(FeedParser.Repo r:filtered)if(projectState(r)==group)members.add(r);
            if(members.isEmpty())continue;
            TextView section=text(Compatibility.section(group)+"  /  "+members.size(),13,group==0?ACCENT:0xffe6b66e);section.setPadding(0,dp(12),0,dp(12));rows.addView(section);
            LinearLayout row=null;
            for(int i=0;i<members.size();i++){
                if(i%columns==0){row=horizontal();row.setGravity(Gravity.TOP);rows.addView(row,new LinearLayout.LayoutParams(-1,-2));}
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(320),1);lp.rightMargin=dp(i%columns==columns-1?0:12);lp.bottomMargin=dp(12);row.addView(card(members.get(i)),lp);
            }
            if(row!=null&&members.size()%columns!=0)for(int i=members.size()%columns;i<columns;i++){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(320),1);lp.rightMargin=dp(i==columns-1?0:12);row.addView(new View(this),lp);}
        }
        if(filtered.isEmpty()){TextView empty=text(repos.isEmpty()?"Your app collection will appear here.\nRefresh to connect to GitHub.":"No matching apps. Try another search.",18,MUTED);empty.setGravity(Gravity.CENTER);rows.addView(empty,new LinearLayout.LayoutParams(-1,dp(180)));}
    }
    LinearLayout card(FeedParser.Repo repo){
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(17),dp(15),dp(17),dp(13));card.setBackground(shape(PANEL,12));
        LinearLayout top=horizontal();String shortName=repo.name().replaceAll("(?i)^mg4[-_]?","");if(shortName.isEmpty())shortName="MG";
        TextView monogram=text(shortName.substring(0,Math.min(2,shortName.length())).toUpperCase(Locale.ROOT),16,ACCENT);monogram.setGravity(Gravity.CENTER);monogram.setTypeface(Typeface.DEFAULT_BOLD);monogram.setBackground(shape(0xff17352e,10));top.addView(monogram,new LinearLayout.LayoutParams(dp(40),dp(40)));space(top,10);
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(1);TextView name=text(repo.name(),16,TEXT);name.setTypeface(Typeface.create("sans-serif-medium",0));name.setMaxLines(1);name.setEllipsize(TextUtils.TruncateAt.END);titles.addView(name);TextView author=text(repo.slug.split("/")[0],11,MUTED);author.setSingleLine(true);titles.addView(author);top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));card.addView(top);
        TextView desc=text(android.text.Html.fromHtml(repo.description,0).toString(),13,MUTED);desc.setMaxLines(3);desc.setEllipsize(TextUtils.TruncateAt.END);desc.setLineSpacing(dp(2),1);desc.setPadding(0,dp(12),0,dp(8));card.addView(desc,new LinearLayout.LayoutParams(-1,0,1));
        int state=projectState(repo);
        TextView compatibility=text(Compatibility.label(state)+"  ⓘ",11,state==0?ACCENT:0xffe6b66e);compatibility.setMinHeight(dp(36));compatibility.setGravity(Gravity.CENTER_VERTICAL);compatibility.setOnClickListener(v->evidence(repo));card.addView(compatibility);
        Ranking.Stats metric=stats.get(repo.slug);String metricText="APK downloads: not loaded";
        if(metric!=null)metricText=String.format(Locale.US,"%,d%s APK downloads",metric.downloads,metric.partial?"+":"")+(metric.latest>0?" · "+android.text.format.DateFormat.format("MMM yyyy",metric.latest):" · no stable release");
        TextView downloads=text(metricText,11,MUTED);downloads.setSingleLine(true);downloads.setPadding(0,dp(4),0,dp(4));card.addView(downloads);
        LinearLayout bottom=horizontal();TextView source=text("App details",12,MUTED);source.setMinHeight(dp(60));source.setGravity(Gravity.CENTER_VERTICAL);source.setOnClickListener(v->open(repo));bottom.addView(source,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView install=button(state==3?"↗  Source":"↓  Install",true,()->{if(state==3)open(repo);else resolve(repo);});install.setContentDescription("Install "+repo.name());bottom.addView(install,new LinearLayout.LayoutParams(dp(150),dp(60)));card.addView(bottom);return card;
    }
    int projectState(FeedParser.Repo r){Ranking.Stats m=stats.get(r.slug);return Compatibility.project(r.slug,firmware,m==null?null:m.latestTag);}
    void evidence(FeedParser.Repo repo){Compatibility.Entry e=Compatibility.get(repo.slug);new AppDialog(this).setTitle(Compatibility.label(projectState(repo))).setMessage(e.note+"\n\nReview: 16 Nov 2026, SWI165. Reviewed stable APK release: "+(e.tag.isEmpty()?"none":e.tag)+". New releases/replaced assets are unverified. Documented support does not establish every feature, revision or hardware combination.").setPositiveButton("README",(d,w)->readProject(repo,false,true)).setNeutralButton("Release notes",(d,w)->readProject(repo,true,true)).setNegativeButton("Close",null).show();}
    void open(FeedParser.Repo r){
        Compatibility.Entry e=Compatibility.get(r.slug);
        new AppDialog(this).setTitle(r.name()).setMessage(r.description+"\n\n"+Compatibility.label(projectState(r))+"\n\n"+e.note)
            .setPositiveButton("Read README",(d,w)->readProject(r,false,false)).setNeutralButton("Release notes",(d,w)->readProject(r,true,false)).setNegativeButton("Close",null).show();
    }
    void readProject(FeedParser.Repo r,boolean releases,boolean reviewed){
        // Keep one reader from the first tap through loading, success or failure.
        if(projectReader!=null&&projectReader.isShowing())return;
        if(isFinishing()||isDestroyed())return;
        final int generation=++projectReadGeneration;
        final String kind=releases?"release notes":"README";
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(18),dp(18),dp(18),dp(18));
        LinearLayout loadingRow=horizontal();ProgressBar spinner=new ProgressBar(this);spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ACCENT));loadingRow.addView(spinner,new LinearLayout.LayoutParams(dp(48),dp(48)));space(loadingRow,20);
        loadingRow.addView(text("Loading "+kind+" from GitHub…",20,TEXT));content.addView(loadingRow);
        TextView hint=text("Please wait. You can close this window to cancel.",18,MUTED);hint.setPadding(0,dp(18),0,0);content.addView(hint);
        final Dialog reader=new AppDialog(this).setTitle(r.name()+(releases?" · Release notes":" · README")).setView(content).setPositiveButton("Close",null).show();
        projectReader=reader;
        reader.setOnDismissListener(d->{
            if(projectReader==reader){projectReader=null;++projectReadGeneration;if(projectReadTask!=null){projectReadTask.cancel(true);projectReadTask=null;}}
            if(!isFinishing()&&!isDestroyed())applyFullscreen();
        });
        projectReadTask=worker.submit(()->{try{
            String result;
            if(releases){JSONArray all=new JSONArray(Network.get("https://api.github.com/repos/"+r.slug+"/releases?per_page=10"));StringBuilder out=new StringBuilder();for(int i=0;i<all.length();i++){JSONObject release=all.getJSONObject(i);out.append(release.optString("tag_name")).append("\n\n").append(release.optString("body","No notes published.")).append("\n\n────────\n\n");}result=out.length()==0?"No published release notes.":out.toString();}
            else {String tag=reviewed?Compatibility.get(r.slug).tag:"";JSONObject readme=new JSONObject(Network.get("https://api.github.com/repos/"+r.slug+"/readme"+(tag.isEmpty()?"":"?ref="+java.net.URLEncoder.encode(tag,"UTF-8"))));result=new String(android.util.Base64.decode(readme.getString("content"),android.util.Base64.DEFAULT),"UTF-8");}
            final String body=result.length()>200000?result.substring(0,200000)+"\n[Text truncated]":result;
            runOnUiThread(()->{
                if(!currentProjectRead(reader,generation))return;
                content.removeAllViews();TextView document=text(body,18,TEXT);document.setTextIsSelectable(true);content.addView(document);status.setText("Project information loaded");
            });
        }catch(Exception e){runOnUiThread(()->{
            if(!currentProjectRead(reader,generation))return;
            content.removeAllViews();content.addView(text("Couldn’t load "+kind+".\n\n"+(e.getMessage()==null?"Check your connection and try again.":e.getMessage()),18,TEXT));
            content.addView(button("Retry",true,()->{reader.dismiss();readProject(r,releases,reviewed);}));
        });}});
    }
    boolean currentProjectRead(Dialog reader,int generation){
        return !isFinishing()&&!isDestroyed()&&projectReader==reader&&reader.isShowing()&&projectReadGeneration==generation;
    }
    void refresh(){if(loading)return;loading=true;sync.setText("Syncing live GitHub list…");worker.execute(()->{try{List<FeedParser.Repo> fresh=Network.feed();JSONArray a=new JSONArray();for(FeedParser.Repo r:fresh){JSONObject o=new JSONObject();o.put("slug",r.slug);o.put("description",r.description);a.put(o);}getSharedPreferences("feed",0).edit().putString("data",a.toString()).putLong("time",System.currentTimeMillis()).apply();runOnUiThread(()->{repos.clear();repos.addAll(fresh);loading=false;sync.setText("● Live from GitHub  ·  "+fresh.size()+" projects  ·  just updated");render();loadStats(new ArrayList<>(fresh));});}catch(Exception e){runOnUiThread(()->{loading=false;sync.setText("Offline / sync unavailable · showing saved feed");status.setText(e.getMessage());});}});}
    void readCache(){try{android.content.SharedPreferences p=getSharedPreferences("feed",0);JSONArray a=new JSONArray(p.getString("data","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);repos.add(new FeedParser.Repo(o.getString("slug"),o.getString("description")));}List<FeedParser.Repo> merged=FeedSources.merge(repos);repos.clear();repos.addAll(merged);if(!repos.isEmpty())sync.setText("Saved feed · "+android.text.format.DateFormat.format("dd MMM, HH:mm",p.getLong("time",0)));render();}catch(Exception ignored){}}
    void resolve(FeedParser.Repo repo){

        if(InstallService.busy){dialog("Download in progress","Wait for the current APK or cancel its download first.");return;}
        status.setText("Finding APKs for "+repo.name()+"…");
        worker.execute(()->{try{List<Network.Asset> assets=Network.assets(repo.slug,beta);runOnUiThread(()->{
            if(isFinishing()||isDestroyed())return;
            if(assets.isEmpty()){status.setText("No published APK found for "+repo.name());new AppDialog(this).setTitle("No APK available").setMessage("This project has no APK in its eligible releases or repository. See the project’s instructions."+(beta?"":" You can include prereleases in About.")).setPositiveButton("View project",(d,w)->open(repo)).setNegativeButton("Close",null).show();return;}
            status.setText("Choose an APK for "+repo.name());for(Network.Asset asset:assets)asset.repo=repo.slug;if(assets.size()==1){confirm(repo,assets.get(0));return;}String[] labels=new String[assets.size()];for(int i=0;i<labels.length;i++){Network.Asset a=assets.get(i);labels[i]="↓  Select APK: "+a.label()+" · "+Compatibility.label(Compatibility.asset(repo.slug,firmware,a.version,a.id));}
            new AppDialog(this).setTitle(repo.name()+" · choose APK").setItems(labels,(d,index)->confirm(repo,assets.get(index))).setNegativeButton("Cancel",null).show();
        });}catch(Exception e){runOnUiThread(()->{status.setText(e.getMessage());dialog("Couldn’t find APKs",e.getMessage());});}});
    }
    void confirm(FeedParser.Repo repo,Network.Asset asset){
        int state=Compatibility.asset(repo.slug,firmware,asset.version,asset.id);
        Compatibility.Entry entry=Compatibility.get(repo.slug);
        String warning=state==Compatibility.CONFIRMED?"The author documents SWI165 support. Review the limitations below.":state==Compatibility.UNSUPPORTED?"WARNING: the author restricts this release to a different firmware. It may not work on your car.":"Compatibility has not been verified for your firmware. This app may not work correctly.";
        new AppDialog(this).setTitle("Install "+repo.name()+"?").setMessage(asset.label()+"\n\nYour firmware: "+(firmware==0?"unknown":"SWI"+firmware)+"\n\n"+warning+"\n\n"+entry.note+"\n\nContinuing downloads the APK and sends it directly to Android’s installer.")
            .setPositiveButton(state==Compatibility.CONFIRMED?"Download & install":"Install anyway",(d,w)->{asset.reviewedFirmware=firmware;begin(asset);})
            .setNegativeButton("Cancel",null).show();
    }
    void begin(Network.Asset a){
        if(InstallService.busy){dialog("Download in progress","Wait for the current download to finish.");return;}
        if(checkSelfPermission("android.permission.INSTALL_PACKAGES")!=android.content.pm.PackageManager.PERMISSION_GRANTED&&!getPackageManager().canRequestPackageInstalls()){
            pending=a;new AppDialog(this).setTitle("Allow app installation").setMessage("Enable permission for MG4 Install, then return here. If Android restarts the app, select the APK again.").setPositiveButton("Open settings",(d,w)->startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())),10)).setNegativeButton("Cancel",(d,w)->pending=null).show();return;
        }
        startForegroundService(new Intent(this,InstallService.class).putExtra("selfUpdateCode",a.selfUpdateCode).putExtra("confirmedInstall",true).putExtra("firmware",firmware).putExtra("reviewed",a.reviewedFirmware).putExtra("repo",a.repo).putExtra("version",a.version).putExtra("assetId",a.id).putExtra("url",a.url).putExtra("name",a.name).putExtra("size",a.size).putExtra("digest",a.digest));status.setText("Starting download…");
    }
    protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==10&&pending!=null){Network.Asset a=pending;pending=null;if(getPackageManager().canRequestPackageInstalls())begin(a);else status.setText("Installation permission was not granted.");}}
    void dialog(String title,String message){if(!isFinishing()&&!isDestroyed())new AppDialog(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show();}
    String firmwareText(){return firmware==0?"Firmware: unknown":"SWI"+firmware+(getPreferences(0).contains("firmwareOverride")?" · manual":" · auto");}
    String sortText(){return new String[]{"Recommended ▾","Most downloaded ▾","Recently updated ▾"}[sortMode];}
    void chooseSort(){new AppDialog(this).setTitle("Order apps").setSingleChoiceItems(new String[]{"Recommended: usefulness + downloads + freshness","Most downloaded: GitHub APK assets","Recently updated: stable APK releases"},sortMode,(d,n)->{sortMode=n;getPreferences(0).edit().putInt("sort",n).apply();sortButton.setText(sortText());render();d.dismiss();}).setNegativeButton("Close",null).show();}
    void chooseFirmware(){
        final int[] versions={0,68,69,131,132,133,165,173};
        String[] choices={"Auto-detect again","SWI68","SWI69","SWI131","SWI132","SWI133","SWI165","SWI173"};
        new AppDialog(this).setTitle("Firmware · detected: "+detected).setItems(choices,(d,n)->{if(n==0){getPreferences(0).edit().remove("firmwareOverride").apply();detected=DeviceFirmware.raw();firmware=Firmware.parse(detected);}else{firmware=versions[n];getPreferences(0).edit().putInt("firmwareOverride",firmware).apply();}firmwareButton.setText(firmwareText());render();if(firmware==0)dialog("Firmware not detected","Check your head unit’s system information and choose the SWI version manually. You can still continue after the compatibility warning.");}).setNegativeButton("Close",null).show();
    }
    void readStats(){try{JSONObject saved=new JSONObject(getSharedPreferences("popularity",0).getString("data","{}"));Iterator<String> keys=saved.keys();while(keys.hasNext()){String key=keys.next();stats.put(key,Popularity.decode(saved.getJSONObject(key)));}}catch(Exception ignored){}}
    void loadStats(List<FeedParser.Repo> snapshot){
        if(metricsLoading)return;metricsLoading=true;final Map<String,Ranking.Stats> cached=new HashMap<>(stats);
        metricsWorker.execute(()->{
            boolean failed=false;String reason="";
            for(FeedParser.Repo r:snapshot){
                if(Thread.currentThread().isInterrupted())break;
                Ranking.Stats old=cached.get(r.slug);if(old!=null&&!old.latestTag.isEmpty()&&System.currentTimeMillis()-old.checked<12*3600000L)continue;
                try{Ranking.Stats fresh=Popularity.fetch(r.slug);cached.put(r.slug,fresh);}
                catch(Exception e){failed=true;reason=e.getMessage();if(reason!=null&&reason.contains("limit"))break;}
            }
            try{JSONObject saved=new JSONObject();for(Map.Entry<String,Ranking.Stats> e:cached.entrySet())saved.put(e.getKey(),Popularity.encode(e.getValue()));getSharedPreferences("popularity",0).edit().putString("data",saved.toString()).apply();}catch(Exception ignored){}
            final boolean partial=failed;runOnUiThread(()->{metricsLoading=false;if(isDestroyed())return;stats.clear();stats.putAll(cached);render();sync.setText(partial?"GitHub feed · some download stats unavailable; saved counts retained":"GitHub feed · APK downloads cached for 12 hours");});
        });
    }
    void checkForUpdates(boolean manual){
        if(checkingUpdate)return;
        android.content.SharedPreferences prefs=getSharedPreferences("updates",0);
        if(!manual&&(!prefs.getBoolean("autoCheck",true)||System.currentTimeMillis()-prefs.getLong("checked",0)<12*3600000L))return;
        checkingUpdate=true;if(manual)status.setText("Checking MG4 Install updates…");
        worker.execute(()->{try{
            SelfUpdater.Release release=SelfUpdater.check(this);
            prefs.edit().putLong("checked",System.currentTimeMillis()).apply();
            runOnUiThread(()->{checkingUpdate=false;if(isDestroyed()||isFinishing())return;availableUpdate=release;updateButton.setText(release==null?"Updates":"Update available");
                if(release==null){if(manual&&visible)dialog("MG4 Install is up to date","Installed version: "+SelfUpdater.version(this));return;}
                if(!visible)return;
                if(!manual&&prefs.getBoolean("autoInstall",false)&&!InstallService.busy){begin(release.asset);return;}
                showUpdate(release);
            });
        }catch(Exception e){runOnUiThread(()->{checkingUpdate=false;if(manual&&visible&&!isDestroyed())dialog("Couldn’t check for updates",e.getMessage());});}});
    }
    void showUpdate(SelfUpdater.Release release){
        new AppDialog(this).setTitle("MG4 Install "+release.version+" available").setMessage("Installed: "+SelfUpdater.version(this)+"\n\n"+release.notes+"\n\nThe APK is verified and installed directly. The app may close during its update; reopen it afterwards.")
            .setPositiveButton("Update now",(d,w)->begin(release.asset)).setNegativeButton("Later",null).show();
    }
    void updateSettings(){
        android.content.SharedPreferences prefs=getSharedPreferences("updates",0);
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(12),dp(20),dp(12));
        content.addView(text("Installed: "+SelfUpdater.version(this)+"\nStable releases from suchhodler-boop/MG4Install. Checks run when the app opens, at most once every 12 hours. Use Check now at any time.\n\nAutomatic installation applies only to MG4 Install itself. Android may ask for confirmation. Use while parked.",18,TEXT));
        CheckBox auto=new CheckBox(this);auto.setText("Check automatically when the app opens");auto.setTextSize(22);auto.setMinHeight(dp(64));auto.setTextColor(TEXT);auto.setChecked(prefs.getBoolean("autoCheck",true));content.addView(auto);
        CheckBox install=new CheckBox(this);install.setText("Automatically download and install MG4 Install updates");install.setTextSize(22);install.setMinHeight(dp(64));install.setTextColor(TEXT);install.setChecked(prefs.getBoolean("autoInstall",false));content.addView(install);
        auto.setOnCheckedChangeListener((b,on)->{prefs.edit().putBoolean("autoCheck",on).apply();if(!on)install.setChecked(false);});
        install.setOnCheckedChangeListener((b,on)->{prefs.edit().putBoolean("autoInstall",on).apply();if(on)auto.setChecked(true);});
        AppDialog panel=new AppDialog(this).setTitle("MG4 Install updates").setView(content).setPositiveButton("Check now",(d,w)->checkForUpdates(true)).setNegativeButton("Close",null);
        if(availableUpdate!=null)panel.setNeutralButton("Update now",(d,w)->showUpdate(availableUpdate));panel.show();
    }
    void showLicense(){
        try(java.io.InputStream in=getResources().openRawResource(getResources().getIdentifier("license","raw",getPackageName()));java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
            new AppDialog(this).setTitle("GNU GPL version 3").setMessage(out.toString("UTF-8")).setPositiveButton("Close",null).show();
        }catch(Exception e){dialog("License","GNU GPL version 3. Full license and source: github.com/suchhodler-boop/MG4Install");}
    }
    void about(){
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(24),dp(10),dp(24),dp(10));
        TextView info=text("A live collection from hojnikb / mg4-apps, plus MG4Zorst, MG4WirelessADP and the EVSuite apps (EVTasker, EVABRPUploader, EVChargePilot, EVLauncher, EVSwipe). Duplicate projects appear once.\n\nRecommended order uses editorial usefulness, GitHub APK download counts and stable-release age. Counts cover up to 100 releases (including prereleases); + means a lower bound. Downloads are not unique users. Missing stats are not treated as zero. Compatibility groups always come first. SWI165 evidence was reviewed on 16 Nov 2026 from READMEs and release notes. Author-confirmed means documented support, not testing on your car. New versions and replaced assets stay unverified until reviewed. Tap a compatibility label for evidence.\n\nLicensed under GNU GPL version 3 (GPL-3.0-only). Source: github.com/suchhodler-boop/MG4Install.\n\nMG4Zorst-inspired design. Installer adapted from Skittle6938/MG4Dash under GPL-3.0.\n\nSystem installation requires compatible MG4 platform firmware. Use while parked.",15,TEXT);content.addView(info);
        CheckBox choice=new CheckBox(this);choice.setText("Include prerelease APKs");choice.setTextColor(TEXT);choice.setTextSize(22);choice.setChecked(beta);choice.setPadding(0,dp(15),0,0);choice.setOnCheckedChangeListener((v,checked)->{beta=checked;getPreferences(0).edit().putBoolean("beta",beta).apply();});content.addView(choice);
        ScrollView aboutScroll=new ScrollView(this);aboutScroll.addView(content);new AppDialog(this).setTitle("MG4 Install "+SelfUpdater.version(this)).setView(aboutScroll).setPositiveButton("Done",null).setNeutralButton("GPL-3.0 license",(d,w)->showLicense()).show();
    }
}
