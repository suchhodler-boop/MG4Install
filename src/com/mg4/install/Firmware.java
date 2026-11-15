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
}
