package edu.nyu.nyuvis.cfutils.nlp;

import edu.nyu.nyuvis.cfutils.Utils;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Processor {
    protected final Properties p;
    private final Set<String> stoplist;
    
    public Processor(Properties properties) {
        this.p = properties;
        stoplist = new HashSet<>();
        if(p.getProperty("stoplist") != null){
            Utils.info("Loading stoplist... ", false);
            try (Stream<String> lines = Files.lines(Paths.get(p.getProperty("stoplist")), Charset.defaultCharset())) {
                lines.forEach(l -> this.stoplist.add(l.toLowerCase()));
            } catch (Exception e){
                Utils.error("Error loading stoplist");
                Utils.error(e.getMessage());
            }
            Utils.success("Done! (" + stoplist.size() + ")");
        }
    }
    
    public Document doc(String text){
        return new Document(this, text);
    }
    
    
    
    public boolean isStopWord(String word){
        return this.stoplist.contains(word);
    }
    public boolean isStopWord(Word word){
        return this.stoplist.contains(word.word().toLowerCase());
    }
    
    public abstract List<Sentence> tokenize(Document doc);
    public abstract List<Sentence> posTag(Document doc);
    public abstract List<KeyWord> extractKeyWords(Document doc);
}
