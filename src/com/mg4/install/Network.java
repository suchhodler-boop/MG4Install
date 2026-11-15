package com.mg4.install;

import java.net.*;
import java.io.*;
import org.json.*;
import java.util.*;

public final class Network {
    public static HttpURLConnection connect(String url) throws Exception {
        URL u=new URL(url);
        if(!u.getProtocol().equals("https")) throw new IOException("Only secure HTTPS downloads are allowed");
        HttpURLConnection c=(HttpURLConnection)u.openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(30000);
        c.setRequestProperty("User-Agent","MG4-Install/1.0");
        c.setRequestProperty("Accept",u.getHost().equals("api.github.com")?"application/vnd.github+json":"*/*");
        int code=c.getResponseCode();
        if(code!=200) { c.disconnect(); throw new IOException(code==403||code==429?"GitHub request limit reached. Please try again later.":"GitHub returned HTTP "+code); }
        if(!c.getURL().getProtocol().equals("https")) { c.disconnect(); throw new IOException("Insecure redirect rejected"); }
        return c;
    }
    public static String get(String url) throws Exception {
        HttpURLConnection c=connect(url);
        try(InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] b=new byte[16384]; int n;
            while((n=in.read(b))!=-1) { if(out.size()+n>8*1024*1024) throw new IOException("Feed response is too large"); out.write(b,0,n); }
            return out.toString("UTF-8");
        } finally { c.disconnect(); }
    }
    public static List<FeedParser.Repo> feed() throws Exception {
        LinkedHashMap<String,FeedParser.Repo> all=new LinkedHashMap<>(); Set<String> seen=new HashSet<>(); String url=FeedParser.FEED;
        while(url!=null) {
            if(!seen.add(url)||seen.size()>30) throw new IOException("Unexpected GitHub pagination");
            String html=get(url); for(FeedParser.Repo r:FeedParser.parse(html)) all.put(r.slug,r);
            url=FeedParser.next(html);
        }
        return new ArrayList<>(all.values());
    }
    public static final class Asset {
        String name,url,version,digest,repo=""; int reviewedFirmware=0; long size,id;
        Asset(String n,String u,String v,String d,long s) { name=n;url=u;version=v;digest=d;size=s; }
        String label() { return name+"\n"+version+(size>0?String.format(java.util.Locale.US," · %.1f MB",size/1048576.0):""); }
    }
    public static List<Asset> assets(String slug,boolean beta) throws Exception {
        List<Asset> out=new ArrayList<>();
        // Walk releases until the newest eligible release containing APKs is found.
        for(int page=1;page<=3;page++) {
            JSONArray releases=new JSONArray(get("https://api.github.com/repos/"+slug+"/releases?per_page=30&page="+page));
            for(int i=0;i<releases.length();i++) {
                JSONObject r=releases.getJSONObject(i);
                if(r.optBoolean("draft")||(!beta&&r.optBoolean("prerelease"))) continue;
                JSONArray a=r.getJSONArray("assets");
                for(int j=0;j<a.length();j++) { JSONObject x=a.getJSONObject(j); String n=x.getString("name");
                    if(n.toLowerCase(Locale.ROOT).endsWith(".apk")){Asset asset=new Asset(n,x.getString("browser_download_url"),r.optString("tag_name"),x.optString("digest",""),x.optLong("size"));asset.id=x.optLong("id");out.add(asset);}
                }
                if(!out.isEmpty()) return out;
            }
            if(releases.length()<30) break;
        }
        // Some projects publish APKs directly in their repository, without releases.
        JSONObject repo=new JSONObject(get("https://api.github.com/repos/"+slug));
        String branch=repo.getString("default_branch");
        JSONObject commit=new JSONObject(get("https://api.github.com/repos/"+slug+"/commits/"+URLEncoder.encode(branch,"UTF-8")));
        String sha=commit.getString("sha");
        JSONObject tree=new JSONObject(get("https://api.github.com/repos/"+slug+"/git/trees/"+sha+"?recursive=1"));
        JSONArray entries=tree.getJSONArray("tree");
        for(int i=0;i<entries.length();i++) { JSONObject x=entries.getJSONObject(i); String path=x.getString("path");
            if(x.optString("type").equals("blob")&&path.toLowerCase(Locale.ROOT).endsWith(".apk")) {
                String encoded=URLEncoder.encode(path,"UTF-8").replace("%2F","/").replace("+","%20");
                out.add(new Asset(path,"https://raw.githubusercontent.com/"+slug+"/"+sha+"/"+encoded,"Repository · "+sha.substring(0,7),"",x.optLong("size")));
            }
        }
        if(out.isEmpty()&&tree.optBoolean("truncated")) throw new IOException("Repository is too large to scan fully. Open its source page.");
        return out;
    }
}
