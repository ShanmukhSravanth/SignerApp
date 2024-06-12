package com.honeywell.controller;

import java.io.ByteArrayInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.honeywell.service.DeviceService;

@Controller
public class DeviceController {

	private final DeviceService deviceService;

	@Autowired
	public DeviceController(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	@GetMapping("/form")
	public String showForm(Model model) {
		return "form";
	}

	@PostMapping("/generateJson")
	public ResponseEntity<InputStreamResource> generateJsonFile(@RequestParam("name") String name,
			@RequestParam("serialNo") String serialNo, @RequestParam("noOfPorts") int noOfPorts) {

		try {
			// Create a DeviceDetails object
			ByteArrayInputStream inputStream = deviceService.getJsonFile(name, serialNo, noOfPorts);

			// Return the JSON file in the response
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=device_details.json")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(new InputStreamResource(inputStream));
		} catch (Exception e) {
			return ResponseEntity.status(500).build();
		}
	}

	

}
