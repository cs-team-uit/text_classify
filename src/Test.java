import java.io.File;

import jvntagger.MaxentTagger;
import vn.hus.nlp.sd.SentenceDetector;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class Test {
	File f = new File(".");

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

	public String gannhan(String cau) {
		String ret = null;
		MaxentTagger tagger = new MaxentTagger(f.getAbsolutePath() + "/data/tools/NLPTools/model/maxent");
		ret = tagger.tagging(cau);
		return ret;
	}

	public static void main(String arg[]) {
		Test test = new Test();

		/**/
		// String vb = "lịch sử Việt Nam có 4.000 năm. trong đó 1.500 năm thuộc
		// thời vua Hùng";
		// String vb = "Trống đồng 4.000 năm được tìm thấy. Mặt trống vẫn nguyên
		// vẹn";
		// String vb = "Bọn mình có mượn được LG G5 bản SnapDragon 820, phiên
		// bản nhiều khả năng sẽ không được bán ở Việt Nam để so sánh với Galaxy
		// S7 Exynos chính hãng. Ngoài ra, do còn kết quả cũ từ bản SnapDragon
		// 820 nên chúng ta cũng cho nó vào danh sách để các bạn xem sự khác
		// biệt. Chúng ta có thử nghiệm một số kết quả benchmark toàn hệ thống
		// cũng như tốc độ tải game hay ứng dụng.";

		String vb = "Sau khi dạo một vòng chợ Tân Bình, chị Phan Mai Hoa than thở rằng không biết vì lý do gì mà thời gian gần đây dù cùng một khu vực, thậm chí cùng một chợ, nhưng giá cả nhiều loại rau xanh khác nhau và đều tăng. “Tui mua ba nhánh lá tía tô với vài cọng hành lá, họ kêu 5.000 đồng rồi” - chị Hoa nói. Theo giải thích của tiểu thương, lá tía tô mấy hôm nay tăng giá dữ lắm, trời nắng... là cây này không có hàng. “Chị lấy chợ đầu mối đã 50.000 - 60.000 đồng/kg rồi. Còn hành lá thì bữa giờ vẫn 40.000 đồng/kg đó, ai bán mắc bao giờ đâu nè” - chủ một sạp giải thích khi được hỏi tại sao các loại rau lá lại mắc đến vậy. Tại chợ Hoàng Hoa Thám (Q.Tân Bình), chỉ riêng mặt hàng cải xanh đã có 2-3 loại giá khác nhau, dao động từ 18.000 - 30.000 đồng/kg. “Rau mấy hôm nay mắc lắm, chị lấy sao bán vậy chứ ai bán mắc cho em làm gì” - tiểu thương tên Thủy giải thích khi người mua phải trả 10.000 đồng cho 400g rau cải.Cầm bó rau cải 7.000 đồng, chị Thanh Trúc (ngụ Q.Bình Thạnh) cho biết giá này vẫn vậy nhưng lượng rau ít hơn tuần trước. Tiểu thương nói hàng về ổn định, không tăng giá nhưng những người thường xuyên đi chợ dễ dàng nhận ra giá chừng đó mà lượng cứ ít đi.  Đặc biệt, theo chị Trúc, giá lươn đồng, tôm sông tăng kinh khủng mà không dễ mua. “Ngày nào ra trễ chút xíu là không còn hàng. Tôm sông thì gần như không mua được” - chị Trúc than.";
		String cau[] = test.tachcau(vb);

		String[] tachtu = new String[cau.length];

		String[] gannhan = new String[cau.length];

		for (int i = 0; i < cau.length; i++) {
			tachtu[i] = test.tachtu(cau[i]);
			gannhan[i] = test.gannhan(tachtu[i]);

		}

		for (int i = 0; i < cau.length; i++) {
			// System.out.println(cau[i]);
			// System.out.println(tachtu[i]);
			System.out.println(gannhan[i]);
		}
		/**/
	}
}
