/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ia; 


/**
 *
 * @author saanvikakde
 */
public class Word {
    //instance variables
    private String letters; 
    String[] conjunctions = {"and", "but", "or", "nor", "for", "so", "yet", "the"};
    //ArrayList list = new ArrayList<String>();

    public Word(String s) {
        letters = s; 
    }
    
    public boolean checkDifficulty() {
  if(letters.length() <= 2) {
     return false; }
  for(String c : conjunctions) {
       if (letters.toLowerCase().equals(c)) 
          return false;  
        } 
        return true;
    }
 
    
    public String toString() {
    return(letters);
}
}
