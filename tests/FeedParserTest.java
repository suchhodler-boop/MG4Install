import com.mg4.install.FeedParser;
import java.nio.file.*;
public class FeedParserTest {
 public static void main(String[] args)throws Exception{
  String fixture="<a href=\"/unrelated/repo\">ignore</a><div id=\"user-list-repositories\"><h2 class=\"h3\"><a href=\"/owner/App\">App</a></h2><p itemprop=\"description\">Useful &amp; simple</p><h2><a href=\"/owner/Other\">Other</a></h2>";
  if(FeedParser.parse(fixture).size()!=2)throw new AssertionError("repository boundaries");
  if(!FeedParser.parse(fixture).get(0).description.equals("Useful & simple"))throw new AssertionError("description decoding");
  if(!FeedParser.next("<a rel=\"next\" href=\"/stars/hojnikb/lists/mg4-apps?page=2&amp;x=1\">").equals(FeedParser.FEED+"?page=2&x=1"))throw new AssertionError("pagination");
  try{FeedParser.parse("login or changed markup");throw new AssertionError("must reject bad HTML");}catch(IllegalArgumentException expected){}
  if(args.length>0){int n=FeedParser.parse(Files.readString(Path.of(args[0]))).size();if(n<1)throw new AssertionError("live fixture empty");System.out.println("Live list: "+n+" projects");}
  System.out.println("FeedParser tests passed");
 }
}
