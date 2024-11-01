/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ia;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;


/**
 *
 * @author saanvikakde
 */
public class LATGUI extends javax.swing.JFrame {
       //My class variables delcared  
       String content; //content of the .txt file uploaded by user
       ArrayList<Word> wordList; //contains each individual word in content 
       ArrayList<Keep> keepList; //contains each word kept from wordList 
       ArrayList<String> temporary; //used to convert keepList to array w/o editing it
       String[] vocabWords; //keepList but converted to an array with any duplicates removed
       String[][] numberedWords; //vocabWords with numbers to create a table in GUI 
       Pattern pattern = Pattern.compile("[\\d\\W]");//using regex to get rid of any digits or non-word characters

    /**
     * Creates new form IAGUI
     */
    public LATGUI() {
        initComponents(); //initializes generated GUI from Design tab
        //my class variables declared
        wordList = new ArrayList<>(); 
        keepList = new ArrayList<>(); 
        temporary = new ArrayList<>(); 
        content = ""; 
             

        
    }
    
    //converts keepList to an array so it can be formatted into a JTabel
    private void convert() { 
        
        for(Keep keep : keepList) { 
            temporary.add(keep.toString()); 
        }
        
        Set<String> set = new HashSet<>(temporary); //remove duplicates from temporary 
       temporary.clear();
       temporary.addAll(set);
       
       System.out.println("Remove Duplicates: " + temporary + "\n"); 
        
    vocabWords = new String[temporary.size()]; 
        
       for(int i = 0; i < vocabWords.length; i++) {
           vocabWords[i] = temporary.get(i); 
       }
       populate(vocabWords); 
       
    }
       
    //populates the vocabTable 
    private void populate(String[] vocabWords) { 
      String[] columns = {"#", "Words:"};
       
      Arrays.sort(vocabWords); 
      
      numberedWords = new String[vocabWords.length][2];
        
        for(int i = 0; i < numberedWords.length; i++) {
            for(int j = 0; j < 2; j++){ 
                if(j == 0) 
                    numberedWords[i][j] = Integer.toString(i + 1); 
                else 
                    numberedWords [i][j] = vocabWords[i];
            }
        }
        
        System.out.println("Table Values: " + Arrays.deepToString(numberedWords) + "\n");

       DefaultTableModel model = new DefaultTableModel(numberedWords, columns); 
       VocabTable.setModel(model);  
        VocabTable.setEnabled(false);

 
   } 
    
    //edits specified vocab word to user's choice 
    private void edit(String editThis, String editTo) { 
            for (int i = 0; i < vocabWords.length; i++) {
                if (editThis.equals(vocabWords[i])) {
                    vocabWords[i] = editTo; 
                }
            }
           populate(vocabWords);  
            

    }
   
    //adds new vocab word as per user's choice
    private void add(String addThis) {
        String[] newVocabWords = new String[vocabWords.length + 1]; 
        for(int i = 0; i < newVocabWords.length; i++) {
            if (i == newVocabWords.length - 1) 
                newVocabWords[i] = addThis;
            else 
                newVocabWords[i] = vocabWords[i]; 
        }
        vocabWords = newVocabWords; 
        populate(newVocabWords);
    }
   
    //deletes vocab word as per user's choice
    private void delete(int deleteThis) { 
        String[] newVocabWords = new String[vocabWords.length - 1];
        System.arraycopy(vocabWords, 0, newVocabWords, 0, deleteThis); 
        System.arraycopy(vocabWords, deleteThis + 1, newVocabWords, deleteThis,  vocabWords.length - deleteThis - 1);
        vocabWords = newVocabWords; 
        populate(newVocabWords);
    }
    
