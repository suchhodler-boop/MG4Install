import com.mg4.install.*;
import java.util.*;
public class RankFirmwareTest {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  check(Firmware.parse("SWI165-29176-1300R32")==165,"SWI165 detection");
  check(Firmware.parse(" swi133-xyz ")==133,"trim and case");
  check(Firmware.parse("DBI68-29558-1X00RXX")==68,"documented distributor prefix");
  check(Firmware.parse("SWI1650-xyz")==0,"reject numeric prefix collision");
  check(Firmware.parse("ABC165-xyz")==0&&Firmware.parse(null)==0,"unknown must stay unknown");
  check(Firmware.mismatch(165,"Mg4-Launcher-SWI69","app.apk"),"repository mismatch");
  check(Firmware.mismatch(165,"Generic","app_SWI68.apk"),"asset mismatch");
  check(!Firmware.mismatch(165,"Generic","app_SWI165.apk"),"matching asset");
  check(!Firmware.mismatch(165,"Generic","app_SWI68_SWI165.apk"),"multiple explicit targets");
  check(Firmware.label("Generic").contains("unverified"),"unnamed is never compatible");
  long now=1800000000000L;Ranking.Stats old=new Ranking.Stats(),fresh=new Ranking.Stats();
  old.downloads=fresh.downloads=100;old.latest=now-800L*86400000;fresh.latest=now-10L*86400000;
  check(Ranking.score("a/app",fresh,now)>Ranking.score("a/app",old,now),"freshness penalty");
  Ranking.Stats popular=new Ranking.Stats();popular.downloads=100000;popular.latest=fresh.latest;
  check(Ranking.score("a/app",popular,now)>Ranking.score("a/app",fresh,now),"download weighting");
  Map<String,Ranking.Stats> map=new HashMap<>();map.put("a/old",old);map.put("a/fresh",fresh);map.put("a/SWI69",popular);
  List<FeedParser.Repo> list=new ArrayList<>(Arrays.asList(new FeedParser.Repo("a/SWI69",""),new FeedParser.Repo("a/old",""),new FeedParser.Repo("a/fresh","")));
  list.sort(Ranking.order(map,0,165,now));check(list.get(0).slug.equals("a/fresh")&&list.get(2).slug.equals("a/SWI69"),"mismatch ranked last");
  list.sort(Ranking.order(map,1,165,now));check(list.get(0).slug.equals("a/SWI69"),"downloads sort is literal");
  list.sort(Ranking.order(map,2,165,now));check(list.get(2).slug.equals("a/old"),"date sort");
  System.out.println("16 firmware/ranking checks passed");
 }
}
