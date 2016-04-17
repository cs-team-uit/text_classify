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

import jvntagger.MaxentTagger;
import models.GibbsSamplingLDA;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class predictData {
	// KNN area
	private double[][] instances;
	private String[] label;
	private String[] correct_label;
	File f = new File(".");
	private int document_count = 360;
	private int test_document_count = 84;
	public int all_keyword_size;
	public static final String TECH_DOC_PATH = "/data/documents/test_data/technology/";
	public static final String EDU_DOC_PATH = "/data/documents/test_data/education/";
	public static final String FASH_DOC_PATH = "/data/documents/test_data/fashion/";
	private List<String> tech_doc = new ArrayList<String>();
	private List<String> edu_doc = new ArrayList<String>();
	private List<String> fash_doc = new ArrayList<String>();
	SentenceDetector detector;
	VietTokenizer tokenizer;
	MaxentTagger tagger;
	GibbsSamplingLDA LDA;

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

	public predictData() throws IOException {
		f = new File(".");
		detector = new SentenceDetector(
				f.getAbsolutePath() + "/data/tools/NLPTools/models/sentDetection/VietnameseSD.bin.gz");
		tokenizer = new VietTokenizer(f.getAbsolutePath() + "/data/tools/NLPTools/tokenizer.properties");
		tagger = new MaxentTagger(f.getAbsolutePath() + "/data/tools/NLPTools/model/maxent");

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
		files = new File(f.getAbsolutePath() + FASH_DOC_PATH).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				fash_doc.add(file.getAbsolutePath());
			}
		}
	}

	public String[] sentSlipt(String vb) {
		String[] ret = null;
		try {

			ret = detector.sentDetect(vb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String wordslipt(String cau) {
		String ret = null;
		ret = tokenizer.segment(cau);
		return ret;
	}

	public String tagger(String sentence) {
		String ret = null;

		ret = tagger.tagging(sentence);
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
		type_tagger[4] = "/N";
		type_tagger[5] = "/V";
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

	private void extractWord(List<String> list_doc, String doc_type) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			set_typetagger();

			FileWriter fwl;
			BufferedWriter bwl;
			File temp = new File(f.getAbsolutePath() + "/data/testing/" + doc_type + "/temp_listword.txt");
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
									if (listword[x].indexOf(".") + 1 == listword[x].length())
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
				}
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

	private void calcVSM(String doc_type) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			set_typetagger();

			File flistword = new File(f.getAbsolutePath() + "/data/testing/" + doc_type + "/listword.txt");
			fw = new FileWriter(flistword.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			String file = f.getAbsolutePath() + "/data/testing/" + doc_type + "/temp_listword.txt";

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
					for (int k = 0; k < 50; k++) {
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
		// Đọc lần lược 3 file list word
		// Mỗi lần đọc một dòng thì so sánh với totaltrainningkey và tạo ra
		// vector nhị phân
		// Đồng thời, ghi lại label của vector đó ở 1 file khác
		// và ghi ra 1 file vừa label vừa vector cho svm
		//
		// Đưa all_keyword vao list
		// Đọc lần lượt từng file
		// Đọc từng dòng, kiểm tra xem word trong file đó có ở chỗ nào của list
		// Vị trí có = 1 , không có thì bằng 0
		// Ghi label vào 1 file khác
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/testing/fashion/listword.txt");
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
		// Đọc ma trận trong file và lưu vào mảng 2 chiều knn_matrix
		// Đọc label trong file và ghi vào mảng label
		// Khai báo class
		// Đọc các lần lượt từng file trong bộ test và biến đổi về cấu trúc
		// vector
		// Predit từng file và so sánh kết quả với label chuẩn trong thư mục
		// test
		// Lưu lại kết quả trong file result
		int k = 20; // number of negh
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
				else if (lineCount == 29)
					bw.write("EDUCATION RESULT\n");
				else if (lineCount == 85)
					bw.write("FASHTION RESULT\n");
				bw.write("Document " + lineCount + ": Predict: " + majClass + " Correct: "
						+ correct_label[lineCount - 1] + "\n");
				if (majClass.equalsIgnoreCase(correct_label[lineCount - 1])) {
					number_of_correct++;
				}
				if (lineCount == 28 || lineCount == 56 || lineCount == 84) {
					bw.write("Final Result: " + number_of_correct + "/28 - Accuracy: " + number_of_correct * 100 / 28
							+ "%\n");
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

	// KNN area
	private void creatematrix_svm() throws IOException {
		// Đọc lần lược 3 file list word
		// Mỗi lần đọc một dòng thì so sánh với totaltrainningkey và tạo ra
		// vector nhị phân
		// Đồng thời, ghi lại label của vector đó ở 1 file khác
		// và ghi ra 1 file vừa label vừa vector cho svm
		//
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/testing/technology/listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/testing/education/listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/testing/fashion/listword.txt");
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
		// argv[0] = "-t 2 -c 100 -s 1 -d 7 -g 4 -r 2";
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
		double alpha = 0.1D;
		double beta = 0.01D;
		int numTopics = 3;
		int numIterations = 200;
		int topWords = 20;
		int savestep = 0;
		String expName = "model";
		String corpusPath = f.getAbsolutePath() + "/data/testing/lda_list.txt";
		String tAssignsFilePath = "/data/testing/lda_result/";
		models.GibbsSamplingLDA lda = new GibbsSamplingLDA(corpusPath, numTopics, alpha, beta, numIterations, topWords,
				expName, tAssignsFilePath, savestep);
		lda.inference();
	}

	private void lda_result() throws FileNotFoundException, IOException {
		// BufferReader from line of testing set in file topAssigment
		// Read label and compare with correct label
		// Write the result
		File ldaresult = new File(f.getAbsolutePath() + "/data/testing/test_svm_matrix.txt");
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
				if (lineCount < 90)
					lineCount++;
				else {
					String[] listword = line.split(" ");
					for (int x = 0; x < listword.length; x++) {
						// Save into array
					}
					// Check label of this line
					// Compare with label[lineCount] and write to file like knn
					String label = ""; // label = label of line
					lineCount++;
					if (lineCount == 1)
						bw.write("TECHNOLOGY RESULT\n");
					else if (lineCount == 31)
						bw.write("EDUCATION RESULT\n");
					else if (lineCount == 61)
						bw.write("FASHTION RESULT\n");
					bw.write("Document " + lineCount + ": Predict: " + label + " Correct: "
							+ correct_label[lineCount - 1] + "\n");
					if (label.equalsIgnoreCase(correct_label[lineCount - 1])) {
						number_of_correct++;
					}
					if (lineCount == 30 || lineCount == 60 || lineCount == 90) {
						bw.write("Final Result: " + number_of_correct + "/30 - Accuracy: "
								+ number_of_correct * 100 / 30 + "%\n");
						number_of_correct = 0;
					}
				}
			}
		}
		bw.close();
	}

	private void prepareData() throws FileNotFoundException, IOException {
		readFile();
		convert_to_vector(tech_doc, "technology");
		convert_to_vector(edu_doc, "education");
		convert_to_vector(fash_doc, "fashion");
	}

	public void FpredictData() throws FileNotFoundException, IOException {
		predictData pd = new predictData();
		// pd.spellcheck();
		// pd.prepareData();
		pd.knn_predict();
		pd.svm_trainning();
		pd.svm_predict();
	}
}
