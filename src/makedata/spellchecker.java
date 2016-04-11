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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class spellchecker {

	Hashtable<String, String> dictionary; // To store all the words of the
											// dictionary
	boolean suggestWord; // To indicate whether the word is spelled correctly or
							// not.
	spellingsuggest suggest;

	File f = new File(".");

	// Check if sentence has special symbol like ; , . ! ? ...
	boolean hasSpecialSymbol;

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

	public String[] sentSlipt(String vb) {
		String[] ret = null;
		try {
			SentenceDetector detector = new SentenceDetector(
					f.getAbsolutePath() + "/data/tools/NLPTools/models/sentDetection/VietnameseSD.bin.gz");
			ret = detector.sentDetect(vb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String wordslipt(String cau) {
		String ret = null;
		VietTokenizer tokenizer = new VietTokenizer(f.getAbsolutePath() + "/data/tools/NLPTools/tokenizer.properties");
		ret = tokenizer.segment(cau);
		return ret;
	}

	public static String readStream(InputStream is) {
		StringBuilder sb = new StringBuilder(512);
		try {
			Reader r = new InputStreamReader(is, "UTF-8");
			int c = 0;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

	public void doCheck(List<String> list_doc) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			for (Iterator<String> i = list_doc.iterator(); i.hasNext();) {
				// Get file dir
				String file = i.next();
				// Create
				File orifile = new File(file);
				// Create temp file
				File temp = new File(file + "-t.txt");
				fw = new FileWriter(temp.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				// Read and check the input from the text file
				BufferedReader inputFile = new BufferedReader(new FileReader(file));
				System.out.println("Reading from " + file);
				// Read all text and split sentence then save to array
				InputStream is = null;
				is = new FileInputStream(file);
				String sentence[] = sentSlipt(readStream(is));
				// Create word split array (original)
				String[] wordsplit = new String[sentence.length];
				// Create array that store every word after split by space
				String[] word = new String[256];
				// Store in array
				for (int j = 0; j < sentence.length; j++) {
					wordsplit[j] = wordslipt(sentence[j]);
				}
				// Process every wordsplit
				for (String wl : wordsplit) {
					// Store every word splited to array
					word = wl.split(" ");

					for (int x = 0; x < word.length; x++) {
						System.out.println(word[x]);
						suggestWord = true;
						hasSpecialSymbol = false;
						String outputWord = checkWord(word[x]);
						// if outputWord length = 1 -> symbol -> no need suggest
						if (outputWord.length() == 1) {
							suggestWord = false;
							hasSpecialSymbol = true;
						}

						String result = outputWord.replace('_', ' ');
						// System.out.println("outputWord = " +
						// outputWord.replace('_', ' '));
						if (suggestWord) {
							// System.out.println(
							// "Suggestions for " + word[x] + " are: " +
							// suggest.correct(outputWord) + "\n");
							String correctword = suggest.correct(outputWord);
							String correct_result = correctword.replace('_', ' ');
							if (correctword.length() > 10) { // error
								if (hasSpecialSymbol)
									bw.write(result);
								else
									bw.write(result + " ");
							} else if (hasSpecialSymbol)
								bw.write(correct_result);
							else
								bw.write(correct_result + " ");
						} else {
							if (hasSpecialSymbol)
								bw.write(result);
							else
								bw.write(result + " ");
						}
					}
				}
				bw.close();
				if (orifile.exists())
					orifile.delete();
				// Rename file (or directory)
				boolean success = temp.renameTo(orifile);
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
		if (length > 2) {
			if (word.substring(length - 2).equals(",\"") || word.substring(length - 2).equals(".\"")
					|| word.substring(length - 2).equals("?\"") || word.substring(length - 2).equals("!\"")) {
				unpunctWord = word.substring(0, length - 2);

				if ((wordCheck = dictionary.get(unpunctWord)) != null) {
					suggestWord = false; // no need to ask for suggestion for a
											// correct word.
					return wordCheck;
				} else // not found
					return unpunctWord; // removing the inflections and
										// returning
			}
		}

		// After all these checks too, word could not be corrected, hence it
		// must be misspelt word. return and ask for suggestions
		return word;
	}

}
