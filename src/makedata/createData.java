package makedata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tools.nlplib;

public class createData {
	public static final String TECH_DOC_PATH = "/data/documents/train_data/technology/";
	public static final String EDU_DOC_PATH = "/data/documents/train_data/education/";
	public static final String HEAL_DOC_PATH = "/data/documents/train_data/healthy/";

	private List<String> tech_doc = new ArrayList<String>();
	private List<String> edu_doc = new ArrayList<String>();
	private List<String> heal_doc = new ArrayList<String>();

	int document_count = 100 * 3;
	int all_keyword_size;
	int doc_keyword_count = 500;
	int numberword_count = 15;

	public File f;
	nlplib lib;

	String[] sentence = new String[256];
	String[] wordsplit = new String[sentence.length];
	String[] wordtagger = new String[sentence.length];
	String[] word = new String[256];
	String[] type_tagger = new String[10];

	List<String> temp_wordoffile = new ArrayList<String>();
	List<String> wordoffile = new ArrayList<String>();
	List<String> lstopword = new ArrayList<String>();

	public createData() {
		f = new File(".");
	}

	public class Weight {
		private String word;
		private double weight;

		public Weight() {
			this.word = "";
			this.weight = 0;
		}

		public Weight(String word, double weight) {
			this.word = word;
			this.weight = weight;
		}
	}

