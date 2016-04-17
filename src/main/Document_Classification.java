package main;

import java.io.IOException;

import makedata.CreateData;
import predictdata.predictData;

public class Document_Classification {
	private predictData pd;
	private CreateData cd;

	public Document_Classification() throws IOException {
		pd = new predictData();
		cd = new CreateData();
	}

	public static void main(String[] args) throws IOException {
		Document_Classification document_classify = new Document_Classification();
		document_classify.cd.FcreateData();
		document_classify.pd.FpredictData();
	}
}
