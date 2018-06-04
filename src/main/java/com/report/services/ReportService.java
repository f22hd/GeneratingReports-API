package com.report.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.report.entities.FileAwsDetails;
// Report , Result
import com.report.entities.Report;
import com.report.entities.Result;

@Service
public class ReportService {
	
	
	private Properties props;
	// S3
	private String accessKey;
	private String secretKey;
	private String bucketName;

	private AmazonS3 s3;
	private final String SEPARATOR = System.getProperty("file.separator");
	private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	private final String DESTINATION = System.getProperty("user.dir") + SEPARATOR +"reports"+ SEPARATOR;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
	private final String generatedDate = new SimpleDateFormat("yyyy / MM / dd").format(timestamp);
	private final String ARABIC_FONT_LOCATION = System.getProperty("user.dir") + "/src/main/resources/static/fonts/Hasan-Enas.ttf";
	private final Font arabicFont = FontFactory.getFont(ARABIC_FONT_LOCATION, BaseFont.IDENTITY_H, 16);
	private final String EXT = ".pdf";

	private String globalErrorMessage;

	
	// constructor
	public ReportService() {
		try {

			props = new Properties();
			props.load(ReportService.class.getResourceAsStream("/application.properties"));
			// loading from application.properties
			accessKey = props.getProperty("aws.access.key");
			secretKey = props.getProperty("aws.secret.key");  
			bucketName = props.getProperty("aws.s3.bucketname");

			BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

			s3 = AmazonS3ClientBuilder
					.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withRegion(Regions.US_EAST_1)
					.build();
			
			// create new bucket if not exist
			if(!s3.doesBucketExistV2(bucketName)) {
				s3.createBucket(bucketName);
			}
			
		} catch (IOException e) {
			System.out.println("unable to load application properties");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String uploadToAws(File file) {
		try {

			if (!s3.doesBucketExistV2(bucketName)) {
				// create new bucket with this name
				s3.createBucket(bucketName);
				System.out.println("created new bucket : " + bucketName);
			}

			System.out.println("uploading file...");

			// upload file and make it public read access
			PutObjectRequest putObjReq = new PutObjectRequest(bucketName, file.getName(), file);
			// make public access
			putObjReq.withCannedAcl(CannedAccessControlList.PublicRead);
			
			s3.putObject(putObjReq);

 
			System.out.println("uploaded successfully..");
			
			return getObjectUrl(file.getName());

		} catch (Exception ex) {
			ex.printStackTrace();
			globalErrorMessage = ex.getMessage();
		}

		return "";
	}

	public Result deleteFile(String fileName) {
		Result result = new Result();
		try {

			try {
				// check object name if exist or not in the bucket, 
				//if not exist will throw an exception.
				s3.getObjectMetadata(bucketName, fileName);

			} catch (AmazonS3Exception ex) {
				return result.setCode("F").setMessage("unable to find object with this name " + fileName);
			}

			System.out.println("deleting in progress for " + fileName);
			DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, fileName);
			s3.deleteObject(deleteRequest);

			System.out.println("file have been deleted successfully.");

			return result.setCode("S").setMessage("file have been deleted successfully.");

		} catch (Exception ex) {
			System.out.println("Error, in deleteFile");
			ex.printStackTrace();
		}

		// otherwise
		result.setCode("F");
		result.setMessage("unable to delete the file");
		return result;
	}

	// list files in a bucket
	public Result getFileNamesInBucket() {

		ListObjectsRequest request = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(2);
		ObjectListing objectListing;

		List<FileAwsDetails> listObjects = new ArrayList<>();

		Result result = new Result();

		do {
			objectListing = s3.listObjects(request);

			for (S3ObjectSummary summaryObj : objectListing.getObjectSummaries()) {

				FileAwsDetails details = new FileAwsDetails();
				details.setName(summaryObj.getKey());
				details.setSize(summaryObj.getSize());
				details.setLast_modified(summaryObj.getLastModified());

				details.setUrl(getObjectUrl(summaryObj.getKey()));

				listObjects.add(details);
			}

			request.setMarker(objectListing.getNextMarker());

		} while (objectListing.isTruncated());

		result.setCode("S");
		result.setFileNameList(listObjects);

		return result;
	}

	// create new pdf report
	public Result createPdfReport(Report report) {
		File dest = new File(DESTINATION);

		if (!dest.exists()) {
			dest.mkdirs();
			System.out.println("folder is created ... " + dest.getAbsolutePath());
		}

		String fileName = "";
		if (report.getFileName() != null) {
			fileName = report.getFileName() + EXT;
		} else {
			// random
			fileName = dateFormat.format(timestamp) + EXT;
		}

		String path = dest.getPath() + SEPARATOR + fileName;
		System.out.println(path);
		Result result = new Result();

		Document document = new Document(PageSize.A4);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));
			writer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

