package com.honeywell.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honeywell.model.DeviceDetails;
import com.honeywell.service.DeviceService;

@Service
public class DeviceServiceImpl implements DeviceService{
	
	public ByteArrayInputStream getJsonFile(String name, String serialNo, int noOfPorts)
			throws IOException, StreamWriteException, DatabindException {
		DeviceDetails deviceDetails = new DeviceDetails();
		deviceDetails.setName(name);
		deviceDetails.setSerialNo(serialNo);
		deviceDetails.setNoOfPorts(noOfPorts);

		// Convert the DeviceDetails object to JSON
		ObjectMapper objectMapper = new ObjectMapper();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		objectMapper.writeValue(outputStream, deviceDetails);

		// Convert the output stream to an input stream
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		return inputStream;
	}
	
}
