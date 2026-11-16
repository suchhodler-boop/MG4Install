// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import java.util.*;
public final class Ranking {
 public static final class Stats {public long downloads,latest,checked;public String latestTag="";public boolean partial;}
 public static int usefulness(String slug) {
  String s=slug.toLowerCase(Locale.ROOT);
  if(s.endsWith("/mg4dash")||s.endsWith("/mg4control"))return 35;
  if(s.endsWith("/mg4radio")||s.endsWith("/mg4-hvacfloat"))return 30;
  if(s.contains("hvac")||s.contains("abrp")||s.contains("carmediaplayer")||s.contains("winclose"))return 25;
  if(s.contains("launcher")||s.contains("swipe")||s.contains("camera"))return 20;
  return 15;
 }
 public static double score(String slug,Stats stats,long now) {
  double value=usefulness(slug);
  if(stats==null)return value;
  value+=Math.min(30,Math.log10(1+stats.downloads)*7);
  if(stats.latest>0){long days=Math.max(0,(now-stats.latest)/86400000L);value+=days<=90?20:days<=180?10:days<=365?0:days<=730?-15:-30;}
  else value-=10;
  return value;
 }
 public static Comparator<FeedParser.Repo> order(Map<String,Stats> stats,int mode,int firmware,long now){return (a,b)->{
  Stats x=stats.get(a.slug),y=stats.get(b.slug);int n=Integer.compare(Compatibility.project(a.slug,firmware,x==null?null:x.latestTag),Compatibility.project(b.slug,firmware,y==null?null:y.latestTag));if(n!=0)return n;
  if(mode==0){n=Double.compare(score(b.slug,y,now),score(a.slug,x,now));}
  else if(mode==1)n=Long.compare(y==null?-1:y.downloads,x==null?-1:x.downloads);
  else n=Long.compare(y==null?0:y.latest,x==null?0:x.latest);
  return n!=0?n:a.slug.compareToIgnoreCase(b.slug);
 };}
}