			document.open();
			Chunk chunk;
			Paragraph p;
			PdfPTable headerTable = new PdfPTable(2);
			headerTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			headerTable.setWidthPercentage(PageSize.A4.getWidth());

			Image logo = Image.getInstance(report.getLogo());
			logo.setAlignment(Element.ALIGN_TOP);
			logo.scaleToFit(80, 100);
			logo.setAbsolutePosition(document.right() - 70, document.top() - 40); // put the logo in top right page

			writer.getDirectContent().addImage(logo);

			if (report.getTitle() != null) {

				arabicFont.setColor(BaseColor.BLUE);
				arabicFont.setSize(22f);
				arabicFont.setStyle(Font.BOLD);

				chunk = new Chunk(report.getTitle(), arabicFont);
				p = new Paragraph(chunk);
				p.setAlignment(Element.ALIGN_CENTER); // in center
				p.setFont(arabicFont);

				PdfContentByte canvas = writer.getDirectContent();

				float x = (document.right() + document.left()) / 2 + document.leftMargin();
				float y = document.top() - 5;
				float rotation = 0;

				ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, p, x, y, rotation, PdfWriter.RUN_DIRECTION_RTL,
						0);

			}

			// date
			arabicFont.setColor(BaseColor.DARK_GRAY);
			arabicFont.setSize(13f);
			arabicFont.setStyle(Font.NORMAL);

			if (report.getDate() != null) {
				chunk = new Chunk("تاريخ : " + report.getDate(), arabicFont);
			} else {
				// auto
				chunk = new Chunk("تاريخ : " + generatedDate, arabicFont);
			}

			p = new Paragraph(chunk);

			PdfContentByte canvas = writer.getDirectContent();

			float x = document.left(20) + document.leftMargin();
			float y = document.top() - 5;
			float rotation = 0;
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, p, x, y, rotation, PdfWriter.RUN_DIRECTION_RTL, 0);

			// add two line space after header
			document.add(new Phrase("\n\n"));

			LineSeparator ls = new LineSeparator();
			document.add(new Chunk(ls));

			// body
			if (report.getBody() != null) {
				// set content from Right to Left
				arabicFont.setColor(BaseColor.DARK_GRAY);
				arabicFont.setSize(17f);
				arabicFont.setStyle(Font.NORMAL);

				chunk = new Chunk(report.getBody(), arabicFont);
				Paragraph phrase = new Paragraph(chunk);

				PdfPTable bodyTable = new PdfPTable(1);
				bodyTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

				PdfPCell bodyCell = new PdfPCell(phrase);
				bodyCell.setBorder(Rectangle.NO_BORDER);

				bodyTable.addCell(bodyCell);

				document.add(bodyTable);

			}

			// add three line space after body
			document.add(new Phrase("\n\n\n"));

			// footer
			PdfPTable footer = new PdfPTable(2);
			footer.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			footer.setWidthPercentage(100);
			arabicFont.setColor(BaseColor.BLACK);
			arabicFont.setSize(17f);
			Paragraph managerSign = new Paragraph(new Chunk("توقيع المدير", arabicFont));
			Paragraph clientSign = new Paragraph(new Chunk("توقيع العميل", arabicFont));

			managerSign.setAlignment(Element.ALIGN_LEFT); // this paragraph on the right side

			PdfPCell managerCell = new PdfPCell();
			managerCell.setBorder(Rectangle.NO_BORDER);

			managerCell.addElement(managerSign);
			footer.addCell(managerCell);

			PdfPCell clientCell = new PdfPCell();
			clientCell.setBorder(Rectangle.NO_BORDER);

			clientCell.addElement(clientSign);
			footer.addCell(clientCell);

			document.add(footer);

			document.close();

			String url = uploadToAws(new File(path));

			if (url != null) {

				result.setCode("S");
				result.setMessage("Report created Successfully.");
				result.setPath(url);

			} else {

				result.setCode("F");
				result.setMessage("can't upload the file. " + globalErrorMessage);
				result.setPath(null);

			}

			deleteLocalFile(path);

			System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
			result.setCode("F");
			result.setMessage("Error in creating pdf. " + ex.getMessage());
		}

		return result;

	}

	private void deleteLocalFile(String path) {
		// delete the file after uploading to AWS
		File file = new File(path);

		if (!file.isDirectory() && file.exists()) {
			file.delete();
			System.out.println("Deleted file from local folder after uploaded to AWS");
		}
	}

	private String getObjectUrl(String objectName) {
		return "https://s3.amazonaws.com/" + bucketName + "/" + objectName;
	}
}
