/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.nlp.utils;

import edu.nyu.nyuvis.cfutils.nlp.Sentence;
import edu.nyu.nyuvis.cfutils.nlp.Word;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author cristian
 */
public class CoreNLP {

    public static Word convert(HasWord hw){
        Word w = new Word();
        w.word(hw.word());
        return w;
    }
    
    public static IndexedWord convert(Word w){
        IndexedWord iw = new IndexedWord();
        iw.setWord(w.word());
        return iw;
    }
    
    public static List<Sentence> convert(DocumentPreprocessor tokenizer) {
        List<Sentence> sentences = new ArrayList<>();
        tokenizer.forEach((List<HasWord> t) -> {
            Sentence s = t.stream().map(w -> {return convert(w);}).collect(Collectors.toCollection(Sentence::new));
            sentences.add(s);
        });
        return sentences;
    }
    public static List<? extends HasWord> convert(Sentence s) {
        return s.stream().map(w -> {
            return convert(w);
        }).collect(Collectors.toCollection(ArrayList::new));
    }
}
