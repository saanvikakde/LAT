/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ia;
import java.util.ArrayList;

/**
 *
 */
public class Keep {
  private String keptLetters; 
  private int numKeep; 
  private ArrayList list = new ArrayList<String>(); 

  public Keep() { 
      keptLetters = null; 
  }
  
   public Keep(String s) { 
        keptLetters = s; // Extract the letters from the Word object
    }
   
  public String toString() {
        return keptLetters;
    }
 
  
  public ArrayList<String> getList() { 
      return list;
  }
  
 

}
