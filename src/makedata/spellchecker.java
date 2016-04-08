package makedata;
/*
 * spellchecker.java
 *
 * Author: Shyam Shankar <syamsankar91@gmail.com>
 * Licensed under GPL Version 3
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class spellchecker {

	Hashtable<String, String> dictionary; // To store all the words of the
											// dictionary
	boolean suggestWord; // To indicate whether the word is spelled correctly or
							// not.
	spellingsuggest suggest;

	public spellchecker() {
		dictionary = new Hashtable<String, String>();
		System.out.println("******Welcome to the spell checker using Hashtable*****");
		System.out.println(
				"The spell checker would check every line from the input file and then give suggestions if needed after each line. \n\n");

		try {
			File f = new File(".");
			// Read and store the words of the dictionary
			BufferedReader dictReader = new BufferedReader(
					new FileReader(f.getAbsolutePath() + "/data/tools/spell_checker/dictionary.txt"));

			while (dictReader.ready()) {
				String dictInput = dictReader.readLine();
				String[] dict = dictInput.split("\\s");

				for (int i = 0; i < dict.length; i++) {
					// key and value are identical
					dictionary.put(dict[i], dict[i]);
				}
			}
			dictReader.close();
			// Initializing a spelling suggest object
			suggest = new spellingsuggest(
					f.getAbsolutePath() + "/data/tools/spell_checker/wordprobabilityDatabase.txt");

		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
		}
	}

	public void doCheck(List<String> list_doc) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			for (Iterator<String> i = list_doc.iterator(); i.hasNext();) {
				// Get file dir
				String file = i.next();
				File orifile = new File(file);
				// Create temp file
				File temp = new File(file + "-t.txt");
				fw = new FileWriter(temp.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				// Read and check the input from the text file
				BufferedReader inputFile = new BufferedReader(new FileReader(file));
				System.out.println("Reading from " + file);

				// Reads input lines one by one
				while (inputFile.ready()) {
					String s = inputFile.readLine();
					System.out.println(s);
					String[] result = s.split("\\s");

					for (int x = 0; x < result.length; x++) {
						suggestWord = true;
						String outputWord = checkWord(result[x]);

						if (suggestWord) {
							System.out.println(
									"Suggestions for " + result[x] + " are:  " + suggest.correct(outputWord) + "\n");
							String correctword = suggest.correct(outputWord);
							if (correctword.length() > 6) { // error
								bw.write(outputWord + " ");
							} else
								bw.write(suggest.correct(outputWord) + " ");
						} else {
							bw.write(outputWord + " ");
						}
					}
				}
				bw.close();
				if (orifile.exists()) {
					boolean success = orifile.delete();
					if (success) {

					}
				}
				// Rename file (or directory)
				boolean success = temp.renameTo(orifile);

				if (!success) {
					// File was not successfully renamed
				}

				inputFile.close();
			}
		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
		}
	}

	public String checkWord(String wordToCheck) {
		String wordCheck, unpunctWord;
		String word = wordToCheck.toLowerCase();

		// if word is found in dictionary then it is spell correctly, so return
		// as it is.
		// note: inflections like "es","ing" provided in the dictionary itself.
		if ((wordCheck = dictionary.get(word)) != null) {
			suggestWord = false; // no need to ask for suggestion for a correct
									// word.
			return wordCheck;
		}

		// Removing punctuation at end of word and giving it a shot ("." or "."
		// or "?!")
		int length = word.length();

		// Checking for the beginning of quotes(example: "she )
		if (length > 1 && word.substring(0, 1).equals("\"")) {
			unpunctWord = word.substring(1, length);

			if ((wordCheck = dictionary.get(unpunctWord)) != null) {
				suggestWord = false; // no need to ask for suggestion for a
										// correct word.
				return wordCheck;
			} else // not found
				return unpunctWord; // removing the punctuation and returning
		}

		// Checking if "." or ",",etc.. at the end is the problem(example: book.
		// when book is present in the dictionary).
		if (word.substring(length - 1).equals(".") || word.substring(length - 1).equals(",")
				|| word.substring(length - 1).equals("!") || word.substring(length - 1).equals(";")
				|| word.substring(length - 1).equals(":")) {
			unpunctWord = word.substring(0, length - 1);

			if ((wordCheck = dictionary.get(unpunctWord)) != null) {
				suggestWord = false; // no need to ask for suggestion for a
										// correct word.
				return wordCheck;
			} else {
				return unpunctWord; // removing the punctuation and returning
			}
		}

		// Checking for "!\"",etc ... in the problem (example: watch!" when
		// watch is present in the dictionary)
		if (length > 2 && word.substring(length - 2).equals(",\"") || word.substring(length - 2).equals(".\"")
				|| word.substring(length - 2).equals("?\"") || word.substring(length - 2).equals("!\"")) {
			unpunctWord = word.substring(0, length - 2);

			if ((wordCheck = dictionary.get(unpunctWord)) != null) {
				suggestWord = false; // no need to ask for suggestion for a
										// correct word.
				return wordCheck;
			} else // not found
				return unpunctWord; // removing the inflections and returning
		}

		// After all these checks too, word could not be corrected, hence it
		// must be misspelt word. return and ask for suggestions
		return word;
	}

}
