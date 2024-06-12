package com.honeywell.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;

@Service
public interface DeviceService {
	
	public ByteArrayInputStream getJsonFile(String name, String serialNo, int noOfPorts) throws IOException, StreamWriteException, DatabindException;

}
