package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.*;
import util.Common;
import util.Constants;
import play.mvc.*; 

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

/**
 * The main set of web services.
 */
@Named
@Singleton
public class ClimateServiceController extends Controller {
	public static final String WILDCARD = "%";
	private final int initialcount = 0;

	// static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";
	private final ClimateServiceRepository climateServiceRepository;
	private final UserRepository userRepository;
 
	// We are using constructor injection to receive a repository to support our
	// desire for immutability.
	@Inject
	public ClimateServiceController(
			final ClimateServiceRepository climateServiceRepository,
			UserRepository userRepository) {
		this.climateServiceRepository = climateServiceRepository;
		this.userRepository = userRepository;
 	}

	public Result addClimateService() {
		JsonNode json = request().body().asJson();
		
		if (json == null) {
			System.out
					.println("Climate service not saved, expecting Json data");
			return badRequest("Climate service not saved, expecting Json data");
		}

		// Parse JSON file
		long rootServiceId = json.findPath("rootServiceId").asLong();
		long creatorId = json.findPath("creatorId").asLong();
		String name = json.findPath("name").asText();
		String purpose = json.findPath("purpose").asText();
		String url = json.findPath("url").asText();
		String scenario = json.findPath("scenario").asText();
		Date createTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat(Common.DATE_PATTERN);
		try {
			createTime = format.parse(json.findPath("createTime").asText());
		} catch (ParseException e) {
			System.out
					.println("No creation date specified, set to current time");
		}
		String versionNo = json.findPath("versionNo").asText();

		try {
			User user = userRepository.findOne(creatorId);
			ClimateService climateService = new ClimateService(rootServiceId,
					user, name, purpose, url, scenario, createTime, versionNo);
			ClimateService savedClimateService = climateServiceRepository
					.save(climateService);
			String registerNote = "ClimateService Name: " + savedClimateService.getName() + ", VersionNo: "+versionNo;
 
			System.out.println("Climate Service saved: "
					+ savedClimateService.getName());
			return created(new Gson().toJson(savedClimateService.getId()));
		} catch (PersistenceException pe) {
			pe.printStackTrace();
			System.out.println("Climate Service not saved: " + name);
			return badRequest("Climate Service not saved: " + name);
		}
	}

	public Result getAllClimateServices(String format) {
		Iterable<ClimateService> climateServices = climateServiceRepository
				.findAll();
		if (climateServices == null) {
			System.out.println("No climate service found");
		}

		String result = new String();
		if (format.equals("json")) {
			result = new Gson().toJson(climateServices);
		}

		return ok(result);

	}
	 
	public Result deleteClimateServiceById(long id) {
		ClimateService climateService = climateServiceRepository.findOne(id);
		if (climateService == null) {
			System.out.println("Climate service not found with id: " + id);
			return notFound("Climate service not found with id: " + id);
		}

		climateServiceRepository.delete(climateService);
		System.out.println("Climate service is deleted: " + id);
		return ok("Climate service is deleted: " + id);
	}
 
}
