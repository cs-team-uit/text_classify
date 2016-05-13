package predictdata;

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import models.GibbsSamplingLDA;
import tools.nlplib;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;;

public class predictData {
	// KNN area
	private double[][] instances;
	private String[] label;
	private String[] correct_label;

	File f = new File(".");
	private int document_count = 100 * 3;
	private int test_document_count = 30 * 3;
	public int all_keyword_size;
	public static final String TECH_DOC_PATH = "/data/documents/test_data/technology/";
	public static final String EDU_DOC_PATH = "/data/documents/test_data/education/";
	public static final String HEAL_DOC_PATH = "/data/documents/test_data/healthy/";
	private List<String> tech_doc = new ArrayList<String>();
	private List<String> edu_doc = new ArrayList<String>();
	private List<String> heal_doc = new ArrayList<String>();
	nlplib lib;
	int numberword_count = 20;

	String sentence[] = new String[256];
	String[] wordsplit = new String[sentence.length];
	// Create array that store every word after split by space
	String[] wordtagger = new String[sentence.length];
	// Store in array
	String[] word = new String[256];
	String[] select_temp_tagger = new String[256];

	String[] type_tagger = new String[10];

	List<String> temp_wordoffile = new ArrayList<String>();
	List<String> wordoffile = new ArrayList<String>();
	List<String> lstopword = new ArrayList<String>();

	public predictData() {
		f = new File("");
	}

