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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jvntagger.MaxentTagger;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class CreateData {
	public static final String TECH_DOC_PATH = "/data/documents/technology/";
	public static final String EDU_DOC_PATH = "/data/documents/education/";
	public static final String FASH_DOC_PATH = "/data/documents/fashion/";

	private List<String> tech_doc = new ArrayList<String>();
	private List<String> edu_doc = new ArrayList<String>();
	private List<String> fash_doc = new ArrayList<String>();
	public File f;
	public spellchecker sc;
	SentenceDetector detector;
	VietTokenizer tokenizer;
	MaxentTagger tagger;

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

	int all_keyword_size;

	int[][] knn_matrix;
	int[][] svm_matrix;

	int[] knn_label;

	CreateData() throws IOException {
		f = new File(".");
		detector = new SentenceDetector(
				f.getAbsolutePath() + "/data/tools/NLPTools/models/sentDetection/VietnameseSD.bin.gz");
		tokenizer = new VietTokenizer(f.getAbsolutePath() + "/data/tools/NLPTools/tokenizer.properties");
		tagger = new MaxentTagger(f.getAbsolutePath() + "/data/tools/NLPTools/model/maxent");
		sc = new spellchecker();
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
		files = new File(f.getAbsolutePath() + FASH_DOC_PATH).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				fash_doc.add(file.getAbsolutePath());
			}
		}
	}

	public void spellCheckandRepair() {
		// Spelling Check Technology Document
		sc.doCheck(tech_doc);
		sc.doCheck(edu_doc);
		sc.doCheck(fash_doc);
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
		type_tagger[0] = "\\Np";
		type_tagger[1] = "\\Nc";
		type_tagger[2] = "\\Nu";
		type_tagger[3] = "\\N";
		type_tagger[4] = "\\V";
	}

	public void extractWord(List<String> list_doc, String doc_type) {
		try {
			FileWriter fw;
			BufferedWriter bw;
			set_typetagger();
			temp_wordoffile.clear();
			wordoffile.clear();
			File temp = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/listword.txt");
			fw = new FileWriter(temp.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (Iterator<String> i = list_doc.iterator(); i.hasNext();) {
				// Get file dir
				String file = i.next();
				// Read and check the input from the text file
				BufferedReader inputFile = new BufferedReader(new FileReader(file));
				System.out.println("Reading from " + file);
				// Read all text and split sentence then save to array
				InputStream is = null;
				is = new FileInputStream(file);
				sentence = sentSlipt(readStream(is));
				for (int j = 0; j < sentence.length; j++) {
					wordsplit[j] = wordslipt(sentence[j]);
					wordtagger[j] = tagger(wordsplit[j]);
				}
				// Process every wordtagger
				for (String wtag : wordtagger) {
					// Store every word splited to array
					word = wtag.split(" ");

					for (int x = 0; x < word.length; x++) {
						for (String typtag : type_tagger) {
							// Type filter
							if (word[x].indexOf(typtag) > 0) {
								word[x].replace(typtag, "");
								break;
							}
						}
						// If chua co trong csdl, them vao , nguoc lai bo qua
						if (!temp_wordoffile.contains(word[x])) {
							// Them vao csdl
							temp_wordoffile.add(word[x]);
						}

					}

				}

				// Tinh tf-idf va luu lai 10 tu quan trong vao file o 1 dong
				// moi
				Collections.sort(wordoffile);
				for (int k = 0; k < 10; k++) {
					bw.write(wordoffile.get(k) + " ");
				}
				bw.write("\n");
				bw.close();
			}
		} catch (IOException e) {
			System.out.println("IOException Occured! ");
			e.printStackTrace();
			// System.exit(-1);
		}
	}

	/**
	 * @param args
	 *            get keyword of document type
	 * @throws IOException
	 */
	public void getkeyword(String file_path, String doc_type) throws IOException {
		// Lưu cùng vị trí với file
		List<String> doc_keyword = new ArrayList<String>();
		FileWriter fw;
		BufferedWriter bw;
		File listofword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/listword.txt");
		File keyword = new File(f.getAbsolutePath() + "/data/trainning/" + doc_type + "/keyword.txt");
		fw = new FileWriter(keyword.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		try (BufferedReader br = new BufferedReader(new FileReader(listofword.getAbsoluteFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					if (!doc_keyword.contains(word[x])) {
						doc_keyword.add(word[x]);
					}
				}

			}
		}
		for (int k = 0; k < doc_keyword.size(); k++) {
			bw.write(doc_keyword.get(k) + " ");
		}
		bw.close();

	}

	public void maketrainningkey() throws FileNotFoundException, IOException {
		// Đọc 3 file mà tạo ra 1 file keyword chung
		File tech_key = new File(f.getAbsolutePath() + "/data/trainning/tecnology/keyword.txt");
		File edu_key = new File(f.getAbsolutePath() + "/data/trainning/education/keyword.txt");
		File fash_key = new File(f.getAbsolutePath() + "/data/trainning/fashion/keyword.txt");
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
		for (int k = 0; k < all_keyword.size(); k++) {
			bw.write(all_keyword.get(k) + " ");
		}
		all_keyword_size = all_keyword.size();
		bw.close();

	}

	public void creatematrix_knn() throws FileNotFoundException, IOException {
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
		File tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/trainning/fashion/listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fknn_matrix = new File(f.getAbsolutePath() + "/data/trainning/knn_matrix.txt");
		FileWriter fw;
		BufferedWriter bw;
		fw = new FileWriter(fknn_matrix.getAbsoluteFile());
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

		knn_matrix = new int[300][all_keyword_size];
		knn_label = new int[300];

		for (int i = 0; i < 300; i++)
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
					if (index > 0) {
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
			int k = 100;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
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
			int k = 200;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] listword = line.split(" ");
				for (int x = 0; x < listword.length; x++) {
					int index = list_allkeyword.indexOf(listword[x]);
					if (index > 0) {
						knn_matrix[k][index] = 1;
					}

				}
				knn_label[k] = 3;
				k++;
			}
		}
		for (int i = 0; i < 300; i++) {
			for (int j = 0; j < all_keyword_size; j++) {
				bw.write(Integer.toString(knn_matrix[i][j]));
				if (j == all_keyword_size - 1)
					bw.write("\n");
			}
		}
		bw.close();
	}

	public void creatematrix_svm() throws IOException {
		// Đọc lần lược 3 file list word
		// Mỗi lần đọc một dòng thì so sánh với totaltrainningkey và tạo ra
		// vector nhị phân
		// Đồng thời, ghi lại label của vector đó ở 1 file khác
		// và ghi ra 1 file vừa label vừa vector cho svm
		//
		List<String> list_allkeyword = new ArrayList<String>();
		File tech_listword = new File(f.getAbsolutePath() + "/data/trainning/technology/listword.txt");
		File edu_listword = new File(f.getAbsolutePath() + "/data/trainning/education/listword.txt");
		File fash_listword = new File(f.getAbsolutePath() + "/data/trainning/fashion/listword.txt");
		File all_key = new File(f.getAbsolutePath() + "/data/trainning/allkeyword.txt");
		File fsvm_matrix = new File(f.getAbsolutePath() + "/data/trainning/svm_matrix.txt");
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
			int k = 0;
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
			int k = 100;
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
			int k = 200;
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

	public void createTrainingData() {
		extractWord(tech_doc, "tecnology");
		extractWord(edu_doc, "education");
		extractWord(fash_doc, "fashion");
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		CreateData credat = new CreateData();
		credat.readFile();
		credat.spellCheckandRepair();
		// TODO Auto-generated method stub
		// String vb = "Camera của Galaxy S7 là một trong vài camera trên điện
		// thoại tốt nhất hiện nay. Công bằng mà nói thì SamSung cải tiến camera
		// trên điện thoại rõ nhất bắt đầu từ chiếc Note 5, và có thay đổi rất
		// nhiều về tư duy màu sắc từ chiếc Galaxy S6. Đó là bộ xử lý hình
		// sau.";
		//
		// String cau[] = credat.sentSlipt(vb);
		// String[] tachtu = new String[cau.length];
		//
		// for (int i = 0; i < cau.length; i++) {
		// tachtu[i] = credat.wordslipt(cau[i]);
		// }
		//
		// for (int i = 0; i < cau.length; i++) {
		// System.out.println(cau[i]);
		// System.out.println(tachtu[i]);
		// }
		// System.out.println(tachtu[1]);
	}

}
