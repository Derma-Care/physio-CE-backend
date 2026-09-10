package com.clinicadmin.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.clinicadmin.dto.PhysiLiteSittingDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.PhysioLiteSitting;
import com.clinicadmin.repository.PhysioLiteSittingRepository;
import com.clinicadmin.service.PhysioLiteSittingService;

@Service
public class PhysioLiteSittingServiceImpl implements PhysioLiteSittingService {

	@Autowired
	private PhysioLiteSittingRepository physioLiteSittingRepository;

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Random RANDOM = new Random();

	@Override
	public Response createSitting(PhysiLiteSittingDTO dto) {

		try {
			PhysioLiteSitting entity = convertToEntity(dto);
			entity.setId(null);
			entity.setSittingId(generateUniqueSittingId(dto.getClinicId(), dto.getBranchId()));

			PhysioLiteSitting saved = physioLiteSittingRepository.save(entity);

			return Response.builder().success(true).message("PhysioLiteSitting created successfully")
					.status(HttpStatus.CREATED.value()).data(convertToDTO(saved)).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to create PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response updateSitting(String id, String clinicId, String branchId, PhysiLiteSittingDTO dto) {

		try {
			Optional<PhysioLiteSitting> existingOpt = physioLiteSittingRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysioLiteSitting not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.OK.value())
						.build();
			}

			PhysioLiteSitting existing = existingOpt.get();
			existing.setSittingName(dto.getSittingName());
			existing.setPackageType(dto.getPackageType());
			existing.setPrice(dto.getPrice());
			existing.setNoOfSittings(dto.getNoOfSittings());
			// clinicId, branchId and sittingId are immutable identifiers, not updated here

			PhysioLiteSitting updated = physioLiteSittingRepository.save(existing);

			return Response.builder().success(true).message("PhysioLiteSitting updated successfully")
					.status(HttpStatus.OK.value()).data(convertToDTO(updated)).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to update PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response deleteSitting(String id, String clinicId, String branchId) {

		try {
			Optional<PhysioLiteSitting> existingOpt = physioLiteSittingRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysioLiteSitting not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.OK.value())
						.build();
			}

			physioLiteSittingRepository.delete(existingOpt.get());

			return Response.builder().success(true).message("PhysioLiteSitting deleted successfully")
					.status(HttpStatus.OK.value()).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to delete PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getSittingById(String id, String clinicId, String branchId) {

		try {
			Optional<PhysioLiteSitting> existingOpt = physioLiteSittingRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysioLiteSitting not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.OK.value())
						.build();
			}

			return Response.builder().success(true).message("PhysioLiteSitting fetched successfully")
					.status(HttpStatus.OK.value()).data(convertToDTO(existingOpt.get())).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getAllSittingsByClinicIdAndBranchId(String clinicId, String branchId) {

		try {
			List<PhysioLiteSitting> list = physioLiteSittingRepository.findByClinicIdAndBranchId(clinicId, branchId);
			List<PhysiLiteSittingDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());

			return Response.builder().success(true)
					.message(dtoList.isEmpty() ? "No PhysioLiteSitting records found"
							: "PhysioLiteSittings fetched successfully")
					.status(HttpStatus.OK.value()).data(dtoList).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysioLiteSittings: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getAllSittingsByClinicId(String clinicId) {

		try {
			List<PhysioLiteSitting> list = physioLiteSittingRepository.findByClinicId(clinicId);
			List<PhysiLiteSittingDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());

			return Response.builder().success(true)
					.message(dtoList.isEmpty() ? "No PhysioLiteSitting records found"
							: "PhysioLiteSittings fetched successfully")
					.status(HttpStatus.OK.value()).data(dtoList).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysioLiteSittings: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	/**
	 * Generates a meaningful, human-readable, guaranteed-unique sitting id. Format:
	 * PLS-<clinicIdPrefix>-<branchIdPrefix>-<timestamp>-<random3digit> e.g.
	 * PLS-CLI001-BR01-20260725143210-482
	 *
	 * Uniqueness is verified against the database in a loop (extremely unlikely to
	 * collide given timestamp + random suffix, but we still guard against it).
	 */
	private String generateUniqueSittingId(String clinicId, String branchId) {

		String clinicPrefix = sanitizePrefix(clinicId);
		String branchPrefix = sanitizePrefix(branchId);

		String candidate;
		int attempts = 0;

		do {
			String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
			int randomSuffix = 100 + RANDOM.nextInt(900); // 3 digit random number (100-999)

			candidate = String.format("PLS-%s-%s-%s-%d", clinicPrefix, branchPrefix, timestamp, randomSuffix);
			attempts++;

			if (attempts > 10) {
				// extreme fallback safety net, practically never reached
				candidate = candidate + "-" + System.nanoTime();
			}

		} while (physioLiteSittingRepository.existsBySittingId(candidate));

		return candidate;
	}

	@Override
	public Response getSittingBySittingId(String sittingId, String clinicId, String branchId) {

		try {

			Optional<PhysioLiteSitting> sitting = physioLiteSittingRepository
					.findBySittingIdAndClinicIdAndBranchId(sittingId, clinicId, branchId);

			if (sitting.isEmpty()) {
				return Response.builder().success(false).message("PhysioLiteSitting not found")
						.status(HttpStatus.OK.value()).build();
			}

			return Response.builder().success(true).message("PhysioLiteSitting fetched successfully")
					.status(HttpStatus.OK.value()).data(convertToDTO(sitting.get())).build();

		} catch (Exception e) {

			return Response.builder().success(false).message("Failed to fetch PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}
	@Override
	public Response deleteSittingBySittingId(String sittingId, String clinicId, String branchId) {

		try {
			Optional<PhysioLiteSitting> existingOpt = physioLiteSittingRepository
					.findBySittingIdAndClinicIdAndBranchId(sittingId, clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false)
						.message("PhysioLiteSitting not found for sittingId: " + sittingId
								+ ", clinicId: " + clinicId + ", branchId: " + branchId)
						.status(HttpStatus.NOT_FOUND.value()).build();
			}

			physioLiteSittingRepository.delete(existingOpt.get());

			return Response.builder().success(true).message("PhysioLiteSitting deleted successfully")
					.status(HttpStatus.OK.value()).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to delete PhysioLiteSitting: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	private String sanitizePrefix(String value) {
		if (value == null || value.isBlank()) {
			return "NA";
		}
		String cleaned = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
		return cleaned.length() > 6 ? cleaned.substring(0, 6) : cleaned;
	}

	private PhysioLiteSitting convertToEntity(PhysiLiteSittingDTO dto) {
		PhysioLiteSitting entity = new PhysioLiteSitting();
		entity.setId(dto.getId());
		entity.setSittingId(dto.getSittingId());
		entity.setSittingName(dto.getSittingName());
		entity.setPackageType(dto.getPackageType());
		;
		entity.setPrice(dto.getPrice());
		entity.setNoOfSittings(dto.getNoOfSittings());
		entity.setClinicId(dto.getClinicId());
		entity.setBranchId(dto.getBranchId());
		return entity;
	}

	private PhysiLiteSittingDTO convertToDTO(PhysioLiteSitting entity) {
		PhysiLiteSittingDTO dto = new PhysiLiteSittingDTO();
		dto.setId(entity.getId());
		dto.setSittingId(entity.getSittingId());
		dto.setSittingName(entity.getSittingName());
		dto.setPackageType(entity.getPackageType());
		dto.setPrice(entity.getPrice());
		dto.setNoOfSittings(entity.getNoOfSittings());
		dto.setClinicId(entity.getClinicId());
		dto.setBranchId(entity.getBranchId());
		return dto;
	}
}