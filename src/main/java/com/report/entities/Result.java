package com.report.entities;

import java.util.ArrayList;
import java.util.List;
import com.report.entities.FileAwsDetails;
public class Result {

	String code;
	String message;
	String path;
	List<FileAwsDetails> fileList = new ArrayList<FileAwsDetails>();

	public Result(String code, String message, String path) {
		super();
		this.code = code;
		this.message = message;
		this.path = path;
	}

	public Result() {
	}

	public List<FileAwsDetails> getFileNameList() {
		return fileList;
	}

	public Result setFileNameList(List<FileAwsDetails> fileList) {
		this.fileList = fileList;
		return this;
	}

	public String getCode() {
		return code;
	}

	public Result setCode(String code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public Result setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getPath() {
		return path;
	}

	public Result setPath(String path) {
		this.path = path;
		return this;
	}

}
