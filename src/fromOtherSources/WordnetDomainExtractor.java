package fromOtherSources;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javatools.datatypes.FinalSet;
import javatools.filehandlers.FileLines;
import javatools.parsers.Char;
import basics.Fact;
import basics.FactCollection;
import basics.FactComponent;
import basics.FactSource;
import basics.FactWriter;
import basics.RDFS;
import basics.Theme;
import fromWikipedia.Extractor;

/**
 * YAGO2s - WordnetDomainExtractor
 * 
 * Adds the Wordnet domains from the Wordnet Domain project, http://wndomains.fbk.eu
 * 
 * For this purpose, the extractor needs the files
 * (1) wn-domains-3.2-20070223.txt form the WordNet Domain project of the Fondazione Bruno Kessler, http://wndomains.fbk.eu
 * (2) wn20-30.noun from the WordNet Mapping project of the Universitat Politecnica de Catalunya, http://www.lsi.upc.es/~nlp   
 * 
 * @author Fabian M. Suchanek
 *
 */
public class WordnetDomainExtractor extends Extractor {

  /** File of wordnet domains from  http://wndomains.fbk.eu*/
  protected File wordnetDomains;

  /** Wordnet mappings from http://www.lsi.upc.es/~nlp*/
  protected File wordnetMappings;

  @Override
  public File inputDataFile() {   
    return wordnetDomains;
  }
  
  /** Output theme*/
  public static final Theme WORDNETDOMAINS = new Theme(
      "yagoWordnetDomains",
      "Thematic domains from the Wordnet Domains project of the Fondazione Bruno Kessler, http://wndomains.fbk.eu . "
          + "These domains group WordNet classes into topics such as 'Music', 'Arts', or 'Soccer'. "+
          "The data was generated using the WordNet mappings provided by the NLP Research Group of the Universitat Politecnica de Catalunya, http://www.lsi.upc.es/~nlp .",
      Theme.ThemeGroup.LINK);

  /** Output theme*/
  public static final Theme WORDNETDOMAINSOURCES = new Theme("wordnetDomainSources",
      "Sources for the thematic domains from the Wordnet Domains project, http://wndomains.fbk.eu");

  @Override
  public Set<Theme> input() {
    return new FinalSet<>(WordnetExtractor.WORDNETIDS);
  }

  @Override
  public Set<Theme> output() {
    return new FinalSet<>(WORDNETDOMAINS, WORDNETDOMAINSOURCES);
  }

  public WordnetDomainExtractor(File wordnetdomainsfolder) {
    this.wordnetMappings = new File(wordnetdomainsfolder, "wn20-30.noun");
    this.wordnetDomains = new File(wordnetdomainsfolder, "wn-domains-3.2-20070223.txt");
  }

  @Override
  public void extract(Map<Theme, FactWriter> output, Map<Theme, FactSource> input) throws Exception {
    //Writer w=new FileWriter("c:/fabian/data/wordnetdomains/wordnetdomains.tsv");
    //w.write("# "+WORDNETDOMAINS.description+"\n");
    Map<String, String> mappings = new HashMap<String, String>();
    Set<String> labels = new HashSet<>();
    Map<String, String> words = new FactCollection(input.get(WordnetExtractor.WORDNETIDS),true).getReverseMap("<hasSynsetId>");
    for (String line : new FileLines(wordnetMappings, "Loading Wordnet Mappings")) {
      String[] split = line.split("\\s");
      if (split.length < 2) continue;
      mappings.put(split[0], split[1]);
    }
    for (String line : new FileLines(wordnetDomains, "Parsing WordNet Domains")) {
      String[] split = line.split("\\s");
      if (split.length < 2) continue;
      String subject = Char.cutLast(Char.cutLast(split[0]));
      subject = mappings.get(subject);
      if (subject == null) continue;
      String id="1" + subject;
      subject = FactComponent.forString(id);
      subject = words.get(subject);
      if (subject == null) continue;
      for (int i = 1; i < split.length; i++) {
        String label = "<wordnetDomain_" + split[i] + ">";
        labels.add(label);
        write(output, WORDNETDOMAINS, new Fact(subject, "<hasWordnetDomain>", label), WORDNETDOMAINSOURCES, "<http://wndomains.fbk.eu>",
            "Wordnet Domain Mapper");
        //if(FactComponent.wordnetWord(subject)!=null) w.write(FactComponent.wordnetWord(subject)+"\t"+id+"\t<http://yago-knowledge.org/resource/"+FactComponent.stripBrackets(subject)+">\t"+split[i]+"\n");
      }      
    }
    //w.close();
    for (String label : labels) {
      write(output, WORDNETDOMAINS, new Fact(label, RDFS.type, "<wordnetDomain>"), WORDNETDOMAINSOURCES, "<http://wndomains.fbk.eu>",
          "Wordnet Domain Mapper");
      write(output, WORDNETDOMAINS, new Fact(label, RDFS.label, FactComponent.forStringWithLanguage(label.substring(15, label.length() - 1),"eng")),
          WORDNETDOMAINSOURCES, "<http://wndomains.fbk.eu>", "Wordnet Domain Mapper");
    }
  }

  public static void main(String[] args) throws Exception {
    new WordnetDomainExtractor(new File("C:/fabian/data/wordnetdomains/")).extract(new File("c:/fabian/data/yago2s"), "test");
  }
}
