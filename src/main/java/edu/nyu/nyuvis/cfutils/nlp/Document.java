/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.nlp;

import java.util.List;

/**
 *
 * @author cristian
 */
public class Document {
    private final String original;
    public String original() { return this.original ;}
    
    
    private  String text;
    public String text() { return this.text ;}
    public void text(String value) { this.text = value; }
    
    private final Processor proc;
    public Processor proc() { return this.proc ;}
    
    private List<KeyWord> keywords;
    public List<KeyWord> keywords() { 
        if(keywords == null) { keywords = proc.extractKeyWords(this);}
        return this.keywords ;
    }
    
    public void posTag(){
        this.proc.posTag(this);
    }
    
    private List<Sentence> sentences;
    public List<Sentence> sentences() { 
        if(sentences == null) { sentences = proc.tokenize(this);}
        return this.sentences ;
    }
    
    public Document(Processor proc, String text) {
        this.text = text;
        this.original = text;
        this.proc = proc;
    }
    
    @Override
    public String toString() {
        return this.text;
    }
}