	private void readFile() {

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

	public boolean isAlpha(String name) {
		char[] chars = name.toCharArray();

		for (char c : chars) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}

		return true;
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

	private void extractWord(List<String> list_doc, String doc_type) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			set_typetagger();

			FileWriter fwl;
			BufferedWriter bwl;
			File temp = new File(f.getAbsolutePath() + "/data/testing/" + doc_type + "/raw_listword.txt");
			fw = new FileWriter(temp.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			for (Iterator<String> i = list_doc.iterator(); i.hasNext();) {
				// Get file dir
				temp_wordoffile.clear();
				wordoffile.clear();
				String file = i.next();
				for (int time_run = 0; time_run < 2; time_run++) {
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
									if (lstopword.contains(listword[x])) {
										continue;
									} else if (listword[x].indexOf(".") + 1 == listword[x].length())
										continue;
									String a_letter = Character
											.toString(listword[x].charAt(listword[x].indexOf(".") + 1));
									if (!a_letter.equals(" ")) {
										StringBuilder str = new StringBuilder(listword[x]);
										str.insert(listword[x].indexOf(".") + 1, ' ');
										bwl.write(str.toString() + " ");
									}
								} else if (listword[x].contains(",")) {
									if (listword[x].indexOf(",") + 1 == listword[x].length())
										continue;
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
				// Read and check the input from the text file
				System.out.println("Reading from " + file);
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
						boolean isChoose = false; // Xac nhan word da dc xet
													// va
													// dc
													// chon
						for (int x = 0; x < word.length; x++) {

							for (String typtag : type_tagger) {
								// Type filter
								String correct_word = lib.sc.check(word[x]);
								word[x] = correct_word;
								if (typtag != null) {
									if (word[x].contains(" ")) {
										word[x].replace(" ", "");
									}
									if (word[x].indexOf(typtag) > 0 && word[x].indexOf("&") < 0
											&& word[x].indexOf("+") < 0 && word[x].indexOf("\"") < 0
											&& word[x].length() > 1) {
										word[x] = word[x].replace(typtag, "");
										word[x] = word[x].toLowerCase();
										isChoose = true;
										break;
									}
								} else
									break;
							}
							// If chua co trong csdl, them vao , nguoc lai
							// bo
							// qua
							if (!temp_wordoffile.contains(word[x])) {
								if (isChoose == true) {
									// Them vao csdl
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
		} catch (

		IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
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

	public void swapWeight(double a, double b) {
		double temp = a;
		a = b;
		b = temp;
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

			File flistword = new File(f.getAbsolutePath() + "/data/testing/" + ftech_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			File fwordweight = new File(f.getAbsolutePath() + "/data/testing/" + ftech_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			String mainlistword = f.getAbsolutePath() + "/data/testing/" + ftech_doc + "/raw_listword.txt";
			String edulistword = f.getAbsolutePath() + "/data/testing/" + fedu_doc + "/raw_listword.txt";
			String heallistword = f.getAbsolutePath() + "/data/testing/" + fheal_doc + "/raw_listword.txt";

			List<String> documents = new ArrayList<String>();
			List<String> otherdocuments = new ArrayList<String>();
			// Read and check the input from the text file
			try (BufferedReader br = new BufferedReader(new FileReader(mainlistword))) {
				for (String line; (line = br.readLine()) != null;) {
					// process the line.
					documents.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edulistword))) {
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
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain));
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

			// Education

			flistword = new File(f.getAbsolutePath() + "/data/testing/" + fedu_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			fwordweight = new File(f.getAbsolutePath() + "/data/testing/" + fedu_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			mainlistword = f.getAbsolutePath() + "/data/testing/" + fedu_doc + "/raw_listword.txt";
			String techlistword = f.getAbsolutePath() + "/data/testing/" + ftech_doc + "/raw_listword.txt";
			heallistword = f.getAbsolutePath() + "/data/testing/" + fheal_doc + "/raw_listword.txt";

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
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain));
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
			// Healthy

			flistword = new File(f.getAbsolutePath() + "/data/testing/" + fheal_doc + "/tf-idf_listword.txt");
			fw_listword = new FileWriter(flistword.getAbsoluteFile());
			bw_listword = new BufferedWriter(fw_listword);
			fwordweight = new File(f.getAbsolutePath() + "/data/testing/" + fheal_doc + "/weight.txt");
			fw_weight = new FileWriter(fwordweight.getAbsoluteFile());
			bw_weight = new BufferedWriter(fw_weight);

			mainlistword = f.getAbsolutePath() + "/data/testing/" + fheal_doc + "/raw_listword.txt";
			techlistword = f.getAbsolutePath() + "/data/testing/" + ftech_doc + "/raw_listword.txt";
			edulistword = f.getAbsolutePath() + "/data/testing/" + fedu_doc + "/raw_listword.txt";

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
			try (BufferedReader br = new BufferedReader(new FileReader(edulistword))) {
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
						idf[x] = Math.log10((double) document_count / (double) (numotherDocumentContain));
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

		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
		}
	}

	private void calcVSM(String doc_type) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			set_typetagger();

			File flistword = new File(f.getAbsolutePath() + "/data/testing/" + doc_type + "/tf-idf_listword.txt");
			fw = new FileWriter(flistword.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			String file = f.getAbsolutePath() + "/data/testing/" + doc_type + "/raw_listword.txt";

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
				// line is not visible here.
			}
			AtomicReference<String> String1;
			AtomicReference<String> String2;
			for (String doc : documents) {
				if (doc != null) {
					word = doc.split(" ");
					tf = new double[word.length];
					idf = new double[word.length];
					weight = new double[word.length];
					for (int x = 0; x < word.length; x++) {
						tf[x] = countNumberEqual(word, word[x]);
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
								String1 = new AtomicReference<String>(word[j - 1]);
								String2 = new AtomicReference<String>(word[j]);
								String1.set(String2.getAndSet(String1.get()));
								word[j - 1] = String1.get();
								word[j] = String2.get();
							}
					int max = 20;
					for (int k = 0; k < max; k++) {
						bw.write(word[k] + " ");
					}
					bw.write("\n");
				}
			}
			bw.close();
		} catch (

		IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
		}
	}

	private void creatematrix_knn() throws FileNotFoundException, IOException {

		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fknn_matrix = new File(f.getAbsolutePath() + "/data/testing/test_knn_matrix.txt");
		File fknn_label = new File(f.getAbsolutePath() + "/data/testing/test_knn_label.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(fknn_matrix.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		FileWriter fwl;
		BufferedWriter bwl;
		fwl = new FileWriter(fknn_label.getAbsoluteFile());
		bwl = new BufferedWriter(fwl);
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

		int knn_matrix[][] = new int[test_document_count][];
		int knn_label[] = new int[test_document_count];
		all_keyword_size = list_allkeyword.size();
		for (int i = 0; i < test_document_count; i++) {
			knn_matrix[i] = new int[all_keyword_size];
		}
		for (int i = 0; i < test_document_count; i++)
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
			int k = test_document_count / 3;
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
				k++;

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			int k = 2 * test_document_count / 3;
			while ((line = br.readLine()) != null) {
				// process the line.
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
		for (int i = 0; i < test_document_count; i++) {
			for (int j = 0; j < all_keyword_size; j++) {
				bw.write(Integer.toString(knn_matrix[i][j]) + " ");
				if (j == all_keyword_size - 1)
					bw.write("\n");
			}
		}
		for (int i = 0; i < test_document_count; i++)
			bwl.write(Integer.toString(knn_label[i]) + "\n");
		bw.close();
		bwl.close();
	}

	private void parseData() throws FileNotFoundException, IOException {
		File fmatrixData = new File(f.getAbsolutePath() + "/data/trainning/knn_matrix.txt");
		File flabelData = new File(f.getAbsolutePath() + "/data/trainning/knn_label.txt");
		File fcorrectlabel = new File(f.getAbsolutePath() + "/data/testing/test_knn_label.txt");
		int lineCount = 0;
		instances = new double[document_count][];
		for (int i = 0; i < document_count; i++) {
			instances[i] = new double[all_keyword_size];
		}
		for (int i = 0; i < 90; i++)
			for (int j = 0; j < all_keyword_size; j++) {
				instances[i][j] = 0;
			}
		label = new String[document_count];
		correct_label = new String[test_document_count];
		try (BufferedReader br = new BufferedReader(new FileReader(fmatrixData.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int i = 0; i < all_keyword_size; i++)
					instances[lineCount][i] = Double.parseDouble(listword[i]);
				lineCount++;
			}
		}
		lineCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(flabelData.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				// String[] listword = line.split(" ");
				label[lineCount] = line;
				lineCount++;
			}
		}
		lineCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(fcorrectlabel.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] slabel = line.split(" ");
				correct_label[lineCount] = slabel[0];
				lineCount++;

			}
		}
	}

	private static String findMajorityClass(String[] array) {
		// add the String array to a HashSet to get unique String values
		Set<String> h = new HashSet<String>(Arrays.asList(array));
		// convert the HashSet back to array
		String[] uniqueValues = h.toArray(new String[0]);
		// counts for unique strings
		int[] counts = new int[uniqueValues.length];
		// loop thru unique strings and count how many times they appear in
		// origianl array
		for (int i = 0; i < uniqueValues.length; i++) {
			for (int j = 0; j < array.length; j++) {
				if (array[j].equals(uniqueValues[i])) {
					counts[i]++;
				}
			}
		}

		for (int i = 0; i < uniqueValues.length; i++)
			System.out.println(uniqueValues[i]);
		for (int i = 0; i < counts.length; i++)
			System.out.println(counts[i]);

		int max = counts[0];
		for (int counter = 1; counter < counts.length; counter++) {
			if (counts[counter] > max) {
				max = counts[counter];
			}
		}
		System.out.println("max # of occurences: " + max);

		// how many times max appears
		// we know that max will appear at least once in counts
		// so the value of freq will be 1 at minimum after this loop
		int freq = 0;
		for (int counter = 0; counter < counts.length; counter++) {
			if (counts[counter] == max) {
				freq++;
			}
		}

		// index of most freq value if we have only one mode
		int index = -1;
		if (freq == 1) {
			for (int counter = 0; counter < counts.length; counter++) {
				if (counts[counter] == max) {
					index = counter;
					break;
				}
			}
			// System.out.println("one majority class, index is: "+index);
			return uniqueValues[index];
		} else {// we have multiple modes
			int[] ix = new int[freq];// array of indices of modes
			System.out.println("multiple majority classes: " + freq + " classes");
			int ixi = 0;
			for (int counter = 0; counter < counts.length; counter++) {
				if (counts[counter] == max) {
					ix[ixi] = counter;// save index of each max count value
					ixi++; // increase index of ix array
				}
			}

			for (int counter = 0; counter < ix.length; counter++)
				System.out.println("class index: " + ix[counter]);

			// now choose one at random
			Random generator = new Random();
			// get random number 0 <= rIndex < size of ix
			int rIndex = generator.nextInt(ix.length);
			System.out.println("random index: " + rIndex);
			int nIndex = ix[rIndex];
			// return unique value at that index
			return uniqueValues[nIndex];
		}

	}

	private void convert_to_vector(List<String> list_doc, String doc_type) throws IOException {
		extractWord(list_doc, doc_type);
		calcVSM(doc_type);
	}

	private void knn_predict() throws FileNotFoundException, IOException {
		int k = 10; // number of negh
		// spellcheck();
		creatematrix_knn();
		parseData();
		int number_of_correct = 0;
		File fresult = new File(f.getAbsolutePath() + "/data/testing/result/knn_result.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(fresult.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		List<Document> documentList = new ArrayList<Document>();
		// list to save distance result
		List<Result> resultList = new ArrayList<Result>();
		// add city data to cityList
		for (int i = 0; i < document_count; i++)
			documentList.add(new Document(instances[i], label[i]));
		int lineCount = 0;
		File ftestmatrix = new File(f.getAbsolutePath() + "/data/testing/test_knn_matrix.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(ftestmatrix.getAbsoluteFile()))) {
			String line;
			double[][] query = new double[test_document_count][];
			for (int i = 0; i < test_document_count; i++) {
				query[i] = new double[all_keyword_size];
			}
			// Vi tri dong
			while ((line = br.readLine()) != null) {

				// process the line.
				String[] listword = line.split(" ");
				for (int i = 0; i < all_keyword_size; i++)
					query[lineCount][i] = Double.parseDouble(listword[i]);
				lineCount++;
				// Process
				for (Document document : documentList) {
					double dist = 0.0;
					for (int j = 0; j < document.documentAttributes.length; j++) {
						dist += Math.pow(document.documentAttributes[j] - query[lineCount - 1][j], 2);
						// System.out.print(city.cityAttributes[j]+" ");
					}
					double distance = Math.sqrt(dist);
					resultList.add(new Result(distance, document.documentType));
					// System.out.println(distance);
				}

				// System.out.println(resultList);
				Collections.sort(resultList, new DistanceComparator());
				String[] ss = new String[k];
				for (int x = 0; x < k; x++) {
					System.out.println(resultList.get(x).documentType + " .... " + resultList.get(x).distance);
					// get classes of k nearest instances (city names) from the
					// list
					// into an array
					ss[x] = resultList.get(x).documentType;
				}
				String majClass = findMajorityClass(ss);
				System.out.println("Class of new instance is: " + majClass);
				if (lineCount == 1)
					bw.write("TECHNOLOGY RESULT\n");
				else if (lineCount == test_document_count / 3 + 1)
					bw.write("EDUCATION RESULT\n");
				else if (lineCount == 2 * test_document_count / 3 + 1)
					bw.write("FASHTION RESULT\n");
				bw.write("Document " + lineCount + ": Predict: " + majClass + " Correct: "
						+ correct_label[lineCount - 1] + "\n");
				if (majClass.equalsIgnoreCase(correct_label[lineCount - 1])) {
					number_of_correct++;
				}
				if (lineCount == test_document_count / 3 || lineCount == 2 * test_document_count / 3
						|| lineCount == test_document_count) {
					bw.write("Final Result: " + number_of_correct + "/" + test_document_count / 3 + " - Accuracy: "
							+ number_of_correct * 100 / (test_document_count / 3) + "%\n");
					number_of_correct = 0;
				}

				// Process
			}
		}
		bw.close();
	}

	static class Document {
		double[] documentAttributes;
		String documentType;

		public Document(double[] documentAttributes, String documentType) {
			this.documentType = documentType;
			this.documentAttributes = documentAttributes;
		}
	}

	// simple class to model results (distance + class)
	static class Result {
		double distance;
		String documentType;

		public Result(double distance, String documentType) {
			this.documentType = documentType;
			this.distance = distance;
		}
	}

	// simple comparator class used to compare results via distances
	static class DistanceComparator implements Comparator<Result> {
		@Override
		public int compare(Result a, Result b) {
			return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
		}
	}

	private String double2label(double pred) {
		if (pred == 0.0)
			return "technology";
		else if (pred == 1.0)
			return "education";
		else
			return "healthy";
	}

	private String string2label(String label) {
		if (label.equals("1"))
			return "technology";
		else if (label.equals("2"))
			return "education";
		else
			return "healthy";
	}

	// KNN area
	private Instance makeInstance(String text, Instances data, List<String> all_keyword) {
		// Declare a nominal attribute along with its values
		FastVector fvNominalVal = new FastVector(3);
		fvNominalVal.addElement("technology");
		fvNominalVal.addElement("education");
		fvNominalVal.addElement("healthy");
		Attribute classatt = new Attribute("class", fvNominalVal);

		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		for (int att = 0; att < all_keyword.size(); att++) {
			atts.add(new Attribute(all_keyword.get(att)));
		}

		// Declare the feature vector
		FastVector fvWekaAttributes = new FastVector(all_keyword.size() + 1);
		fvWekaAttributes.addElement(classatt);
		for (int i = 0; i < all_keyword.size(); i++)
			fvWekaAttributes.addElement(atts.get(i));
		data.setClassIndex(0);
		// Create instance of length two.
		DenseInstance instance = new DenseInstance(all_keyword.size() + 1);

		// Set value for message attribute
		String[] listword = text.split(" ");
		for (int i = 0; i < listword.length; i++) {
			if (all_keyword.indexOf(listword[i]) > 0) {
				int index = all_keyword.indexOf(listword[i]);
				instance.setValue(index + 1, 1.0);
			}
		}
		for (int i = 1; i <= all_keyword.size(); i++)
			if (instance.value(i) != 1.0) // neu vi tri nay chua co gia
											// tri
				instance.setValue(i, 0);
		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}

	private void predict_bayes() throws IOException {
		try {
			parseData();
			File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
			File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
			File fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
			File fresult = new File(f.getAbsolutePath() + "/data/testing/result/bayes_result.txt");
			File trainningkey = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
			List<String> all_keyword = new ArrayList<String>();
			List<String> query_word = new ArrayList<String>();
			FileWriter fw;
			BufferedWriter bw;
			fw = new FileWriter(fresult.getAbsoluteFile());
			bw = new BufferedWriter(fw);
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
			Reader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(f.getAbsolutePath() + "/data/trainning/data.arff"), "UTF-8"));
			ArffReader arff = new ArffReader(r);

			Instances samples = arff.getData();

			samples.setClassIndex(0);
			Classifier classifier = new NaiveBayesMultinomialUpdateable();
			classifier.buildClassifier(samples);
			r.close();

			int lineCount = 0;
			int number_of_correct = 0;
			// --------------------------------------
			try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
				String line;
				while ((line = br.readLine()) != null) {
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
				String line;
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
				String line;
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			Instances testset = samples.stringFreeStructure();
			for (int i = 0; i < test_document_count; i++) {
				// DenseInstance instance = new DenseInstance(2);
				// instance.setValue(attribute2, query_word.get(i));
				Instance instance = makeInstance(query_word.get(i), testset, all_keyword);

				double majClass = classifier.classifyInstance(instance);
				// System.out.println(filteredInstance.stringValue(1));
				String predict = double2label(majClass);
				System.out.println("Class of new instance is: " + predict);
				lineCount = i + 1;
				if (lineCount == 1)
					bw.write("======TECHNOLOGY RESULT======\n");
				else if (lineCount == test_document_count / 3 + 1)
					bw.write("======EDUCATION RESULT======\n");
				else if (lineCount == 2 * test_document_count / 3 + 1)
					bw.write("======FASHTION RESULT======\n");
				String correctlabel = "";
				correctlabel = string2label(correct_label[lineCount - 1]);
				bw.write("Document " + lineCount + ": Predict: " + predict + "|| Correct: " + correctlabel + "\n");
				if (predict.equalsIgnoreCase(correctlabel)) {
					number_of_correct++;
				}
				if (lineCount == test_document_count / 3 || lineCount == 2 * test_document_count / 3
						|| lineCount == test_document_count) {
					bw.write("Final Result: " + number_of_correct + "/" + test_document_count / 3 + " - Accuracy: "
							+ number_of_correct * 100 / (test_document_count / 3) + "%\n");
					number_of_correct = 0;
				}
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void predict_knn() throws IOException {
		try {
			parseData();
			File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
			File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
			File fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
			File fresult = new File(f.getAbsolutePath() + "/data/testing/result/knn_result_weka.txt");
			List<String> query_word = new ArrayList<String>();
			File trainningkey = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
			List<String> all_keyword = new ArrayList<String>();
			FileWriter fw;
			BufferedWriter bw;
			fw = new FileWriter(fresult.getAbsoluteFile());
			bw = new BufferedWriter(fw);
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

			Reader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(f.getAbsolutePath() + "/data/trainning/data.arff"), "UTF-8"));
			ArffReader arff = new ArffReader(r);

			Instances samples = arff.getData();

			samples.setClassIndex(0);
			IBk classifier = new IBk();
			classifier.setKNN(3);
			classifier.buildClassifier(samples);

			r.close();
			int lineCount = 0;
			int number_of_correct = 0;
			// --------------------------------------
			try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}

			Instances testset = samples.stringFreeStructure();
			for (int i = 0; i < test_document_count; i++) {
				// DenseInstance instance = new DenseInstance(2);
				// instance.setValue(attribute2, query_word.get(i));
				Instance instance = makeInstance(query_word.get(i), testset, all_keyword);

				double majClass = classifier.classifyInstance(instance);
				// System.out.println(newinstances.instance(i).stringValue(1));
				String predict = double2label(majClass);
				System.out.println("Class of new instance is: " + predict);
				lineCount = i + 1;
				if (lineCount == 1)
					bw.write("======TECHNOLOGY RESULT======\n");
				else if (lineCount == test_document_count / 3 + 1)
					bw.write("======EDUCATION RESULT======\n");
				else if (lineCount == 2 * test_document_count / 3 + 1)
					bw.write("======FASHTION RESULT======\n");
				String correctlabel = "";
				correctlabel = string2label(correct_label[lineCount - 1]);
				bw.write("Document " + lineCount + ": Predict: " + predict + " Correct: " + correctlabel + "\n");
				if (predict.equalsIgnoreCase(correctlabel)) {
					number_of_correct++;
				}
				if (lineCount == test_document_count / 3 || lineCount == 2 * test_document_count / 3
						|| lineCount == test_document_count) {
					bw.write("Final Result: " + number_of_correct + "/" + test_document_count / 3 + " - Accuracy: "
							+ number_of_correct * 100 / (test_document_count / 3) + "%\n");
					number_of_correct = 0;
				}
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void predict_svm() throws IOException {
		try {
			parseData();
			File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
			File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
			File fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
			File fresult = new File(f.getAbsolutePath() + "/data/testing/result/svm_result_weka.txt");
			List<String> query_word = new ArrayList<String>();
			File trainningkey = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
			List<String> all_keyword = new ArrayList<String>();
			FileWriter fw;
			BufferedWriter bw;
			fw = new FileWriter(fresult.getAbsoluteFile());
			bw = new BufferedWriter(fw);
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
			Reader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(f.getAbsolutePath() + "/data/trainning/data.arff"), "UTF-8"));
			ArffReader arff = new ArffReader(r);

			Instances samples = arff.getData();

			samples.setClassIndex(0);
			SMO classifier = new SMO();
			classifier.buildClassifier(samples);
			classifier.setC(2);
			classifier.setEpsilon(0.1);
			RBFKernel kernel = (RBFKernel) Kernel.forName("weka.classifiers.functions.supportVector.RBFKernel", null);
			kernel.setGamma(2);
			classifier.setKernel(kernel);
			r.close();
			int lineCount = 0;
			int number_of_correct = 0;
			// --------------------------------------
			try (BufferedReader br = new BufferedReader(new FileReader(tech_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(edu_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}
			try (BufferedReader br = new BufferedReader(new FileReader(fash_listword.getAbsoluteFile()))) {
				String line;
				// Vi tri dong
				while ((line = br.readLine()) != null) {
					// process the line.
					query_word.add(line);
				}
			}

			Instances testset = samples.stringFreeStructure();
			for (int i = 0; i < test_document_count; i++) {
				// DenseInstance instance = new DenseInstance(2);
				// instance.setValue(attribute2, query_word.get(i));
				// instance.setDataset(newinstances);
				// newinstances.add(instance);
				Instance instance = makeInstance(query_word.get(i), testset, all_keyword);

				double majClass = classifier.classifyInstance(instance);
				String predict = double2label(majClass);
				System.out.println("Class of new instance is: " + predict);
				lineCount = i + 1;
				if (lineCount == 1)
					bw.write("=======TECHNOLOGY RESULT======\n");
				else if (lineCount == test_document_count / 3 + 1)
					bw.write("======EDUCATION RESULT======\n");
				else if (lineCount == 2 * test_document_count / 3 + 1)
					bw.write("======FASHTION RESULT======\n");
				String correctlabel = "";
				correctlabel = string2label(correct_label[lineCount - 1]);
				bw.write("Document " + lineCount + ": Predict: " + predict + " Correct: " + correctlabel + "\n");
				if (predict.equalsIgnoreCase(correctlabel)) {
					number_of_correct++;
				}
				if (lineCount == test_document_count / 3 || lineCount == 2 * test_document_count / 3
						|| lineCount == test_document_count) {
					bw.write("Final Result: " + number_of_correct + "/" + test_document_count / 3 + " - Accuracy: "
							+ number_of_correct * 100 / (test_document_count / 3) + "%\n");
					number_of_correct = 0;
				}
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void creatematrix_svm() throws IOException {
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fsvm_matrix = new File(f.getAbsolutePath() + "/data/testing/test_svm_matrix.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(fsvm_matrix.getAbsoluteFile());
		bw = new BufferedWriter(fw);

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
				bw.write("1 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw.write(index + ":1 ");
					}
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
				bw.write("2 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw.write(index + ":1 ");
					}
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
				bw.write("3 ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						bw.write(index + ":1 ");
					}
				}
				bw.write("\n");
			}
		}
		bw.close();

	}

	private void svm_trainning() throws IOException {
		// Check if model has created
		creatematrix_svm();
		// If not create yet
		String[] argv = new String[3];
		argv[0] = "/data/trainning/svm_matrix.txt";
		argv[1] = "/data/testing/svm.model";
		predictdata.svm_train.svm_learn(argv);

	}

	private void svm_predict() throws IOException {
		String[] argv = new String[3];
		argv[0] = "/data/testing/test_svm_matrix.txt";
		argv[1] = "/data/testing/svm.model";
		argv[2] = "/data/testing/result/svm_result.txt";
		predictdata.svm_predict.svm_classify(argv);
	}

	private void lda_predict() throws Exception {
		// Read all listword from trainning set and save it to variable
		// Then add listword of testing set to the same variable
		// Save it to new file and read it to precess LDA
		// Add function check label of predict with correct label and print
		// result
		List<String> ldalist = new ArrayList<String>();
		File train_tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/tf-idf_listword.txt");
		File train_edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/tf-idf_listword.txt");
		File train_fash_listword = new File(f.getAbsolutePath() + "/data/trainning/healthy/tf-idf_listword.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(train_tech_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(train_edu_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);
			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(train_fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);
			}
		}
		File test_tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/tf-idf_listword.txt");
		File test_edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/tf-idf_listword.txt");
		File test_fash_listword = new File(f.getAbsolutePath() + "/data/testing/healthy/tf-idf_listword.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(test_tech_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);

			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(test_edu_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);
			}
		}
		try (BufferedReader br = new BufferedReader(new FileReader(test_fash_listword.getAbsoluteFile()))) {
			String line;
			// Vi tri dong
			while ((line = br.readLine()) != null) {
				// process the line.
				ldalist.add(line);
			}
		}
		File ldalistword = new File(f.getAbsolutePath() + "/data/testing/lda_result/lda_list.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(ldalistword.getAbsoluteFile());
		bw = new BufferedWriter(fw);
		for (int i = 0; i < ldalist.size(); i++) {
			bw.write(ldalist.get(i));
			bw.write("\n");
		}
		bw.close();
		double alpha = 0.1653D;
		double beta = 0.05435D;
		int numTopics = 3;
		int numIterations = 200;
		int topWords = 100;
		int savestep = 0;
		String expName = "model";
		String corpusPath = f.getAbsolutePath() + "/data/testing/lda_result/lda_list.txt";
		String tAssignsFilePath = "";
		models.GibbsSamplingLDA lda = new GibbsSamplingLDA(corpusPath, numTopics, alpha, beta, numIterations, topWords,
				expName, tAssignsFilePath, savestep);
		lda.inference();
	}

	private void lda_result() throws FileNotFoundException, IOException {
		// BufferReader from line of testing set in file topAssigment
		// Read label and compare with correct label
		// Write the result
		File ldaresult = new File(f.getAbsolutePath() + "/data/testing/result/lda_result.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(ldaresult.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		File topassign = new File(f.getAbsolutePath() + "/data/testing/lda_result/model.topicAssignments");
		int lineCount = 0;
		int number_of_correct = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(topassign.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				if (lineCount < document_count)
					lineCount++;
				else {
					String[] listword = line.split(" ");
					int type1 = 0;
					int type2 = 0;
					int type3 = 0;
					for (int x = 0; x < listword.length; x++) {
						// Save into array
						if (listword[x].contains("1"))
							type1++;
						else if (listword[x].contains("2"))
							type2++;
						else if (listword[x].contains("3"))
							type3++;
					}

					// Check label of this line
					// Compare with label[lineCount] and write to file like knn
					String label = ""; // label = label of line
					if (type1 > type2 && type1 > type3)
						label = "1";
					else if (type2 > type1 && type2 > type3)
						label = "2";
					else if (type3 > type1 && type3 > type2)
						label = "3";

					lineCount++;
					if (lineCount - document_count == 1)
						bw.write("TECHNOLOGY RESULT\n");
					else if (lineCount - document_count == test_document_count / 3 + 1)
						bw.write("EDUCATION RESULT\n");
					else if (lineCount - document_count == 2 * test_document_count / 3 + 1)
						bw.write("FASHTION RESULT\n");
					bw.write("Document " + (lineCount - document_count) + ": Predict: " + label + " Correct: "
							+ correct_label[lineCount - document_count - 1] + "\n");
					if (label.equalsIgnoreCase(correct_label[lineCount - document_count - 1])) {
						number_of_correct++;
					}
					if (lineCount - document_count == test_document_count / 3
							|| lineCount - document_count == 2 * test_document_count / 3
							|| lineCount - document_count == test_document_count) {
						bw.write("Final Result: " + number_of_correct + "/" + (test_document_count / 3)
								+ " - Accuracy: " + number_of_correct * 100 / (test_document_count / 3) + "%\n");
						number_of_correct = 0;
					}
				}
			}
		}
		bw.close();
	}

	private void prepareData() throws FileNotFoundException, IOException {
		readFile();
		extractWord(tech_doc, "technology");
		extractWord(edu_doc, "education");
		extractWord(heal_doc, "healthy");
		calcVSM();
	}

	public void FpredictData(nlplib lib) throws Exception {
		this.lib = lib;
		f = new File(".");
		stopword();
		prepareData();
		// knn_predict();
		// svm_trainning();
		// svm_predict();
		predict_bayes();
		predict_svm();
		predict_knn();
		// lda_predict();
		// lda_result();
	}
}
