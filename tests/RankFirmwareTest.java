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
  long now=1800000000000L;Ranking.Stats old=new Ranking.Stats(),fresh=new Ranking.Stats();
  old.downloads=fresh.downloads=100;old.latest=now-800L*86400000;fresh.latest=now-10L*86400000;
  check(Ranking.score("a/app",fresh,now)>Ranking.score("a/app",old,now),"freshness penalty");
  Ranking.Stats popular=new Ranking.Stats();popular.downloads=100000;popular.latest=fresh.latest;
  check(Ranking.score("a/app",popular,now)>Ranking.score("a/app",fresh,now),"download weighting");
  String confirmed="Skittle6938/MG4Dash", unknown="a/SWI165", blocked="ernestomicco2026-cell/Mg4-Launcher-SWI69";
  Compatibility.Entry e=Compatibility.get(confirmed);
  check(Compatibility.asset(confirmed,165,e.tag,e.ids[0])==0,"reviewed APK is confirmed");
  check(Compatibility.asset(confirmed,165,"future",e.ids[0])==1,"new release unverified");
  check(Compatibility.asset(confirmed,165,e.tag,-1)==1,"replaced asset unverified");
  check(Compatibility.asset(confirmed,69,e.tag,e.ids[0])==1,"165 evidence cannot confirm 69");
  check(Compatibility.project(confirmed,165,"future")==1,"new latest tag changes section");
  check(Compatibility.project(unknown,165,"")==1,"filename cannot confirm firmware");
  check(Compatibility.project("a/SWI69",165,"")==1,"filename cannot reject firmware");
  check(Compatibility.project(blocked,165,null)==2,"explicit README restriction");
  check(Compatibility.project("Skittle6938/MG4Radio",165,null)==1,"fix notes not full support");
  check(Compatibility.project("Skittle6938/MG4-AndroidAuto-BootFix",165,null)==1,"unknown remains unknown");
  check(Compatibility.project("MG-EV-Hackers/Open-Vehicle-Monitoring-System-3-MGEV",165,null)==3,"reference project");
  Map<String,Ranking.Stats> map=new HashMap<>();map.put(unknown,popular);map.put(confirmed,old);
  for(int mode=0;mode<3;mode++){
   List<FeedParser.Repo> list=new ArrayList<>(Arrays.asList(new FeedParser.Repo(unknown,"SWI165 compatible"),new FeedParser.Repo(blocked,""),new FeedParser.Repo(confirmed,"")));
   list.sort(Ranking.order(map,mode,165,now));check(list.get(0).slug.equals(confirmed)&&list.get(2).slug.equals(blocked),"compatibility groups precede sort mode "+mode);
  }
  System.out.println("21 firmware, evidence and ranking checks passed");
 }
}
