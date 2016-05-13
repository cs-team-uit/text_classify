package main;

import java.io.IOException;

import makedata.createData;
import predictdata.predictData;
import tools.nlplib;

public class documentClassifier {
	private predictData predict;
	private createData create;
	private static nlplib lib;

	public documentClassifier() throws IOException {
		create = new createData();
		predict = new predictData();
		lib = new nlplib();
	}

	public static void main(String[] args) throws Exception {
		documentClassifier document_classify = new documentClassifier();
		document_classify.create.FcreateData(lib);
		document_classify.predict.FpredictData(lib);
	}
}
