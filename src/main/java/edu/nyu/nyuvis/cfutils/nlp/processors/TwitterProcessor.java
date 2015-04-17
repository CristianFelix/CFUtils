/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.nlp.processors;

import edu.nyu.nyuvis.cfutils.Utils;
import edu.nyu.nyuvis.cfutils.nlp.Document;
import edu.nyu.nyuvis.cfutils.nlp.KeyWord;
import edu.nyu.nyuvis.cfutils.nlp.Processor;
import edu.nyu.nyuvis.cfutils.nlp.Sentence;
import edu.nyu.nyuvis.cfutils.nlp.Word;
import edu.nyu.nyuvis.cfutils.nlp.utils.CoreNLP;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterProcessor extends Processor{
    
    private final Pattern url = Pattern.compile("(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w\\.-]*)*");
    private final Pattern usrName = Pattern.compile("(^|(?<=(\\s)))@\\w+");
    private final Pattern hashTag = Pattern.compile("(^|(?<=(\\s)))#\\w+");
    
    private MaxentTagger tagger;
    private MaxentTagger tagger() {
        if(tagger == null) {
            Utils.info("Loading tagger...",false);
            String taggerPath = this.p.getProperty("tagger");
            tagger = new MaxentTagger(taggerPath);
            Utils.success("Done!");
        }
        return tagger;
    };
    
    public TwitterProcessor(Properties properties) {
        super(properties);
    }
    
    
   
    public void process(Document doc){
        try {
            String text = doc.text();
            
            String regexPattern = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";
            byte[] utf8 = text.getBytes("UTF-8");
            
            String string1 = new String(utf8, "UTF-8");
            
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(string1);
            
            text = matcher.replaceAll("");
            
            //Url
            Matcher mUrl = url.matcher(text);
            text = mUrl.replaceAll("_url");
            
            //User Name
            Matcher mUser = usrName.matcher(text);
            text = mUser.replaceAll("_user");
            
            //HashTag
            Matcher mTag = hashTag.matcher(text);
            text = mTag.replaceAll("_hastag");
            
            doc.text(text);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TwitterProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
   
    

    @Override
    public List<Sentence> tokenize(Document doc){
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(doc.text()));
        tokenizer.setEscaper(null);
        return CoreNLP.convert(tokenizer);
    }

    @Override
    public List<Sentence> posTag(Document doc) {
        doc.sentences().forEach(s -> {
            List<TaggedWord> tagSentence = tagger().tagSentence(CoreNLP.convert(s));
            for (int i = 0; i < tagSentence.size(); i++) {
                s.get(i).tag(tagSentence.get(i).tag());
            }
        });
        return doc.sentences();
    }
    
    
    
    @Override
    public List<KeyWord> extractKeyWords(Document doc) {
        List<KeyWord> keywords = new ArrayList<>();
        doc.sentences().forEach(s -> {
            KeyWord keyword = new KeyWord();
            for(Word w : s) {
                if(isSplitter(w)){
                    if(keyword.size() > 0) {
                        keywords.add(keyword);
                        keyword = new KeyWord();
                    }
                } else {
                    keyword.add(w);
                }
            }
            if(keyword.size() > 0)
                keywords.add(keyword);
        });
        return keywords;
    }
    
    private boolean isSplitter(Word w) {
        String text = w.word().trim();
        if(isStopWord(w))
            return true;
        
        if(!w.tag().startsWith("NN"))
            return true;
        
        Pattern p = Pattern.compile("\\W");
        Matcher m = p.matcher(w.word());
        
        return m.matches();
    }
}
