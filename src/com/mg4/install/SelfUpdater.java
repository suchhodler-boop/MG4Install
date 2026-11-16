// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import android.content.*;
import android.content.pm.*;
import org.json.*;
import java.io.*;
public final class SelfUpdater {
 public static final class Release {public Network.Asset asset;public String notes,version;}
 public static String version(Context c){try{return c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName;}catch(Exception e){return "unknown";}}
 public static long code(Context c)throws Exception{return c.getPackageManager().getPackageInfo(c.getPackageName(),0).getLongVersionCode();}
 public static Release check(Context c)throws Exception{
  JSONObject release=new JSONObject(Network.get("https://api.github.com/repos/"+UpdatePolicy.REPO+"/releases/latest"));
  if(release.optBoolean("draft")||release.optBoolean("prerelease"))throw new IOException("No stable update available");
  String tag=release.getString("tag_name");JSONArray assets=release.getJSONArray("assets");JSONObject metadata=null;
  for(int i=0;i<assets.length();i++)if("update.json".equals(assets.getJSONObject(i).optString("name")))metadata=assets.getJSONObject(i);
  if(metadata==null)throw new IOException("This release has no update metadata");
  String metadataUrl=metadata.getString("browser_download_url");
  if(!UpdatePolicy.releaseUrl(metadataUrl,tag,"update.json"))throw new IOException("Unexpected update metadata URL");
  JSONObject manifest=new JSONObject(Network.get(metadataUrl));
  if(manifest.getInt("schema")!=1||!"com.mg4.install".equals(manifest.getString("packageName"))||!tag.equals("v"+manifest.getString("versionName")))throw new IOException("Invalid update metadata");
  long next=manifest.getLong("versionCode");if(!UpdatePolicy.newer(code(c),next))return null;
  // The emulator has a deliberately different application identity; never offer it a car APK.
  if(!"com.mg4.install".equals(c.getPackageName()))throw new IOException("Car self-updates are disabled in the emulator build");
  JSONObject apk=manifest.getJSONObject("apk");String name=apk.getString("name"),sha=apk.getString("sha256"),url=apk.getString("url");long size=apk.getLong("size");
  if(!name.equals("MG4-Install-"+manifest.getString("versionName")+"-car.apk")||!UpdatePolicy.digest(sha)||!UpdatePolicy.releaseUrl(url,tag,name)||size<=0||size>1024L*1024*1024)throw new IOException("Invalid update APK metadata");
  JSONObject matched=null;for(int i=0;i<assets.length();i++){JSONObject a=assets.getJSONObject(i);if(name.equals(a.optString("name")))matched=a;}
  if(matched==null||!url.equals(matched.getString("browser_download_url"))||size!=matched.getLong("size"))throw new IOException("Update APK does not match the published release asset");
  String githubDigest=matched.optString("digest","");if(!githubDigest.isEmpty()&&!githubDigest.equalsIgnoreCase("sha256:"+sha))throw new IOException("Update checksum metadata disagrees with GitHub");
  Release result=new Release();result.asset=new Network.Asset(name,url,tag,"sha256:"+sha,size);result.asset.id=matched.getLong("id");result.asset.repo=UpdatePolicy.REPO;result.asset.selfUpdateCode=next;result.version=manifest.getString("versionName");result.notes=release.optString("body","");return result;
 }
 static String[] signatures(PackageInfo p){if(p.signatures==null)return null;String[] out=new String[p.signatures.length];for(int i=0;i<out.length;i++)out[i]=p.signatures[i].toCharsString();return out;}
 public static void validate(Context c,File apk,long expected)throws Exception{
  PackageManager pm=c.getPackageManager();PackageInfo installed=pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNATURES),candidate=pm.getPackageArchiveInfo(apk.getPath(),PackageManager.GET_SIGNATURES);
  if(candidate==null)throw new IOException("Update is not a valid APK");
  UpdatePolicy.validatePackage(installed.packageName,installed.getLongVersionCode(),signatures(installed),candidate.packageName,candidate.getLongVersionCode(),signatures(candidate),expected);
 }
}
