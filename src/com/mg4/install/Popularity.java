package com.mg4.install;
import org.json.*;
import java.time.Instant;
import java.util.Locale;
public final class Popularity {
 public static Ranking.Stats fetch(String slug)throws Exception {
  JSONArray releases=new JSONArray(Network.get("https://api.github.com/repos/"+slug+"/releases?per_page=100"));
  Ranking.Stats s=new Ranking.Stats();s.checked=System.currentTimeMillis();s.partial=releases.length()==100;
  for(int i=0;i<releases.length();i++){
   JSONObject release=releases.getJSONObject(i);if(release.optBoolean("draft"))continue;
   JSONArray assets=release.getJSONArray("assets");boolean apk=false;
   for(int j=0;j<assets.length();j++){JSONObject a=assets.getJSONObject(j);if(a.optString("name").toLowerCase(Locale.ROOT).endsWith(".apk")){s.downloads+=Math.max(0,a.optLong("download_count"));apk=true;}}
   if(apk&&!release.optBoolean("prerelease"))try{s.latest=Math.max(s.latest,Instant.parse(release.getString("published_at")).toEpochMilli());}catch(Exception ignored){}
  }return s;
 }
 public static JSONObject encode(Ranking.Stats s)throws Exception {return new JSONObject().put("downloads",s.downloads).put("latest",s.latest).put("checked",s.checked).put("partial",s.partial);}
 public static Ranking.Stats decode(JSONObject j){Ranking.Stats s=new Ranking.Stats();s.downloads=j.optLong("downloads");s.latest=j.optLong("latest");s.checked=j.optLong("checked");s.partial=j.optBoolean("partial");return s;}
}
