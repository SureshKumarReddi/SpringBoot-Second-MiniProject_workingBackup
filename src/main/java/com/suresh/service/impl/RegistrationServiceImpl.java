package com.suresh.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.suresh.bindings.User;
import com.suresh.constants.AppConstants;
import com.suresh.exceptions.RegistrationException;
import com.suresh.models.CityEntity;
import com.suresh.models.CountryEntity;
import com.suresh.models.StateEntity;
import com.suresh.models.UserEntity;
import com.suresh.props.AppsProps;
import com.suresh.repositories.CityMasterRepository;
import com.suresh.repositories.CountryMasterRepository;
import com.suresh.repositories.StateMasterRepository;
import com.suresh.repositories.UserDetailsRepository;
import com.suresh.service.RegistrationService;
import com.suresh.utils.EmailUtils;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	@Autowired
	UserDetailsRepository userRepository;
	@Autowired
	CountryMasterRepository countryRepository;
	@Autowired
	StateMasterRepository stateRepository;
	@Autowired
	CityMasterRepository cityRepository;
	@Autowired
	private EmailUtils email;
	@Autowired
	private AppsProps props;

	@Override
	public boolean uniqueEmail(String email) {
		UserEntity user = userRepository.findByUserEmail(email);
		return user.getUserEmail().equals(email) ? false : true;
	}

	@Override
	public Map<Integer, String> getCountries() {
		List<CountryEntity> countriesList = countryRepository.findAll();
		Map<Integer, String> countries = new HashMap<>();
		for (CountryEntity c : countriesList) {
			countries.put(c.getCountryId(), c.getCountryName());
		}
		return countries;
	}

	@Override
	public Map<Integer, String> getStates(Integer countryId) {
		List<StateEntity> states = stateRepository.findByCountryId(countryId);

		Map<Integer, String> stateMap = new HashMap<>();

		for (StateEntity s : states) {
			stateMap.put(s.getStateId(), s.getStateName());
		}
		return stateMap;
	}

	@Override
	public Map<Integer, String> getCities(Integer stateId) {
		List<CityEntity> findByStateId = cityRepository.findByStateId(stateId);
		Map<Integer, String> citiesMap = new HashMap<>();

		for (CityEntity s : findByStateId) {
			citiesMap.put(s.getCityId(), s.getCityName());
		}
		return citiesMap;
	}

	@Override
	public boolean registerUser(User user) {

		user.setUserPassword(generateRandomPassword());
		user.setUserAccountStatus(AppConstants.ACCOUNT_STATUS);
		UserEntity entity = new UserEntity();

		BeanUtils.copyProperties(user, entity);

		UserEntity userSaved = userRepository.save(entity);
		if (userSaved.getUserId() != null) {
			sendEmail(user);
			return true;
		}

		return false;
	}

	private String generateRandomPassword() {
		String generatePassword = null;
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		generatePassword = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		return generatePassword;
	}

	private boolean sendEmail(User user) {
		boolean emailSent = false;
		try {
			Map<String, String> messages = props.getMessages();

			String subject = messages.get(AppConstants.REG_MAIL_SUBJECT);
			;
			String to = user.getUserEmail();
			String bodyFileName = messages.get(AppConstants.REG_BODY_MAIL_TEMPLATE);
			String body = readMailBody(bodyFileName, user);

			email.sendEmail(subject, body, to);
			emailSent = true;
		} catch (Exception e) {
			throw new RegistrationException(e.getMessage());
		}

		return false;
	}

	public String readMailBody(String fileName, User user) {
		String body = null;

		StringBuffer buffer = new StringBuffer();
		Path path = Paths.get(fileName);

		try (Stream<String> stream = Files.lines(path)) {
			stream.forEach(line -> buffer.append(line));

			body = buffer.toString();
			body = body.replace(AppConstants.FNAME, user.getFirstName());
			body = body.replace(AppConstants.TEMP_PWD, user.getUserPassword());
			body = body.replace(AppConstants.EMAIL, user.getUserEmail());
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		return body;
	}
}
