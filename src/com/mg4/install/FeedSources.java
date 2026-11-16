// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;
import java.util.*;
/** Extra sources are always present, including offline; GitHub slugs are case-insensitive. */
public final class FeedSources {
 public static List<FeedParser.Repo> merge(Collection<FeedParser.Repo> feed){
  LinkedHashMap<String,FeedParser.Repo> all=new LinkedHashMap<>();
  for(FeedParser.Repo r:feed)all.put(r.slug.toLowerCase(Locale.ROOT),r);
  add(all,"suchhodler-boop/MG4Zorst","Live engine-sound synthesizer with configurable voices and driving instruments for the MG4.");
  add(all,"suchhodler-boop/MG4WirelessADP","Enable wireless ADB on compatible MG4 head units; shows IP addresses and connection commands.");
  return new ArrayList<>(all.values());
 }
 static void add(Map<String,FeedParser.Repo> all,String slug,String description){all.putIfAbsent(slug.toLowerCase(Locale.ROOT),new FeedParser.Repo(slug,description));}
}
