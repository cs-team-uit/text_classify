package tools;

import java.io.File;
import java.io.IOException;

import jvntagger.MaxentTagger;
import makedata.spellchecker;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class nlplib {
	public SentenceDetector detector;
	public VietTokenizer tokenizer;
	public MaxentTagger tagger;
	public spellchecker sc;
	private File f;
	public nlplib() throws IOException {
		f = new File(".");
		detector = new SentenceDetector(
				f.getAbsolutePath() + "/data/tools/NLPTools/models/sentDetection/VietnameseSD.bin.gz");
		tokenizer = new VietTokenizer(f.getAbsolutePath() + "/data/tools/NLPTools/tokenizer.properties");
		tagger = new MaxentTagger(f.getAbsolutePath() + "/data/tools/NLPTools/model/maxent");
		sc = new spellchecker();
	}
}
