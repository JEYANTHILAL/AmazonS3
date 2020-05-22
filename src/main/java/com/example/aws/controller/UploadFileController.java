package com.example.aws.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Controller
@RequestMapping("/home/")
public class UploadFileController {

	@Autowired
	private AmazonS3 s3client;

	@Value("${s3.endpointUrl}")
	private String endpointUrl;

	@Value("${s3.bucketName}")
	private String bucketName;

	
	@PostMapping("/uploadFile")
	public String uploadFile(@RequestPart(value = "file") MultipartFile multipartFile) {

		String fileUrl = "";
		String status = null;
		try {

			// converting multipart file to file
			File file = convertMultiPartToFile(multipartFile);

			// filename
			String fileName = multipartFile.getOriginalFilename();

			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;

			status = uploadFileTos3bucket(fileName, file);

			file.delete();

		} catch (Exception e) {

			return "UploadController().uploadFile().Exception : " + e.getMessage();

		}

		return status + " " + fileUrl;
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String uploadFileTos3bucket(String fileName, File file) {
		try {
			s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
					.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (AmazonServiceException e) {
			return "uploadFileTos3bucket().Uploading failed :" + e.getMessage();
		}
		return "Uploading Successfull -> ";
	}

	
	@GetMapping("listfile")
	public String listFile(Model model) {
		
		HashMap<String, String> bucket=new HashMap<String,String>();
		String URL = "";
		ListObjectsV2Result result = s3client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
        	URL = endpointUrl + "" + os.getKey();
        	//bucket.add(URL);
        	//bucket.add(os.getKey());
        	
        	//bucket.put("url", URL);
        	bucket.put(URL, os.getKey());
            System.out.println("* " + os.getKey());
        }
        model.addAttribute("itemName", bucket);
        
        return "home";
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String deleteFile(@RequestParam(name="value")String key,Model model) {
		
		//List<String> bucket=new ArrayList<String>();
		try {
            s3client.deleteObject(bucketName, key);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
        
        
        
        return "redirect:/home/listfile";
	}

}