package main;

import java.io.IOException;

import makedata.CreateData;
import predictdata.predictData;
import tools.nlplib;

public class Document_Classification {
	private predictData predict;
	private CreateData create;
	private static nlplib lib;


	public Document_Classification() throws IOException {
		create = new CreateData();
		predict = new predictData();
		lib = new nlplib();
	}

	public static void main(String[] args) throws Exception {
		Document_Classification document_classify = new Document_Classification();
		document_classify.create.FcreateData(lib);
		document_classify.predict.FpredictData(lib);
	}
}
