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
    public void onCreate(Bundle b){super.onCreate(b);beta=getPreferences(0).getBoolean("beta",false);getWindow().setFlags(1024,1024);getWindow().getDecorView().setSystemUiVisibility(4098|1024|512);build();registerReceiver(updates,new IntentFilter(getPackageName()+".STATUS"));readCache();refresh();}
    public void onConfigurationChanged(android.content.res.Configuration c){super.onConfigurationChanged(c);render();}
    public void onResume(){super.onResume();if(status!=null)showStatus();}
    public void onDestroy(){unregisterReceiver(updates);worker.shutdownNow();super.onDestroy();}
    void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(28),dp(20),dp(28),dp(14));root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=horizontal();
        TextView logo=text("MG4",25,ACCENT);logo.setTypeface(Typeface.create("sans-serif-black",0));header.addView(logo);space(header,10);
        TextView title=text("INSTALL",25,TEXT);title.setLetterSpacing(.12f);title.setTypeface(Typeface.create("sans-serif-light",0));header.addView(title);
        View stretch=new View(this);header.addView(stretch,new LinearLayout.LayoutParams(0,1,1));
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
        count.setText("COMMUNITY APPS   /   "+filtered.size());
        int width=getResources().getConfiguration().screenWidthDp;int columns=Math.max(1,Math.min(4,(width-42)/280));
        LinearLayout row=null;
        for(int i=0;i<filtered.size();i++){
            if(i%columns==0){row=horizontal();row.setGravity(Gravity.TOP);rows.addView(row,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout card=card(filtered.get(i));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(204),1);lp.rightMargin=dp(i%columns==columns-1?0:12);lp.bottomMargin=dp(12);row.addView(card,lp);
        }
        if(row!=null&&filtered.size()%columns!=0)for(int i=filtered.size()%columns;i<columns;i++){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(204),1);lp.rightMargin=dp(i==columns-1?0:12);row.addView(new View(this),lp);}
        if(filtered.isEmpty()){TextView empty=text(repos.isEmpty()?"Your app collection will appear here.\nRefresh to connect to GitHub.":"No matching apps. Try another search.",18,MUTED);empty.setGravity(Gravity.CENTER);rows.addView(empty,new LinearLayout.LayoutParams(-1,dp(180)));}
    }
    LinearLayout card(FeedParser.Repo repo){
        LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(17),dp(15),dp(17),dp(13));card.setBackground(shape(PANEL,12));
        LinearLayout top=horizontal();String shortName=repo.name().replaceAll("(?i)^mg4[-_]?","");if(shortName.isEmpty())shortName="MG";
        TextView monogram=text(shortName.substring(0,Math.min(2,shortName.length())).toUpperCase(Locale.ROOT),16,ACCENT);monogram.setGravity(Gravity.CENTER);monogram.setTypeface(Typeface.DEFAULT_BOLD);monogram.setBackground(shape(0xff17352e,10));top.addView(monogram,new LinearLayout.LayoutParams(dp(40),dp(40)));space(top,10);
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(1);TextView name=text(repo.name(),16,TEXT);name.setTypeface(Typeface.create("sans-serif-medium",0));name.setMaxLines(1);name.setEllipsize(TextUtils.TruncateAt.END);titles.addView(name);TextView author=text(repo.slug.split("/")[0],11,MUTED);author.setSingleLine(true);titles.addView(author);top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));card.addView(top);
        TextView desc=text(android.text.Html.fromHtml(repo.description,0).toString(),13,MUTED);desc.setMaxLines(3);desc.setEllipsize(TextUtils.TruncateAt.END);desc.setLineSpacing(dp(2),1);desc.setPadding(0,dp(12),0,dp(8));card.addView(desc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout bottom=horizontal();TextView source=text("View project  ↗",12,MUTED);source.setMinHeight(dp(44));source.setGravity(Gravity.CENTER_VERTICAL);source.setOnClickListener(v->open(repo));bottom.addView(source,new LinearLayout.LayoutParams(0,dp(44),1));
        TextView install=button("↓  Install",true,()->resolve(repo));install.setContentDescription("Install "+repo.name());bottom.addView(install,new LinearLayout.LayoutParams(dp(105),dp(44)));card.addView(bottom);return card;
    }
    void open(FeedParser.Repo r){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/"+r.slug)));}catch(Exception e){dialog("Project link","https://github.com/"+r.slug);}}
    void refresh(){if(loading)return;loading=true;sync.setText("Syncing live GitHub list…");worker.execute(()->{try{List<FeedParser.Repo> fresh=Network.feed();JSONArray a=new JSONArray();for(FeedParser.Repo r:fresh){JSONObject o=new JSONObject();o.put("slug",r.slug);o.put("description",r.description);a.put(o);}getSharedPreferences("feed",0).edit().putString("data",a.toString()).putLong("time",System.currentTimeMillis()).apply();runOnUiThread(()->{repos.clear();repos.addAll(fresh);loading=false;sync.setText("● Live from GitHub  ·  "+fresh.size()+" projects  ·  just updated");render();});}catch(Exception e){runOnUiThread(()->{loading=false;sync.setText("Offline / sync unavailable · showing saved feed");status.setText(e.getMessage());});}});}
    void readCache(){try{android.content.SharedPreferences p=getSharedPreferences("feed",0);JSONArray a=new JSONArray(p.getString("data","[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);repos.add(new FeedParser.Repo(o.getString("slug"),o.getString("description")));}if(!repos.isEmpty())sync.setText("Saved feed · "+android.text.format.DateFormat.format("dd MMM, HH:mm",p.getLong("time",0)));render();}catch(Exception ignored){}}
    void resolve(FeedParser.Repo repo){
        if(InstallService.busy){dialog("Download in progress","Wait for the current APK or cancel its download first.");return;}
        status.setText("Finding APKs for "+repo.name()+"…");
        worker.execute(()->{try{List<Network.Asset> assets=Network.assets(repo.slug,beta);runOnUiThread(()->{
            if(isFinishing()||isDestroyed())return;
            if(assets.isEmpty()){status.setText("No published APK found for "+repo.name());new AlertDialog.Builder(this).setTitle("No APK available").setMessage("This project has no APK in its eligible releases or repository. See the project’s instructions."+(beta?"":" You can include prereleases in About.")).setPositiveButton("View project",(d,w)->open(repo)).setNegativeButton("Close",null).show();return;}
            status.setText("Choose an APK for "+repo.name());String[] labels=new String[assets.size()];for(int i=0;i<labels.length;i++)labels[i]=assets.get(i).label();
            new AlertDialog.Builder(this).setTitle(repo.name()+" · choose APK").setItems(labels,(d,index)->confirm(repo,assets.get(index))).setNegativeButton("Cancel",null).show();
        });}catch(Exception e){runOnUiThread(()->{status.setText(e.getMessage());dialog("Couldn’t find APKs",e.getMessage());});}});
    }
    void confirm(FeedParser.Repo repo,Network.Asset asset){new AlertDialog.Builder(this).setTitle("Install "+repo.name()+"?").setMessage(asset.label()+"\n\nFrom "+repo.slug+". Choose a build compatible with your head unit. Android verifies its signing certificate.").setPositiveButton("Download & install",(d,w)->begin(asset)).setNegativeButton("Cancel",null).show();}
    void begin(Network.Asset a){
        if(InstallService.busy){dialog("Download in progress","Wait for the current download to finish.");return;}
        if(checkSelfPermission("android.permission.INSTALL_PACKAGES")!=android.content.pm.PackageManager.PERMISSION_GRANTED&&!getPackageManager().canRequestPackageInstalls()){
            pending=a;new AlertDialog.Builder(this).setTitle("Allow app installation").setMessage("Enable permission for MG4 Install, then return here. If Android restarts the app, select the APK again.").setPositiveButton("Open settings",(d,w)->startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())),10)).setNegativeButton("Cancel",(d,w)->pending=null).show();return;
        }
        startForegroundService(new Intent(this,InstallService.class).putExtra("url",a.url).putExtra("name",a.name).putExtra("size",a.size).putExtra("digest",a.digest));status.setText("Starting download…");
    }
    protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==10&&pending!=null){Network.Asset a=pending;pending=null;if(getPackageManager().canRequestPackageInstalls())begin(a);else status.setText("Installation permission was not granted.");}}
    void dialog(String title,String message){if(!isFinishing()&&!isDestroyed())new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show();}
    void about(){
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(24),dp(10),dp(24),dp(10));
        TextView info=text("A live collection from hojnikb / mg4-apps.\n\nMG4Zorst-inspired design. Installer adapted from Skittle6938/MG4Dash under GPL-3.0.\n\nSystem installation requires compatible MG4 platform firmware. Use while parked.",15,TEXT);content.addView(info);
        CheckBox choice=new CheckBox(this);choice.setText("Include prerelease APKs");choice.setChecked(beta);choice.setPadding(0,dp(15),0,0);choice.setOnCheckedChangeListener((v,checked)->{beta=checked;getPreferences(0).edit().putBoolean("beta",beta).apply();});content.addView(choice);
        new AlertDialog.Builder(this).setTitle("MG4 Install 1.0.0").setView(content).setPositiveButton("Done",null).setNeutralButton("GitHub list",(d,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(FeedParser.FEED)));}catch(Exception ignored){}}).show();
    }
}