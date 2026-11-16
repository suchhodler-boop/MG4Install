// SPDX-License-Identifier: GPL-3.0-only
package com.mg4.install;

import java.util.*;
import java.util.regex.*;

/** Parses only repository headings inside GitHub's public star-list section. */
public final class FeedParser {
    public static final String FEED = "https://github.com/stars/hojnikb/lists/mg4-apps";
    public static final class Repo {
        public final String slug, description;
        public Repo(String s, String d) { slug=s; description=d; }
        public String name() { return slug.substring(slug.indexOf('/')+1); }
    }
    public static List<Repo> parse(String html) {
        int start=html.indexOf("id=\"user-list-repositories\"");
        if(start<0) throw new IllegalArgumentException("GitHub list layout changed. Your saved feed is preserved.");
        String body=html.substring(start);
        Pattern p=Pattern.compile("<h2\\b[^>]*>\\s*<a\\b[^>]*href=\"/([A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+)\"[^>]*>.*?</h2>(.*?)(?=<h2\\b|$)",Pattern.DOTALL);
        Matcher m=p.matcher(body); LinkedHashMap<String,Repo> result=new LinkedHashMap<>();
        while(m.find()) {
            Matcher d=Pattern.compile("<p\\b[^>]*itemprop=\"description\"[^>]*>(.*?)</p>",Pattern.DOTALL).matcher(m.group(2));
            result.put(m.group(1),new Repo(m.group(1),d.find()?plain(d.group(1)):"Community project for your MG4. Open the project for details."));
        }
        if(result.isEmpty() && !body.contains("This list is empty") && !body.contains("doesn’t have any repositories") && !body.contains("doesn't have any repositories"))
            throw new IllegalArgumentException("No repositories could be read. Try refreshing later.");
        return new ArrayList<>(result.values());
    }
    public static String next(String html) {
        Matcher m=Pattern.compile("<a\\b([^>]*rel=\"next\"[^>]*)>").matcher(html);
        if(!m.find()) return null;
        Matcher href=Pattern.compile("href=\"([^\"]+)\"").matcher(m.group(1));
        if(!href.find()) return null;
        String url=href.group(1).replace("&amp;","&");
        if(url.startsWith("/")) url="https://github.com"+url;
        if(!url.startsWith(FEED+"?")) throw new IllegalArgumentException("Unexpected pagination URL");
        return url;
    }
    static String plain(String s) { return s.replaceAll("<[^>]+>","").replace("&amp;","&").replace("&lt;","<").replace("&gt;",">").replace("&#39;","'").replace("&quot;","\"").replaceAll("\\s+"," ").trim(); }
}
