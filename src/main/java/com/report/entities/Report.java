package com.report.entities;

public class Report {

	private String title;
	private String body;
	private String date;
	private String to;
	private String from;
	private String fileName;
	private String logo = "https://s3.amazonaws.com/elasticbeanstalk-us-east-1-794942786245/logo.png";//System.getProperty("user.dir") + "/src/main/resources/static/img/logo.png" ;
	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String created_at) {
		this.date = created_at;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

}
