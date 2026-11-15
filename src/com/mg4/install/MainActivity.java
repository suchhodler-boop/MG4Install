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
    boolean metricsLoading=false; int sortMode=0,firmware=0; String detected="";TextView sortButton,firmwareButton;
    final List<FeedParser.Repo> repos=new ArrayList<>();
    LinearLayout root,rows; TextView sync,status,count; EditText search; boolean loading=false,beta=false; String query="";
    Network.Asset pending; FeedParser.Repo pendingRepo;
    BroadcastReceiver updates=new BroadcastReceiver(){ public void onReceive(Context c,Intent i){showStatus();} };
    int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xff303b42);return d;}
    TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setFontFeatureSettings("kern");return v;}
    TextView button(String label,boolean primary,Runnable action){TextView v=text(label,14,primary?BG:TEXT);v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.create("sans-serif-medium",0));v.setPadding(dp(17),0,dp(17),0);v.setMinHeight(dp(48));v.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x3345d5a6),shape(primary?ACCENT:PANEL2,9),null));v.setOnClickListener(w->action.run());return v;}
    LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void space(LinearLayout l,int n){l.addView(new View(this),new LinearLayout.LayoutParams(dp(n),1));}
    public void onCreate(Bundle b){super.onCreate(b);beta=getPreferences(0).getBoolean("beta",false);getWindow().setFlags(1024,1024);getWindow().getDecorView().setSystemUiVisibility(4098|1024|512);detected=DeviceFirmware.raw();firmware=getPreferences(0).getInt("firmwareOverride",Firmware.parse(detected));sortMode=getPreferences(0).getInt("sort",0);readStats();build();registerReceiver(updates,new IntentFilter(getPackageName()+".STATUS"));readCache();refresh();}
    public void onConfigurationChanged(android.content.res.Configuration c){super.onConfigurationChanged(c);render();}
    public void onResume(){super.onResume();if(status!=null)showStatus();}
    public void onDestroy(){unregisterReceiver(updates);worker.shutdownNow();metricsWorker.shutdownNow();super.onDestroy();}
    void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(28),dp(20),dp(28),dp(14));root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=horizontal();
        TextView logo=text("MG4",25,ACCENT);logo.setTypeface(Typeface.create("sans-serif-black",0));header.addView(logo);space(header,10);
        TextView title=text("INSTALL",25,TEXT);title.setLetterSpacing(.12f);title.setTypeface(Typeface.create("sans-serif-light",0));header.addView(title);
        View stretch=new View(this);header.addView(stretch,new LinearLayout.LayoutParams(0,1,1));
        firmwareButton=button(firmwareText(),false,()->chooseFirmware());header.addView(firmwareButton);space(header,10);
        sortButton=button(sortText(),false,()->chooseSort());header.addView(sortButton);space(header,10);
        header.addView(button("↻  Refresh",false,()->refresh()));space(header,10);header.addView(button("About",false,()->about()));space(header,10);header.addView(button("×",false,()->finish()));
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(50)));
        View line=new View(this);line.setBackgroundColor(0xff303c41);LinearLayout.LayoutParams rule=new LinearLayout.LayoutParams(-1,dp(1));rule.topMargin=dp(16);rule.bottomMargin=dp(18);root.addView(line,rule);
        LinearLayout tools=horizontal();LinearLayout heading=new LinearLayout(this);heading.setOrientation(1);
        TextView h=text("Your MG4. More possibilities.",23,TEXT);h.setTypeface(Typeface.create("sans-serif-medium",0));heading.addView(h);
        sync=text("Connecting to hojnikb / mg4-apps…",12,MUTED);sync.setPadding(0,dp(5),0,0);heading.addView(sync);tools.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(14);search.setTextColor(TEXT);search.setHintTextColor(MUTED);search.setHint("Search apps or developers");search.setPadding(dp(16),0,dp(12),0);search.setBackground(shape(PANEL,9));tools.addView(search,new LinearLayout.LayoutParams(dp(260),dp(48)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){query=s.toString().toLowerCase(Locale.ROOT);render();}public void afterTextChanged(Editable e){}});
        root.addView(tools);count=text("COMMUNITY APPS",11,ACCENT);count.setLetterSpacing(.15f);count.setPadding(0,dp(19),0,dp(11));root.addView(count);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);rows=new LinearLayout(this);rows.setOrientation(1);scroll.addView(rows);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=horizontal();status=text("Tap an install icon to choose an APK.",12,MUTED);status.setMaxLines(2);footer.addView(status,new LinearLayout.LayoutParams(0,dp(42),1));status.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(button("Cancel download",false,()->{if(InstallService.busy)startService(new Intent(this,InstallService.class).setAction("cancel"));}));root.addView(footer);
    }
    void showStatus(){status.setText(getSharedPreferences("state",0).getString("status","Tap an install icon to choose an APK."));}
    void render(){
        if(rows==null)return;rows.removeAllViews();
        List<FeedParser.Repo> filtered=new ArrayList<>();for(FeedParser.Repo r:repos)if((r.slug+" "+r.description).toLowerCase(Locale.ROOT).contains(query))filtered.add(r);
        Collections.sort(filtered,Ranking.order(stats,sortMode,firmware,System.currentTimeMillis()));
        count.setText("COMMUNITY APPS   /   "+filtered.size());
        int width=getResources().getConfiguration().screenWidthDp;int columns=Math.max(1,Math.min(4,(width-42)/280));
        for(int group=0;group<4;group++){
            List<FeedParser.Repo> members=new ArrayList<>();for(FeedParser.Repo r:filtered)if(projectState(r)==group)members.add(r);
            if(members.isEmpty())continue;
            TextView section=text(Compatibility.section(group)+"  /  "+members.size(),13,group==0?ACCENT:0xffe6b66e);section.setPadding(0,dp(12),0,dp(12));rows.addView(section);
            LinearLayout row=null;
            for(int i=0;i<members.size();i++){
                if(i%columns==0){row=horizontal();row.setGravity(Gravity.TOP);rows.addView(row,new LinearLayout.LayoutParams(-1,-2));}
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(250),1);lp.rightMargin=dp(i%columns==columns-1?0:12);lp.bottomMargin=dp(12);row.addView(card(members.get(i)),lp);
            }
            if(row!=null&&members.size()%columns!=0)for(int i=members.size()%columns;i<columns;i++){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(250),1);lp.rightMargin=dp(i==columns-1?0:12);row.addView(new View(this),lp);}
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
        LinearLayout bottom=horizontal();TextView source=text("View project  ↗",12,MUTED);source.setMinHeight(dp(44));source.setGravity(Gravity.CENTER_VERTICAL);source.setOnClickListener(v->open(repo));bottom.addView(source,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView install=button(state==3?"↗  Source":"↓  Install",true,()->{if(state==3)open(repo);else resolve(repo);});install.setContentDescription("Install "+repo.name());bottom.addView(install,new LinearLayout.LayoutParams(dp(105),dp(44)));card.addView(bottom);return card;
    }
    int projectState(FeedParser.Repo r){Ranking.Stats m=stats.get(r.slug);return Compatibility.project(r.slug,firmware,m==null?null:m.latestTag);}
    void link(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){dialog("Source link",url);}}
    void evidence(FeedParser.Repo repo){Compatibility.Entry e=Compatibility.get(repo.slug);new AlertDialog.Builder(this).setTitle(Compatibility.label(projectState(repo))).setMessage(e.note+"\n\nReview: 16 Nov 2026, SWI165. Reviewed stable APK release: "+(e.tag.isEmpty()?"none":e.tag)+". New releases/replaced assets are unverified. Documented support does not establish every feature, revision or hardware combination.").setPositiveButton("README",(d,w)->link(e.readme)).setNeutralButton("Release notes",(d,w)->link(e.release)).setNegativeButton("Close",null).show();}
    void open(FeedParser.Repo r){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/"+r.slug)));}catch(Exception e){dialog("Project link","https://github.com/"+r.slug);}}
    void refresh(){if(loading)return;loading=true;sync.setText("Syncing live GitHub list…");worker.execute(()->{try{List<FeedParser.Repo> fresh=Network.feed();JSONArray a=new JSONArray();for(FeedParser.Repo r:fresh){JSONObject o=new JSONObject();o.put("slug",r.slug);o.put("description",r.description);a.put(o);}getSharedPreferences("feed",0).edit().putString("data",a.toString()).putLong("time",System.currentTimeMillis()).apply();runOnUiThread(()->{repos.clear();repos.addAll(fresh);loading=false;sync.setText("● Live from GitHub  ·  "+fresh.size()+" projects  ·  just updated");render();loadStats(new ArrayList<>(fresh));});}catch(Exception e){runOnUiThread(()->{loading=false;sync.setText("Offline / sync unavailable · showing saved feed");status.setText(e.getMessage());});}});}
    void readCache(){try{android.content.SharedPreferences p=getSharedPreferences("feed",0);JSONArray a=new JSONArray(p.getString("data","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);repos.add(new FeedParser.Repo(o.getString("slug"),o.getString("description")));}if(!repos.isEmpty())sync.setText("Saved feed · "+android.text.format.DateFormat.format("dd MMM, HH:mm",p.getLong("time",0)));render();}catch(Exception ignored){}}
    void resolve(FeedParser.Repo repo){
        if(firmware==0){chooseFirmware();return;}
        if(InstallService.busy){dialog("Download in progress","Wait for the current APK or cancel its download first.");return;}
        status.setText("Finding APKs for "+repo.name()+"…");
        worker.execute(()->{try{List<Network.Asset> assets=Network.assets(repo.slug,beta);runOnUiThread(()->{
            if(isFinishing()||isDestroyed())return;
            if(assets.isEmpty()){status.setText("No published APK found for "+repo.name());new AlertDialog.Builder(this).setTitle("No APK available").setMessage("This project has no APK in its eligible releases or repository. See the project’s instructions."+(beta?"":" You can include prereleases in About.")).setPositiveButton("View project",(d,w)->open(repo)).setNegativeButton("Close",null).show();return;}
            status.setText("Choose an APK for "+repo.name());for(Network.Asset asset:assets)asset.repo=repo.slug;String[] labels=new String[assets.size()];for(int i=0;i<labels.length;i++){Network.Asset a=assets.get(i);labels[i]=a.label()+" · "+Compatibility.label(Compatibility.asset(repo.slug,firmware,a.version,a.id));}
            new AlertDialog.Builder(this).setTitle(repo.name()+" · choose APK").setItems(labels,(d,index)->confirm(repo,assets.get(index))).setNegativeButton("Cancel",null).show();
        });}catch(Exception e){runOnUiThread(()->{status.setText(e.getMessage());dialog("Couldn’t find APKs",e.getMessage());});}});
    }
    void confirm(FeedParser.Repo repo,Network.Asset asset){
        int state=Compatibility.asset(repo.slug,firmware,asset.version,asset.id);
        if(state==Compatibility.UNSUPPORTED||state==Compatibility.REFERENCE){dialog("Installation blocked",Compatibility.get(repo.slug).note);return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(24),dp(10),dp(24),dp(10));
        Compatibility.Entry entry=Compatibility.get(repo.slug);
        box.addView(text(asset.label()+"\n\nYour firmware: SWI"+firmware+"\n"+Compatibility.label(state)+"\n\n"+entry.note+"\n\nEvidence reviewed 16 Nov 2026; stable release: "+(entry.tag.isEmpty()?"none":entry.tag)+". Confirmation applies only to reviewed asset IDs. Other releases and replacement assets remain unverified. Check the full firmware revision and equipment.",15,TEXT));
        TextView sources=button("Read compatibility evidence",false,()->evidence(repo));box.addView(sources);
        CheckBox checked=new CheckBox(this);checked.setText("I checked the author’s instructions for my firmware");box.addView(checked);
        ScrollView reviewScroll=new ScrollView(this);reviewScroll.addView(box); AlertDialog d=new AlertDialog.Builder(this).setTitle("Check firmware before installing").setView(reviewScroll).setPositiveButton("Download & install",(v,w)->{asset.reviewedFirmware=firmware;begin(asset);}).setNegativeButton("Cancel",null).setNeutralButton("View project",(v,w)->open(repo)).create();
        d.setOnShowListener(v->{d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);checked.setOnCheckedChangeListener((c,on)->d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(on));});d.show();
    }
    void begin(Network.Asset a){
        if(firmware==0||a.reviewedFirmware!=firmware||Compatibility.asset(a.repo,firmware,a.version,a.id)>=Compatibility.UNSUPPORTED){dialog("Review firmware again","Firmware is unknown, changed, or mismatched. Select a matching APK and review compatibility.");return;}
        if(InstallService.busy){dialog("Download in progress","Wait for the current download to finish.");return;}
        if(checkSelfPermission("android.permission.INSTALL_PACKAGES")!=android.content.pm.PackageManager.PERMISSION_GRANTED&&!getPackageManager().canRequestPackageInstalls()){
            pending=a;new AlertDialog.Builder(this).setTitle("Allow app installation").setMessage("Enable permission for MG4 Install, then return here. If Android restarts the app, select the APK again.").setPositiveButton("Open settings",(d,w)->startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())),10)).setNegativeButton("Cancel",(d,w)->pending=null).show();return;
        }
        startForegroundService(new Intent(this,InstallService.class).putExtra("firmware",firmware).putExtra("reviewed",a.reviewedFirmware).putExtra("repo",a.repo).putExtra("version",a.version).putExtra("assetId",a.id).putExtra("url",a.url).putExtra("name",a.name).putExtra("size",a.size).putExtra("digest",a.digest));status.setText("Starting download…");
    }
    protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==10&&pending!=null){Network.Asset a=pending;pending=null;if(getPackageManager().canRequestPackageInstalls())begin(a);else status.setText("Installation permission was not granted.");}}
    void dialog(String title,String message){if(!isFinishing()&&!isDestroyed())new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show();}
    String firmwareText(){return firmware==0?"Firmware: unknown":"SWI"+firmware+(getPreferences(0).contains("firmwareOverride")?" · manual":" · auto");}
    String sortText(){return new String[]{"Recommended ▾","Most downloaded ▾","Recently updated ▾"}[sortMode];}
    void chooseSort(){new AlertDialog.Builder(this).setTitle("Order apps").setSingleChoiceItems(new String[]{"Recommended: usefulness + downloads + freshness","Most downloaded: GitHub APK assets","Recently updated: stable APK releases"},sortMode,(d,n)->{sortMode=n;getPreferences(0).edit().putInt("sort",n).apply();sortButton.setText(sortText());render();d.dismiss();}).setNegativeButton("Close",null).show();}
    void chooseFirmware(){
        final int[] versions={0,68,69,131,132,133,165,173};
        String[] choices={"Auto-detect again","SWI68","SWI69","SWI131","SWI132","SWI133","SWI165","SWI173"};
        new AlertDialog.Builder(this).setTitle("Firmware · detected: "+detected).setItems(choices,(d,n)->{if(n==0){getPreferences(0).edit().remove("firmwareOverride").apply();detected=DeviceFirmware.raw();firmware=Firmware.parse(detected);}else{firmware=versions[n];getPreferences(0).edit().putInt("firmwareOverride",firmware).apply();}firmwareButton.setText(firmwareText());render();if(firmware==0)dialog("Firmware not detected","Check your head unit’s system information and choose the SWI version manually. Installation is disabled until a version is set.");}).setNegativeButton("Close",null).show();
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
    void about(){
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(24),dp(10),dp(24),dp(10));
        TextView info=text("A live collection from hojnikb / mg4-apps.\n\nRecommended order uses editorial usefulness, GitHub APK download counts and stable-release age. Counts cover up to 100 releases (including prereleases); + means a lower bound. Downloads are not unique users. Missing stats are not treated as zero. Compatibility groups always come first. SWI165 evidence was reviewed on 16 Nov 2026 from READMEs and release notes. Author-confirmed means documented support, not testing on your car. New versions and replaced assets stay unverified until reviewed. Tap a compatibility label for evidence.\n\nMG4Zorst-inspired design. Installer adapted from Skittle6938/MG4Dash under GPL-3.0.\n\nSystem installation requires compatible MG4 platform firmware. Use while parked.",15,TEXT);content.addView(info);
        CheckBox choice=new CheckBox(this);choice.setText("Include prerelease APKs");choice.setChecked(beta);choice.setPadding(0,dp(15),0,0);choice.setOnCheckedChangeListener((v,checked)->{beta=checked;getPreferences(0).edit().putBoolean("beta",beta).apply();});content.addView(choice);
        ScrollView aboutScroll=new ScrollView(this);aboutScroll.addView(content);new AlertDialog.Builder(this).setTitle("MG4 Install 1.2.0").setView(aboutScroll).setPositiveButton("Done",null).setNeutralButton("GitHub list",(d,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(FeedParser.FEED)));}catch(Exception ignored){}}).show();
    }
}