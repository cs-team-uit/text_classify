import java.io.File;

import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class CreateData {
	public File f;

	CreateData() {
		f = new File(".");
	}

	public String[] tachcau(String vb) {
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

	public String tachtu(String cau) {
		String ret = null;
		VietTokenizer tokenizer = new VietTokenizer(f.getAbsolutePath() + "/data/tools/NLPTools/tokenizer.properties");
		ret = tokenizer.segment(cau);
		return ret;
	}

	public static void main(String[] args) {
		CreateData credat = new CreateData();
		// TODO Auto-generated method stub
		String vb = "Camera của Galaxy S7 là một trong vài camera trên điện thoại tốt nhất hiện nay. Công bằng mà nói thì SamSung cải tiến camera trên điện thoại rõ nhất bắt đầu từ chiếc Note 5, và có thay đổi rất nhiều về tư duy màu sắc từ chiếc Galaxy S6. Đó là bộ xử lý hình sau khi ghi nhận file ảnh thô (RAW) xử lý màu, khử nhiễu hạt, tương phản… không rực rỡ nịnh mắt kiểu giả tạo như trước, bớt khử nhiễu láng mịn quá mức. Đến S7 là cả một sự cải tiến rất mạnh về cả phần cứng vật lý, trong đó phải nói đến tấm cảm biến hình ảnh. Thay đổi gốc của camera là phần cứng vật lý. Họ làm cảm biến to hơn một chút, điểm ảnh to hơn một chút, không còn chạy theo 'số chấm' đánh vào tâm lý người dùng. Trên tấm cảm biến đó, họ cố gắng ứng dụng công nghệ dual-pixel để tăng cường khả năng lấy nét tốt và nhanh hơn. Giao diện công cụ chụp ảnh có một ít thay đổi tích cực sẽ nói bên dưới, làm cho người thích chụp, khai thác khả năng của camera điện thoại, thích thú và tạo cảm hứng nhiều hơn.";

		// String vb = "An đang xem thông báo.";
		String cau[] = credat.tachcau(vb);

		String[] tachtu = new String[cau.length];

		for (int i = 0; i < cau.length; i++) {
			tachtu[i] = credat.tachtu(cau[i]);
		}

		for (int i = 0; i < cau.length; i++) {
			System.out.println(cau[i]);
			System.out.println(tachtu[i]);
		}
	}

}
