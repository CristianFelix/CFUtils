/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.nyu.nyuvis.cfutils.nlp;

import java.util.ArrayList;

/**
 *
 * @author cristian
 */
public class KeyWord extends ArrayList<Word>{

    @Override
    public String toString() {
        String result = "";
        for(Word w: this){
             result += w.word() + " ";
        }
        return result.trim();
    }
    
}
