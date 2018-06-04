package com.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.report.entities.Report;
import com.report.entities.Result;
import com.report.services.ReportService;

@RestController
@RequestMapping(value = "/report", produces = "application/json", consumes = "application/json")
public class ReportController {

	@Autowired
	ReportService reportService;
 	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Result createReport(@RequestBody Report report) {
	
		// validation
 		if (report == null || report.getBody() == null || report.getBody().length() <= 0) {
			Result result = new Result();
			result.setCode("F");
			result.setMessage("body is missed, please send the data to populate it. ");
			return result;
		}

		return reportService.createPdfReport(report);
	}
	
	
	@RequestMapping( value = "/", method = RequestMethod.GET )
	public Result listReports() {
	
		return reportService.getFileNamesInBucket();
		
	}
	
	@RequestMapping(value = "/delete/" , method = RequestMethod.POST)
	public Result deleteReport(@RequestBody Report report) {
		System.out.println("deletion in progress : "+report.getFileName());
		if(report.getFileName() == null) {
			Result result = new Result();
			result.setCode("F");
			result.setMessage("we couldn't find file name.");
			return result;
		}
		
		return reportService.deleteFile(report.getFileName());
		
	}

}
