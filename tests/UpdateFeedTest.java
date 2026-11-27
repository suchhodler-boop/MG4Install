import com.mg4.install.*;
import java.util.*;
public class UpdateFeedTest {
 static int count=0;static void check(boolean value,String why){count++;if(!value)throw new AssertionError(why);}
 static void rejected(Runnable r,String why){boolean failed=false;try{r.run();}catch(IllegalArgumentException e){failed=true;}check(failed,why);}
 public static void main(String[] args){
  List<FeedParser.Repo> merged=FeedSources.merge(Collections.emptyList());check(merged.size()==7,"extras exist offline");
  List<FeedParser.Repo> upstream=Arrays.asList(new FeedParser.Repo("SUCHHODLER-BOOP/mg4zorst","upstream description"),new FeedParser.Repo("suchhodler-boop/MG4WirelessADP","ADB"),new FeedParser.Repo("a/other","other"));
  merged=FeedSources.merge(upstream);check(merged.size()==8,"extras do not duplicate upstream even with different casing");check(merged.get(0).description.equals("upstream description"),"upstream description wins");check(FeedSources.merge(merged).size()==8,"merge is idempotent");
  check(UpdatePolicy.newer(5,6),"upgrade offered");check(!UpdatePolicy.newer(6,6)&&!UpdatePolicy.newer(7,6),"no repeated update or downgrade");
  String url="https://github.com/suchhodler-boop/MG4Install/releases/download/v1.4.0/update.json";
  check(UpdatePolicy.releaseUrl(url,"v1.4.0","update.json"),"official stable URL");check(!UpdatePolicy.releaseUrl(url.replace("https:","http:"),"v1.4.0","update.json"),"HTTPS required");
  check(!UpdatePolicy.releaseUrl(url.replace("MG4Install/","Other/"),"v1.4.0","update.json"),"other repo rejected");check(!UpdatePolicy.releaseUrl(url,"v1.4.0-beta","update.json"),"beta rejected");check(!UpdatePolicy.releaseUrl(url+"?redirect=x","v1.4.0","update.json"),"unexpected URL rejected");
  check(UpdatePolicy.digest(new String(new char[64]).replace('\0','a')),"sha256 accepted");check(!UpdatePolicy.digest("abc")&&!UpdatePolicy.digest(null),"missing/short checksum rejected");
  UpdatePolicy.validatePackage("app",5,new String[]{"key"},"app",6,new String[]{"key"},6);check(true,"matching upgrade accepted");
  rejected(()->UpdatePolicy.validatePackage("app",5,new String[]{"key"},"other",6,new String[]{"key"},6),"wrong package");
  rejected(()->UpdatePolicy.validatePackage("app",5,new String[]{"key"},"app",6,new String[]{"other"},6),"wrong signer");
  rejected(()->UpdatePolicy.validatePackage("app",5,new String[]{"key"},"app",5,new String[]{"key"},5),"same version");
  rejected(()->UpdatePolicy.validatePackage("app",5,new String[]{"key"},"app",4,new String[]{"key"},4),"downgrade");
  rejected(()->UpdatePolicy.validatePackage("app",5,new String[]{"key"},"app",7,new String[]{"key"},6),"manifest and APK version disagree");
  check(!UpdatePolicy.signatures(null,new String[]{"key"})&&!UpdatePolicy.signatures(new String[0],new String[0]),"missing signatures");
  System.out.println(count+" updater and supplemental feed checks passed");
 }
}
