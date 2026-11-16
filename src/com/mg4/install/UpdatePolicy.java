// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import java.util.*;
public final class UpdatePolicy {
 public static final String REPO="suchhodler-boop/MG4Install";
 public static boolean newer(long installed,long offered){return installed>0&&offered>installed;}
 public static boolean digest(String sha){return sha!=null&&sha.matches("(?i)[0-9a-f]{64}");}
 public static boolean releaseUrl(String url,String tag,String name){
  if(tag==null||!tag.matches("v[0-9]+\\.[0-9]+\\.[0-9]+")||name==null||!name.matches("[A-Za-z0-9._-]+"))return false;
  return ("https://github.com/"+REPO+"/releases/download/"+tag+"/"+name).equals(url);
 }
 public static boolean signatures(String[] installed,String[] offered){
  if(installed==null||offered==null||installed.length==0||installed.length!=offered.length)return false;
  return new HashSet<>(Arrays.asList(installed)).equals(new HashSet<>(Arrays.asList(offered)));
 }
 public static void validatePackage(String installedPackage,long installedCode,String[] installedSignatures,String offeredPackage,long offeredCode,String[] offeredSignatures,long expectedCode){
  if(!installedPackage.equals(offeredPackage))throw new IllegalArgumentException("Update has a different package name");
  if(offeredCode!=expectedCode||!newer(installedCode,offeredCode))throw new IllegalArgumentException("Update is not the expected newer version");
  if(!signatures(installedSignatures,offeredSignatures))throw new IllegalArgumentException("Update signing certificate does not match this installation");
 }
}
