package makedata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class CreateData {
	public static final String TECH_DOC_PATH = "/data/documents/technology/";
	public static final String EDU_DOC_PATH = "/data/documents/education/";
	public static final String ECONOMIC_DOC_PATH = "/data/documents/economic/";

	private List<String> tech_doc = new ArrayList<String>();
	private List<String> edu_doc = new ArrayList<String>();
	private List<String> econ_doc = new ArrayList<String>();
	public File f;
	public spellchecker sc;

	CreateData() {
		f = new File(".");
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
		files = new File(f.getAbsolutePath() + ECONOMIC_DOC_PATH).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				econ_doc.add(file.getAbsolutePath());
			}
		}
	}

	public void spellCheckandRepair() {
		// Spelling Check Technology Document
		sc.doCheck(tech_doc);
		sc.doCheck(edu_doc);
		sc.doCheck(econ_doc);
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreateData credat = new CreateData();
		credat.readFile();
		credat.spellCheckandRepair();
		// TODO Auto-generated method stub
		String vb = "Camera của Galaxy S7 là một trong vài camera trên điện thoại tốt nhất hiện nay. Công bằng mà nói thì SamSung cải tiến camera trên điện thoại rõ nhất bắt đầu từ chiếc Note 5, và có thay đổi rất nhiều về tư duy màu sắc từ chiếc Galaxy S6. Đó là bộ xử lý hình sau.";

		String cau[] = credat.sentSlipt(vb);
		String[] tachtu = new String[cau.length];

		for (int i = 0; i < cau.length; i++) {
			tachtu[i] = credat.wordslipt(cau[i]);
		}

		for (int i = 0; i < cau.length; i++) {
			System.out.println(cau[i]);
			System.out.println(tachtu[i]);
		}
		System.out.println(tachtu[1]);
	}

}