    //saves the vocabulary list with translations to an excel sheet
    private void saveToExcel(String language, String outputFilePath) throws IOException, UnsupportedEncodingException, ParseException { 
       XSSFWorkbook workbook = new XSSFWorkbook(); 
        XSSFSheet sheet = workbook.createSheet("VocabularySheet");

        Row headerRow = sheet.createRow(0); //Create the header row

        Cell cellNumber = headerRow.createCell(0);
        cellNumber.setCellValue("Number");
        Cell cellWord = headerRow.createCell(1); 
        cellWord.setCellValue("Vocabulary Word");
        Cell cellTranslation = headerRow.createCell(2);
        cellTranslation.setCellValue("Translation");

        for (int i = 0; i < numberedWords.length; i++) {
                Row row = sheet.createRow(i + 1);

                Cell cell1 = row.createCell(0);
                cell1.setCellValue(numberedWords[i][0]);

                Cell cell2 = row.createCell(1);
                cell2.setCellValue(numberedWords[i][1]);

                Cell cell3 = row.createCell(2);
               
                cell3.setCellValue(translateWord((numberedWords[i][1]), language));
           
        
          }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
                workbook.write(fileOut);
               System.out.println("\nExcel file saved successfully to: " + outputFilePath);
               JOptionPane.showMessageDialog(this, "Excel file successfully saved to: " + outputFilePath, "File Saved", JOptionPane.INFORMATION_MESSAGE);


            }
         catch (IOException e) {
           JOptionPane.showMessageDialog(this, "Error writing file to Excel.", "Error", JOptionPane.ERROR_MESSAGE);

        }

    }
    
   //allows the user to upload a .txt file to the program so it can be stored into content
   private void uploadFile() {
    JFileChooser fileChooser = new JFileChooser(); //fileChooser GUI allows user to select file 
        int choice = fileChooser.showOpenDialog(this); //input from user through fileChooser
         if (choice == JFileChooser.APPROVE_OPTION) { //if user wishes to select file 
             BufferedReader reader; //declares reader 
             File selectedFile = fileChooser.getSelectedFile();
             String filename = selectedFile.getName(); 
             if(!(filename.substring(filename.length() - 4).equals(".txt"))) { //makes sure that file is a plain text file
               JOptionPane.showMessageDialog(this, "Selected file is not a plain text file.", "Error", JOptionPane.ERROR_MESSAGE);
               wordList.clear(); keepList.clear(); temporary.clear(); content = ""; //makes sure that no lists have assigned values
             }
             else {
            try {
                reader = new BufferedReader(new FileReader(selectedFile)); 
                // Read the content of the document and store it in a string
                String line;
                while ((line = reader.readLine()) != null) {
                    content += (line + "\n");
                }
           System.out.print(content);

            } catch (IOException ex) { //debugging 
                JOptionPane.showMessageDialog(this, "Error reading the document", "Error", JOptionPane.ERROR_MESSAGE);
            }
                                     
              createList(content); //creates the list based on the content reader traversed 
       System.out.println("Original Wordlist: " + wordList + "\n"); 
       categorize(); //creates Word and Keep objects based on complexity of words 
       System.out.println("Keeplist: " + keepList + "\n"); 
       CardLayout card = (CardLayout)parentPanel.getLayout();
       card.show(parentPanel, "List"); //changes panel to show user the displayed vocabulary list  
       convert(); //allows for arraylist to be converted into a 2d array with additional values to integrate into JTable 
       
       }

       }   
   }
   
   //creates wordList from content by spliting word in except into a String and formatting correctly and alphabetically
   private void createList(String c) {  
     String[] words = c.split("\\s+"); 
     for(String s : words) {
          s = s.replaceAll(" ", "");
          s = s.replaceAll("[\\d\\W]", "");  
          Word word = new Word(s);
          wordList.add(word);

     }

   }
   
   //adds Word objects to keepList by making them Keep objects 
   private void categorize() {
       for (Word w : wordList) { 
           if (w.checkDifficulty()) {
           String word = w.toString().substring(0,1).toUpperCase() + w.toString().substring(1).toLowerCase(); 
           keepList.add(new Keep(word));
           }
       }
   }
   
   //allows user to choose the file where Excel sheet should be stores
   private void ChooseFile(String language) throws IOException, ParseException { 
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");


        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String outputFilePath = fileChooser.getSelectedFile().getAbsolutePath() + ".xlsx"; 
            saveToExcel(language, outputFilePath); 
       }
        else 
          System.out.println("File save operation canceled by the user.");
        
   }
    
   //translates each vocabulary word into language as specified by user 
   private String translateWord(String word, String targetLang) throws UnsupportedEncodingException, IOException, ParseException {
       String url = "https://www.wordreference.com/en" + targetLang  + "/" + word; //using wordreference to generate translations
       CloseableHttpClient client =  HttpClients.createDefault(); //creating client to traverse html 
       HttpGet request = new HttpGet(url); //getting html  
       try (var response = client.execute(request)) {
       String webContent = EntityUtils.toString(response.getEntity());
      // System.out.println(webContent); 
       if (webContent.contains("translation found for")) {
       System.out.println("Translation not found"); //checks the existence of vocabulary word
        return "Translation not found"; }
    
       String start = "td class='ToWrd' >"; 
       String end = "<"; 
       webContent = webContent.substring(webContent.indexOf(start));
       int indexStart = (webContent.indexOf(start) + start.length());
       int indexEnd = (webContent.indexOf(end));
    //   System.out.println(content.substring(indexStart, indexEnd)); 
       return webContent.substring(indexStart, indexEnd); 
       
   }
   }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        parentPanel = new javax.swing.JPanel();
        mainMenu = new javax.swing.JPanel();
        uploadFileButton = new javax.swing.JButton();
        uploadDescription = new javax.swing.JLabel();
        mainMenuTitle = new javax.swing.JLabel();
        list = new javax.swing.JPanel();
        listTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        VocabTable = new javax.swing.JTable();
        editButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        translateButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(750, 500));

        parentPanel.setLayout(new java.awt.CardLayout());

        uploadFileButton.setText("Upload File");
        uploadFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadFileButtonActionPerformed(evt);
            }
        });

        uploadDescription.setText("Please upload a plain text file (.txt) to generate a vocabulary list. ");

        mainMenuTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        mainMenuTitle.setText("Language Acquisition Tool");

        javax.swing.GroupLayout mainMenuLayout = new javax.swing.GroupLayout(mainMenu);
        mainMenu.setLayout(mainMenuLayout);
        mainMenuLayout.setHorizontalGroup(
            mainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMenuLayout.createSequentialGroup()
                .addContainerGap(204, Short.MAX_VALUE)
                .addGroup(mainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMenuLayout.createSequentialGroup()
                        .addComponent(uploadDescription)
                        .addGap(185, 185, 185))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMenuLayout.createSequentialGroup()
                        .addComponent(mainMenuTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(253, 253, 253))))
            .addGroup(mainMenuLayout.createSequentialGroup()
                .addGap(286, 286, 286)
                .addComponent(uploadFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        mainMenuLayout.setVerticalGroup(
            mainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMenuLayout.createSequentialGroup()
                .addGap(81, 81, 81)
                .addComponent(mainMenuTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(uploadDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(132, 132, 132)
                .addComponent(uploadFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
        );

        parentPanel.add(mainMenu, "MainMenu");

        listTitle.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        listTitle.setText("Vocabulary List");

        VocabTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        VocabTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(VocabTable);

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        translateButton.setText("Translate");
        translateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout listLayout = new javax.swing.GroupLayout(list);
        list.setLayout(listLayout);
        listLayout.setHorizontalGroup(
            listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(deleteButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(translateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(backButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(listTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(301, 301, 301))
        );
        listLayout.setVerticalGroup(
            listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listLayout.createSequentialGroup()
                .addGroup(listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(listLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(listTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                    .addGroup(listLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(backButton)))
                .addGroup(listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(listLayout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(translateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(listLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );

        parentPanel.add(list, "List");

        getContentPane().add(parentPanel, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>                        

    private void uploadFileButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
       uploadFile(); 
      
    }                                                

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO add your handling code here:
        JPanel editPanel = new JPanel(); 
        JLabel selectWord = new JLabel("Select word: "); 
        JLabel editWord = new JLabel("Edit to: *Note that it must be one word without any special characters.");
        JComboBox words = new JComboBox(vocabWords); 
        JTextField newWord = new JTextField(5); 
        editPanel.add(selectWord); 
        editPanel.add(words); 
        editPanel.add(editWord); 
        editPanel.add(newWord); 
        int result = JOptionPane.showConfirmDialog(null, editPanel,
                "Edit Word:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
           String editThis = words.getSelectedItem().toString(); 
           String editTo = newWord.getText(); 
           Matcher matcher = pattern.matcher(editTo);
           if((matcher.find()) || editTo.contains(" ")) {
             JOptionPane.showMessageDialog(this, "Invalid word. Contains multiple words or special characters.", "Error", JOptionPane.ERROR_MESSAGE);
         }
         else {
           System.out.println("Editing: " + editThis + " To: " + editTo);
           edit(editThis, editTo); 
           }
        }
        
        
        
    }                                          

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
        JPanel addPanel = new JPanel(); 
        JLabel addWord = new JLabel("Add word: *Note that it must be one word without any special characters."); 
        JTextField newWord = new JTextField(5); 
        addPanel.add(addWord); 
        addPanel.add(newWord); 
        int result = JOptionPane.showConfirmDialog(null, addPanel,
                "Add Word:", JOptionPane.OK_CANCEL_OPTION);
         if (result == JOptionPane.OK_OPTION) { 
         String addThis = newWord.getText(); 
         Matcher matcher = pattern.matcher(addThis);
         if ((matcher.find()) || addThis.contains(" ")) {
             JOptionPane.showMessageDialog(this, "Invalid word. Contains multiple words or special characters.", "Error", JOptionPane.ERROR_MESSAGE);
         }
         else {
         System.out.println("Adding: " + addThis); 
         add(addThis); 
         }
         }

    }                                         

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // TODO add your handling code here:
        JPanel deletePanel = new JPanel(); 
        JLabel deleteWord = new JLabel("Delete word: "); 
        JComboBox words = new JComboBox (vocabWords); 
        deletePanel.add(deleteWord);
        deletePanel.add(words); 
         int result = JOptionPane.showConfirmDialog(null, deletePanel,
                "Delete Word:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int deleteThis = words.getSelectedIndex(); 
            System.out.println("Deleting: " + vocabWords[deleteThis]); 
            delete(deleteThis); 
        }
    }                                            

    private void translateButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                
        // TODO add your handling code here:
       String[] languages = {
            "Spanish",
            "French",
            "German",
            "Japanese",
            "Russian",
            "Portuguese",
            "Italian",
            "Korean", 
        };
       String[] languageAbbreviations = {
            "es",   // Spanish
            "fr",   // French
            "de",   // German
            "ja",   // Japanese
            "ru",   // Russian
            "pt",   // Portuguese
            "it",   // Italian
            "ko",   //Korean
        };  
       JPanel translatePanel = new JPanel(); 
        JLabel chooseLanguage = new JLabel("Choose language: "); 
        JComboBox lang = new JComboBox(languages); 
        translatePanel.add(chooseLanguage); 
        translatePanel.add(lang); 
        int result = JOptionPane.showConfirmDialog(null, translatePanel,
                "Choose Language of Translation:", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String language = languageAbbreviations[lang.getSelectedIndex()]; 
   

            result = JOptionPane.showConfirmDialog(null, "Would you like to download an Excel file with the translated vocabulary words?", "Excel File Download",
        JOptionPane.OK_CANCEL_OPTION); 
            if (result == JOptionPane.OK_OPTION) 
               try {
                   ChooseFile(language);
            } catch (IOException | ParseException ex) {
            } 
        }
        
    }                                               

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
         // TODO add your handling code here:
         wordList.clear(); keepList.clear(); temporary.clear(); content = ""; 
          CardLayout card = (CardLayout)parentPanel.getLayout();
       card.show(parentPanel, "MainMenu"); 
    }                                          


        
    // Variables declaration - do not modify                     
    private javax.swing.JTable VocabTable;
    private javax.swing.JButton addButton;
    private javax.swing.JButton backButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel list;
    private javax.swing.JLabel listTitle;
    private javax.swing.JPanel mainMenu;
    private javax.swing.JLabel mainMenuTitle;
    private javax.swing.JPanel parentPanel;
    private javax.swing.JButton translateButton;
    private javax.swing.JLabel uploadDescription;
    private javax.swing.JButton uploadFileButton;
    // End of variables declaration                   
}
