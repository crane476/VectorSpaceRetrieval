/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vectorspaceretrieval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author crane476
 */
public class VectorSpaceRetrieval {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner input = new Scanner(System.in);
        System.out.print("Enter a File Directory: ");
        String inDirectory = input.nextLine();
        System.out.print("Enter Location of Query Document: ");
        String queryDoc = input.nextLine();
        System.out.print("Specify Number of Documents to Return: ");
        int k = input.nextInt();
        File fileNames = new File(inDirectory);
        File fList[] = fileNames.listFiles();
        ArrayList<String> filePaths = new ArrayList<>();
        for (int i = 0; i < fList.length; i++) {
            filePaths.add(fList[i].toString());
        }
        ArrayList<String> stopWords = getStopWords();
        indexFiles(filePaths, stopWords);
    }

    public static ArrayList<String> getStopWords() {
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            stopWords = (ArrayList<String>) Files.readAllLines(Paths.get("src\\vectorspaceretrieval\\stopwords.txt"));
        } catch (IOException ex) {
        }
        for (int i = 0; i < stopWords.size(); i++) {
            System.out.println(stopWords.get(i));
        }
        return stopWords;
    }

    public static String processSpecialCharacters(String word) {
        word = word.replaceAll("[+^&:\\[\\],\"/();]", "");
        int length = word.length();
        if (word.contains("'")) {
            if (word.charAt(length - 2) == '\'') {
                if (word.charAt(length - 1) == 's') {
                    word = word.replace("'", "");
                }
            } else if (word.charAt(length - 1) == '\'') {
                word = word.replace("'", "");
            }
        } else if (word.contains(".")) {
            char[] array = word.toCharArray();
            int count = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] == '.') {
                    count++;
                }
            }
            if (count == 1 && word.charAt(length - 1) == '.') {
                word = word.replace(".", "");
            }
        } else if (word.equals("-")) {
            word = word.replace("-", "");
        }
        return word;
    }

    public static void indexFiles(ArrayList<String> files, ArrayList<String> stopWords) {
        String newToken;
        HashMap<String, HashMap<Integer, Integer>> index = new HashMap<>(); //List of tokens which map to posting
        HashMap<Integer, Integer> posting; //list of document ID's which map to number of occurrences
        int count = 0;
        HashMap<Integer, Integer> wordCount = new HashMap<>(); //list of documents and total number of words they contain
        for (int i = 0; i < files.size(); i++) {
            try {
                Scanner fileIn = new Scanner(new File(files.get(i)));
                while (fileIn.hasNext()) {
                    newToken = fileIn.next();
                    newToken = newToken.toLowerCase();
                    newToken = processSpecialCharacters(newToken);
                    if (!stopWords.contains(newToken) && !newToken.equals("")) {
                        if (index.containsKey(newToken)) {
                            if (index.get(newToken).containsKey(i)) { //if term has already been encountered in this document
                                HashMap<Integer, Integer> newPosting = (HashMap) index.get(newToken).clone(); //shallow copy of posting
                                newPosting.put(i, newPosting.get(i) + 1); //update number of occurrences
                                index.put(newToken, newPosting); //overwrite posting with new one
                            } else { //term has been encountered, but not in current document
                                HashMap<Integer, Integer> newPosting = (HashMap) index.get(newToken).clone();
                                newPosting.put(i, 1);
                                index.put(newToken, newPosting);
                            }
                        } else { //term has not yet been encountered
                            posting = new HashMap<>();
                            posting.put(i, 1);
                            index.put(newToken, posting);
                        }
                    }
                    count++;
                }
                wordCount.put(i, count);
                count = 0;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VectorSpaceRetrieval.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
