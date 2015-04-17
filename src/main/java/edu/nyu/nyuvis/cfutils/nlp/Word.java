/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.nlp;

/**
 *
 * @author cristian
 */
public class Word {
    private String word;
    public String word() { return this.word ;}
    public void word(String value) { this.word = value; }
    
    private String tag;
    public String tag() { return this.tag ;}
    public void tag(String value) { this.tag = value; }

    @Override
    public String toString() {
        return this.word + "-" + this.tag;
    }
}