	public void readFile() {

		File[] files;
		// Read Technology Document
		files = new File(f.getAbsolutePath() + TECH_DOC_PATH).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				tech_doc.add(file.getAbsolutePath());
			}
		}
		files = new File(f.getAbsolutePath() + EDU_DOC_PATH).listFiles();
		// Read Education Document
		for (File file : files) {
			if (file.isFile()) {
				edu_doc.add(file.getAbsolutePath());
			}
		}
		// Read Economic Document
		files = new File(f.getAbsolutePath() + HEAL_DOC_PATH).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				heal_doc.add(file.getAbsolutePath());
			}
		}
	}

	public String[] sentSlipt(String vb) {
		String[] ret = null;
		try {
			ret = lib.detector.sentDetect(vb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String wordslipt(String cau) {
		String ret = null;
		ret = lib.tokenizer.segment(cau);
		return ret;
	}

	public String tagger(String sentence) {
		String ret = null;

		ret = lib.tagger.tagging(sentence);
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

	public void set_typetagger() {
		type_tagger[0] = "/Np";
		type_tagger[1] = "/Nc";
		type_tagger[2] = "/Nu";
		type_tagger[3] = "/Ny";
		type_tagger[4] = "/Nb";
		type_tagger[5] = "/N";
		type_tagger[6] = "/V";
	}

	public void stopword() throws FileNotFoundException, IOException {
		File stopword = new File(f.getAbsolutePath() + "/data/stopword.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(stopword))) {
			for (String line; (line = br.readLine()) != null;) {
				// process the line.
				lstopword.add(line);
			}
		}
	}

	public void extractWord(List<String> list_doc, String doc_type) throws FileNotFoundException, IOException {
		try {
			FileWriter fw;
			BufferedWriter bw = null;
			File temp = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/raw_listword.txt");
			fw = new FileWriter(temp.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			FileWriter fwl;
			BufferedWriter bwl = null;
			set_typetagger();

			for (Iterator<String> i = list_doc.iterator(); i.hasNext();) {
				temp_wordoffile.clear();

				String file = i.next();
				for (int time_run = 0; time_run < 1; time_run++) {
					File preprocessing = new File(file + "-t.raw");
					fwl = new FileWriter(preprocessing.getAbsoluteFile());
					bwl = new BufferedWriter(fwl);
					File orifile = new File(file); // dung de doc va doi ten
					// Read and check the input from the text file
					System.out.println("Reading from " + file);
					try (BufferedReader br = new BufferedReader(new FileReader(orifile.getAbsoluteFile()))) {
						String line;
						while ((line = br.readLine()) != null) {
							// process the line.
							String[] listword = line.split(" ");
							for (int x = 0; x < listword.length; x++) {

								if (listword[x].contains(".")) {
									if (listword[x].indexOf(".") + 1 == listword[x].length()) {
										bwl.write(listword[x] + " ");
										continue;
									}

									String a_letter = Character
											.toString(listword[x].charAt(listword[x].indexOf(".") + 1));
									if (!a_letter.equals(" ")) {
										StringBuilder str = new StringBuilder(listword[x]);
										str.insert(listword[x].indexOf(".") + 1, ' ');
										bwl.write(str.toString() + " ");
									}
								} else if (listword[x].contains(",")) {
									if (listword[x].indexOf(",") + 1 == listword[x].length()) {
										bwl.write(listword[x] + " ");
										continue;
									}
									String a_letter = Character
											.toString(listword[x].charAt(listword[x].indexOf(",") + 1));
									if (!a_letter.equals(" ")) {
										StringBuilder str = new StringBuilder(listword[x]);
										str.insert(listword[x].indexOf(",") + 1, ' ');
										bwl.write(str.toString() + " ");
									}
								} else
									bwl.write(listword[x] + " ");
							}

						}
					}
					bwl.close();
					if (orifile.exists())
						orifile.delete();
					// Rename file (or directory)
					preprocessing.renameTo(orifile);
					if (preprocessing.exists())
						preprocessing.renameTo(orifile);
				}
				// sc.doCheck(file);
				// Read all text and split sentence then save to array
				InputStream is = null;
				is = new FileInputStream(file);
				sentence = sentSlipt(readStream(is));
				wordtagger = new String[sentence.length];
				wordsplit = new String[sentence.length];
				for (int j = 0; j < sentence.length; j++) {
					wordsplit[j] = wordslipt(sentence[j]);
					wordtagger[j] = tagger(wordsplit[j]);
				}
				// Process every wordtagger
				for (String wtag : wordtagger) {
					// Store every word splited to array
					if (wtag != null) {
						word = wtag.split(" ");
						boolean isChoose = false;
						for (int x = 0; x < word.length; x++) {
							for (String typtag : type_tagger) {

								// Type filter
								if (typtag != null) {
									if (word[x].length() == 1)
										continue;
									if (word[x].contains(" ")) {
										word[x].replace(" ", "");
									}

									if (word[x].indexOf(typtag) > 0 && word[x].indexOf("&") < 0
											&& word[x].indexOf("+") < 0 && word[x].indexOf("\"") < 0
											&& word[x].length() > 3) {
										word[x] = word[x].replace(typtag, "");
										word[x] = word[x].toLowerCase();
										String correct_word = lib.sc.check(word[x]);
										word[x] = correct_word;
										isChoose = true;
										break;
									}

								} else
									break;
							}
							if (!temp_wordoffile.contains(word[x])) {
								if (isChoose == true) {
									// Them vao csdl
									if (lstopword.contains(word[x])) {
										isChoose = false;
										continue;
									}
									temp_wordoffile.add(word[x]);
									isChoose = false;
								}
							} else if (isChoose == true)
								isChoose = false;

						}

					}

				}
				Collections.sort(temp_wordoffile);
				for (int k = 0; k < temp_wordoffile.size(); k++) {
					bw.write(temp_wordoffile.get(k) + " ");
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
		}
	}

	public int countNumberEqual(String[] itemList, String itemToCheck) {
		int count = 0;
		for (String i : itemList) {
			if (i.equals(itemToCheck)) {
				count++;
			}
		}
		return count;
	}

	public void calcVSM() {
		try {
			FileWriter fw_listword;
			BufferedWriter bw_listword;
			FileWriter fw_weight;
			BufferedWriter bw_weight;
			set_typetagger();
			String ftech_doc;
			String fedu_doc;
			String fheal_doc;

			ftech_doc = "technology";
			fedu_doc = "education";
			fheal_doc = "healthy";

			double[] tf;
			double[] idf;
			double[] weight;
			// tf: so lan xuat hien cua 1 tu trong trong cung loai van ban
			// idf log(toan bo van ban/so van ban trong cac loai khac chua tu)
			// Technology
			String techterm = f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/terms.txt";
			String eduterm = f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/terms.txt";
			String healterm = f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/terms.txt";

			File flistword = new File(f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			File fwordweight = new File(f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			String mainlistword = f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/raw_listword.txt";
			String edulistword = f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/raw_listword.txt";
			String heallistword = f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/raw_listword.txt";

			List<String> documents = new ArrayList<String>();
			List<String> otherdocuments = new ArrayList<String>();
			List<String> tech_term = new ArrayList<String>();
			List<String> edu_term = new ArrayList<String>();
			List<String> heal_term = new ArrayList<String>();
			// Read and check the input from the text file
			try (BufferedReader br = new BufferedReader(new FileReader(mainlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					documents.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edulistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(heallistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(techterm))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					tech_term.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(eduterm))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					edu_term.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(healterm))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					heal_term.add(line.toLowerCase());
				}
			}
			for (String doc : documents) {
				if (doc != null) {
					word = doc.split(" ");
					tf = new double[word.length];
					idf = new double[word.length];
					weight = new double[word.length];
					for (int x = 0; x < word.length; x++) {
						if (word[x].contains("/")) {
							continue;
						}
						if (word[x].length() < 3)
							continue;
						int numDocumentContain = 0;
						for (String temp_doc : documents) {
							if (temp_doc.contains(word[x])) {
								numDocumentContain++;
							}
						}
						tf[x] = (double) numDocumentContain / (double) documents.size();
						int numotherDocumentContain = 0;
						for (String temp_doc : otherdocuments) {
							if (temp_doc.contains(word[x])) {
								numotherDocumentContain++;
							}
						}
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain + 1));
						weight[x] = tf[x] * idf[x];
						String[] wordinterm = word[x].split("_");
						for (String w : wordinterm) {
							if (tech_term.contains(w.toLowerCase())) {
								weight[x] = 10;
								break;
							} else if (edu_term.contains(w.toLowerCase()) || heal_term.contains(w.toLowerCase())) {
								weight[x] = 0;
								break;
							} else {
								weight[x] = tf[x] * idf[x];
								continue;
							}

						}

					}
					for (int i = 0; i < word.length - 1; i++)
						for (int j = word.length - 1; j > i; j--)
							if (weight[j] > weight[j - 1]) {
								double temp = weight[j - 1];
								weight[j - 1] = weight[j];
								weight[j] = temp;
								String wtemp = word[j - 1];
								word[j - 1] = word[j];
								word[j] = wtemp;
							}
					int max = numberword_count;
					for (int k = 0; k < max; k++) {
						bw_listword.write(word[k] + " ");
					}
					for (int k = 0; k < max; k++) {
						bw_weight.write(weight[k] + " ");
					}
					bw_listword.write("\n");
					bw_weight.write("\n");
				}
			}
			bw_listword.close();
			bw_weight.close();

			// Education

			flistword = new File(f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			fwordweight = new File(f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			mainlistword = f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/raw_listword.txt";
			String techlistword = f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/raw_listword.txt";
			heallistword = f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/raw_listword.txt";

			documents = new ArrayList<String>();
			otherdocuments = new ArrayList<String>();
			// Read and check the input from the text file
			try (BufferedReader br = new BufferedReader(new FileReader(mainlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					documents.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(techlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(heallistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line);
				}
			}
			for (String doc : documents) {
				if (doc != null) {
					word = doc.split(" ");
					tf = new double[word.length];
					idf = new double[word.length];
					weight = new double[word.length];
					for (int x = 0; x < word.length; x++) {
						if (word[x].contains("/")) {
							continue;
						}
						if (word[x].length() < 3)
							continue;
						int numDocumentContain = 0;
						for (String temp_doc : documents) {
							if (temp_doc.contains(word[x])) {
								numDocumentContain++;
							}
						}
						tf[x] = (double) numDocumentContain / (double) documents.size();
						int numotherDocumentContain = 0;
						for (String temp_doc : otherdocuments) {
							if (temp_doc.contains(word[x])) {
								numotherDocumentContain++;
							}
						}
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain + 1));
						weight[x] = tf[x] * idf[x];
						String[] wordinterm = word[x].split("_");
						for (String w : wordinterm) {
							if (edu_term.contains(w.toLowerCase())) {
								weight[x] = 10;
								break;
							} else if (heal_term.contains(w.toLowerCase()) || tech_term.contains(w.toLowerCase())) {
								weight[x] = 0;
								break;
							} else
								continue;
						}
					}
					for (int i = 0; i < word.length - 1; i++)
						for (int j = word.length - 1; j > i; j--)
							if (weight[j] > weight[j - 1]) {
								double temp = weight[j - 1];
								weight[j - 1] = weight[j];
								weight[j] = temp;
								String wtemp = word[j - 1];
								word[j - 1] = word[j];
								word[j] = wtemp;
							}
					int max = numberword_count;
					for (int k = 0; k < max; k++) {
						bw_listword.write(word[k] + " ");
					}
					for (int k = 0; k < max; k++) {
						bw_weight.write(weight[k] + " ");
					}
					bw_listword.write("\n");
					bw_weight.write("\n");
				}
			}
			bw_listword.close();
			bw_weight.close();
			// Healthy

			flistword = new File(f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			fwordweight = new File(f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			mainlistword = f.getAbsolutePath() + "/data/trainning/" + fheal_doc + "/raw_listword.txt";
			techlistword = f.getAbsolutePath() + "/data/trainning/" + ftech_doc + "/raw_listword.txt";
			edulistword = f.getAbsolutePath() + "/data/trainning/" + fedu_doc + "/raw_listword.txt";

			documents = new ArrayList<String>();
			otherdocuments = new ArrayList<String>();
			// Read and check the input from the text file
			try (BufferedReader br = new BufferedReader(new FileReader(mainlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					documents.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(techlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line.toLowerCase());
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edulistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					otherdocuments.add(line.toLowerCase());
				}
			}
			for (String doc : documents) {
				if (doc != null) {
					word = doc.split(" ");
					tf = new double[word.length];
					idf = new double[word.length];
					weight = new double[word.length];
					for (int x = 0; x < word.length; x++) {
						if (word[x].contains("/")) {
							continue;
						}
						if (word[x].length() < 3)
							continue;
						int numDocumentContain = 0;
						for (String temp_doc : documents) {
							if (temp_doc.contains(word[x])) {
								numDocumentContain++;
							}
						}
						tf[x] = (double) numDocumentContain / (double) documents.size();
						int numotherDocumentContain = 0;
						for (String temp_doc : otherdocuments) {
							if (temp_doc.contains(word[x])) {
								numotherDocumentContain++;
							}
						}
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain + 1));
						weight[x] = tf[x] * idf[x];
						String[] wordinterm = word[x].split("_");
						for (String w : wordinterm) {
							if (heal_term.contains(w.toLowerCase())) {
								weight[x] = 10;
								break;
							} else if (edu_term.contains(w.toLowerCase()) || tech_term.contains(w.toLowerCase())) {
								weight[x] = 0;
								break;
							} else
								continue;
						}

					}
					for (int i = 0; i < word.length - 1; i++)
						for (int j = word.length - 1; j > i; j--)
							if (weight[j] > weight[j - 1]) {
								double temp = weight[j - 1];
								weight[j - 1] = weight[j];
								weight[j] = temp;
								String wtemp = word[j - 1];
								word[j - 1] = word[j];
								word[j] = wtemp;
							}
					int max = numberword_count;
					for (int k = 0; k < max; k++) {
						bw_listword.write(word[k] + " ");
					}
					for (int k = 0; k < max; k++) {
						bw_weight.write(weight[k] + " ");
					}
					bw_listword.write("\n");
					bw_weight.write("\n");
				}
			}
			bw_listword.close();
			bw_weight.close();

		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
		}
	}

	public void calcVSM(String doc_type) {
		try {
			FileWriter fw_listword;
			BufferedWriter bw_listword;
			FileWriter fw_weight;
			BufferedWriter bw_weight;
			set_typetagger();

			File flistword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			String file = f.getAbsolutePath() + "/data/trainning/" + doc_type + "/raw_listword.txt";

			File fwordweight = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);
			double[] tf;
			double[] idf;
			double[] weight;
			List<String> documents = new ArrayList<String>();
			// Read and check the input from the text file
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					documents.add(line);
				}
			}
			for (String doc : documents) {
				if (doc != null) {
					word = doc.split(" ");
					tf = new double[word.length];
					idf = new double[word.length];
					weight = new double[word.length];
					for (int x = 0; x < word.length; x++) {
						if (word[x].contains("/")) {
							continue;
						}
						tf[x] = (double) countNumberEqual(word, word[x]) / (double) word.length;
						int numDocumentContain = 0;
						for (String temp_doc : documents) {
							if (temp_doc.contains(word[x])) {
								numDocumentContain++;
							}
						}
						idf[x] = Math.log10(documents.size() / (numDocumentContain));
						weight[x] = tf[x] * idf[x];
					}
					for (int i = 0; i < word.length - 1; i++)
						for (int j = word.length - 1; j > i; j--)
							if (weight[j] > weight[j - 1]) {
								double temp = weight[j - 1];
								weight[j - 1] = weight[j];
								weight[j] = temp;
								String wtemp = word[j - 1];
								word[j - 1] = word[j];
								word[j] = wtemp;
							}
					int max = numberword_count;
					for (int k = 0; k < max; k++) {
						bw_listword.write(word[k] + " ");
					}
					for (int k = 0; k < max; k++) {
						bw_weight.write(weight[k] + " ");
					}
					bw_listword.write("\n");
					bw_weight.write("\n");
				}
			}
			bw_listword.close();
			bw_weight.close();
		} catch (

		IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            get keyword of document type
	 * @throws IOException
	 */
	public void getkeyword(String doc_type) throws IOException {
		List<String> doc_keyword = new ArrayList<String>();
		FileWriter fw;
		BufferedWriter bw;
		File listofword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/tf-idf_listword.txt");
		File weightofword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/weight.txt");
		File keyword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/keyword.txt");
		fw = new FileWriter(keyword.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		Weight[] word_weight;
		word_weight = new Weight[document_count * numberword_count];
		for (int i = 0; i < document_count * numberword_count; i++) {
			word_weight[i] = new Weight();
			word_weight[i].weight = 0;
			word_weight[i].word = "";
		}
		int index = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(listofword.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					word_weight[index].word = listword[x];
					index++;
				}

			}
		}
		index = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(weightofword.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					word_weight[index].weight = Double.parseDouble(listword[x]);
					index++;
				}

			}
		}
		index = 0;
		// Kiem tra lai danh sach de loai tu trung lap
		try (BufferedReader br = new BufferedReader(new FileReader(listofword.getAbsoluteFile()))) {
			String line;
			int line_index = 0;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					if (doc_keyword.contains(listword[x])) { // Neu da ton tai
						// Word 2 cua hang 2 se co gia vi tri trong word_weight
						// = 1*10(so luong word) + 2 = 12 // thi xoa
						word_weight[line_index * numberword_count + x].word = "";
						word_weight[line_index * numberword_count + x].weight = 0;
					} else
						doc_keyword.add(listword[x]);
				}
				line_index++;
			}
		}
		// Sap xep
		for (int i = 0; i < document_count * numberword_count - 1; i++)
			for (int j = document_count * numberword_count - 1; j > i; j--)
				if (word_weight[j].weight > word_weight[j - 1].weight) {
					Weight temp = word_weight[j - 1];
					word_weight[j - 1] = word_weight[j];
					word_weight[j] = temp;
				}
		for (int k = 0; k < doc_keyword.size(); k++) {
			bw.write(word_weight[k].word + " ");
		}
		bw.close();

	}

	public void maketrainningkey() throws FileNotFoundException, IOException {
		File tech_key = new File(f.getAbsolutePath() + "/data/trainning/technology/keyword.txt");
		File edu_key = new File(f.getAbsolutePath() + "/data/trainning/education/keyword.txt");
		File fash_key = new File(f.getAbsolutePath() + "/data/trainning/healthy/keyword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");

		List<String> all_keyword = new ArrayList<String>();
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(all_key.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		try (BufferedReader br = new BufferedReader(new FileReader(tech_key.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					if (!all_keyword.contains(listword[x])) {
						all_keyword.add(listword[x]);
					}
				}

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(edu_key.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					if (!all_keyword.contains(listword[x])) {
						all_keyword.add(listword[x]);
					}
				}

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fash_key.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					if (!all_keyword.contains(listword[x])) {
						all_keyword.add(listword[x]);
					}
				}
			}
		}
		java.util.Collections.sort(all_keyword);
		for (int k = 0; k < all_keyword.size(); k++) {
			bw.write(all_keyword.get(k) + " ");
		}
		all_keyword_size = all_keyword.size();
		bw.close();

	}

	public void create_weka_data() throws IOException {
		File tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/tf-idf_listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/tf-idf_listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/trainning/healthy/tf-idf_listword.txt");
		File bayes_matrix = new File(f.getAbsolutePath() + "/data/trainning/data.arff");
		File trainningkey = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		List<String> all_keyword = new ArrayList<String>();
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(bayes_matrix.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		bw.write("@relation document_classify\n");
		bw.write("\n");
		bw.write("@attribute class {technology, education, healthy}\n");
		try (BufferedReader br = new BufferedReader(new FileReader(trainningkey.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					all_keyword.add(listword[x]);
				}
			}
		}
		java.util.Collections.sort(all_keyword);
		for (int i = 0; i < all_keyword.size(); i++)
			bw.write("@attribute " + all_keyword.get(i) + " numeric\n");

		bw.write("\n");
		bw.write("@data\n");

		try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw.write("{0 technology");
				Arrays.sort(listword);
				for (int x = 0; x < listword.length; x++) {
					if (x < listword.length - 1) {
						if (all_keyword.indexOf(listword[x]) > 0) {
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1");
						}
					} else if (x == listword.length - 1)
						if (all_keyword.indexOf(listword[x]) > 0)
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1}");
						else
							bw.write(" }");
				}
				bw.write("\n");

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw.write("{0 education");
				Arrays.sort(listword);
				for (int x = 0; x < listword.length; x++) {
					if (x < listword.length - 1) {
						if (all_keyword.indexOf(listword[x]) > 0) {
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1");
						}
					} else if (x == listword.length - 1)
						if (all_keyword.indexOf(listword[x]) > 0)
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1}");
						else
							bw.write(" }");
				}
				bw.write("\n");

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw.write("{0 healthy");
				Arrays.sort(listword);
				for (int x = 0; x < listword.length; x++) {
					if (x < listword.length - 1) {
						if (all_keyword.indexOf(listword[x]) > 0) {
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1");
						}
					} else if (x == listword.length - 1)
						if (all_keyword.indexOf(listword[x]) > 0)
							bw.write("," + all_keyword.indexOf(listword[x]) + " 1}");
						else
							bw.write(" }");
				}
				bw.write("\n");
			}
		}
		bw.close();
	}

	public void creatematrix_knn() throws FileNotFoundException, IOException {
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/tf-idf_listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/tf-idf_listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/trainning/healthy/tf-idf_listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fknn_matrix = new File(f.getAbsolutePath() + "/data/trainning/knn_matrix.txt");
		File fknn_label = new File(f.getAbsolutePath() + "/data/trainning/knn_label.txt");
		FileWriter fw_matrix;
		BufferedWriter bw_matrix;
		fw_matrix = new FileWriter(fknn_matrix.getAbsoluteFile());
		bw_matrix = new BufferedWriter(fw_matrix);

		FileWriter fw_label;
		BufferedWriter bw_label;
		fw_label = new FileWriter(fknn_label.getAbsoluteFile());
		bw_label = new BufferedWriter(fw_label);
		try (BufferedReader br = new BufferedReader(new FileReader(all_key.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					list_allkeyword.add(listword[x]);
				}

			}
		}

		int knn_matrix[][] = new int[document_count][];
		int knn_label[] = new int[document_count];
		all_keyword_size = list_allkeyword.size();
		for (int i = 0; i < document_count; i++) {
			knn_matrix[i] = new int[all_keyword_size];
		}
		for (int i = 0; i < document_count; i++)
			for (int j = 0; j < all_keyword_size; j++) {
				knn_matrix[i][j] = 0;
			}

		try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			int k = 0;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index != -1) {
						knn_matrix[k][index] = 1;
					}

				}
				knn_label[k] = 1;
				k++;

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			int k = document_count / 3;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index != -1) {
						knn_matrix[k][index] = 1;
					}

				}
				knn_label[k] = 2;
				System.out.println(k);
				k++;

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			int k = 2 * document_count / 3;
			while ((line = br.readLine()) != null) {
				// process the line.
				if (k == document_count)
					break;
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index != -1) {
						knn_matrix[k][index] = 1;
					}
				}
				knn_label[k] = 3;
				k++;
			}
		}
		for (int i = 0; i < document_count; i++) {
			for (int j = 0; j < all_keyword_size; j++) {
				bw_matrix.write(Integer.toString(knn_matrix[i][j]) + " ");
				if (j == all_keyword_size - 1)
					bw_matrix.write("\n");
			}
		}
		for (int i = 0; i < document_count; i++)
			bw_label.write(Integer.toString(knn_label[i]) + "\n");
		bw_matrix.close();
		bw_label.close();
	}

	public void creatematrix_svm() throws IOException {
		//
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/tf-idf_listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/tf-idf_listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/trainning/healthy/tf-idf_listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fsvm_matrix = new File(f.getAbsolutePath() + "/data/trainning/svm_matrix.txt");
		FileWriter fw_matrix;
		BufferedWriter bw_matrix;
		fw_matrix = new FileWriter(fsvm_matrix.getAbsoluteFile());
		bw_matrix = new BufferedWriter(fw_matrix);

		try (BufferedReader br = new BufferedReader(new FileReader(all_key.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					list_allkeyword.add(listword[x]);
				}

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw_matrix.write("1 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw_matrix.write(index + ":1 ");
					}
				}
				bw_matrix.write("\n");

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw_matrix.write("2 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw_matrix.write(index + ":1 ");
					}
				}
				bw_matrix.write("\n");
			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				bw_matrix.write("3 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw_matrix.write(index + ":1 ");
					}
				}
				bw_matrix.write("\n");
			}
		}
		bw_matrix.close();

	}

	public void createTrainingData() throws IOException {
		stopword();
		extractWord(tech_doc, "technology");
		extractWord(edu_doc, "education");
		extractWord(heal_doc, "healthy");
		calcVSM();
		getkeyword("technology");
		getkeyword("education");
		getkeyword("healthy");
		maketrainningkey();
		creatematrix_knn();
		create_weka_data();
		creatematrix_svm();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public void FcreateData(nlplib lib) throws IOException {
		this.lib = lib;
		readFile();
		createTrainingData();
	}

}
