package com.mg4.install;
import java.util.*;
import java.util.regex.*;
/** Read-only detection learned from MG4Control FirmwareInfo; see THIRD_PARTY.md. */
public final class Firmware {
 public static int parse(String raw) {
  if(raw==null)return 0;
  Matcher m=Pattern.compile("^(?:SWI|DBI)(68|69|131|132|133|165|173)(?![0-9])").matcher(raw.trim().toUpperCase(Locale.ROOT));
  return m.find()?Integer.parseInt(m.group(1)):0;
 }
 public static Set<Integer> targets(String name) {
  Set<Integer> out=new TreeSet<>();
  Matcher m=Pattern.compile("(?i)SWI[-_ ]*([0-9]{2,4})(?![0-9])").matcher(name);
  while(m.find())out.add(Integer.parseInt(m.group(1)));return out;
 }
 public static boolean mismatch(int firmware,String repo,String asset) {
  Set<Integer> a=targets(asset),p=targets(repo);
  return firmware>0&&((!a.isEmpty()&&!a.contains(firmware))||(!p.isEmpty()&&!p.contains(firmware)));
 }
 public static String label(String name) {Set<Integer> t=targets(name);return t.isEmpty()?"Compatibility unverified":"Named target: SWI"+t.toString().replace("[","").replace("]","").replace(", "," / SWI");}
}
