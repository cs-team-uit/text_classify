package spellcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class word_spellcheck {
	List<String> dictionary;
	int[][] table;
	int[] distance;
	String[] listword;
	File f = new File(".");

	public word_spellcheck() {
		dictionary = new ArrayList<String>();
		try {
			File f = new File(".");
			String dict = f.getAbsolutePath() + "/data/tools/spell_checker/dictionary.txt";
			try (BufferedReader br = new BufferedReader(new FileReader(dict))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					dictionary.add(line);
				}
			}
			distance = new int[dictionary.size()];
			listword = new String[dictionary.size()];
			// Initializing a spelling sugget object
		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
		}
	}

	private int min(int a, int b, int c) {
		if (a < b && a < c)
			return a;
		else if (b < c && b < a)
			return b;
		else
			return c;

	}

	private int sub(char a, char b) {
		if (a == b)
			return 0;
		else
			return 2;
	}

	public String check(String wordToCheck) {
		String word = wordToCheck.toLowerCase();
		if (dictionary.indexOf(word) > 0)
			return word;
		int n = word.length();
		table = new int[n + 1][];
		for (int i = 0; i < dictionary.size(); i++) {
			String compare_word = dictionary.get(i);
			int m = compare_word.length();
			for (int ii = 0; ii < n + 1; ii++)
				table[ii] = new int[m + 1];
			table[0][0] = 0;
			for (int ii = 0; ii < n + 1; ii++)
				table[ii][0] = ii;
			for (int jj = 0; jj < m + 1; jj++)
				table[0][jj] = jj;
			for (int ii = 1; ii <= n; ii++) {
				for (int jj = 1; jj <= m; jj++) {
					table[ii][jj] = min(table[ii - 1][jj] + 1, table[ii][jj - 1] + 1,
							table[ii - 1][jj - 1] + sub(word.charAt(ii - 1), compare_word.charAt(jj - 1)));
				}
			}
			distance[i] = table[n][m];
			listword[i] = compare_word;
		}
		int min = distance[0];
		String result = listword[0];
		for (int ii = 1; ii < dictionary.size(); ii++) {
			if (distance[ii] < min) {
				min = distance[ii];
				result = listword[ii];
			}
		}
		return result;

	}
}