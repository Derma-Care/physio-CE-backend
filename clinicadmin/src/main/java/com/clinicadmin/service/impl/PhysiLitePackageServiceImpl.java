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

import com.clinicadmin.dto.PhysiLitePackageDTO;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.PhysiLitePackage;
import com.clinicadmin.repository.PhysiLitePackageRepository;
import com.clinicadmin.service.PhysiLitePackageService;

@Service
public class PhysiLitePackageServiceImpl implements PhysiLitePackageService {

	@Autowired
	private PhysiLitePackageRepository physiLitePackageRepository;

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Random RANDOM = new Random();

	@Override
	public Response createPackage(String clinicId, String branchId, PhysiLitePackageDTO dto) {

		try {
			PhysiLitePackage entity = convertToEntity(dto);
			entity.setId(null);
			entity.setClinicId(clinicId);
			entity.setBranchId(branchId);
			entity.setPackageId(generateUniquePackageId(clinicId, branchId));

			PhysiLitePackage saved = physiLitePackageRepository.save(entity);

			return Response.builder().success(true).message("PhysiLitePackage created successfully")
					.status(HttpStatus.CREATED.value()).data(convertToDTO(saved)).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to create PhysiLitePackage: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response updatePackage(String id, String clinicId, String branchId, PhysiLitePackageDTO dto) {

		try {
			Optional<PhysiLitePackage> existingOpt = physiLitePackageRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysiLitePackage not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.NOT_FOUND.value())
						.build();
			}

			PhysiLitePackage existing = existingOpt.get();
			existing.setPackageName(dto.getPackageName());
			existing.setPackageType(dto.getPackageType());
			existing.setPrice(dto.getPrice());
			existing.setNoOfSittings(dto.getNoOfSittings());
			// clinicId, branchId and packageId are immutable identifiers, not updated here

			PhysiLitePackage updated = physiLitePackageRepository.save(existing);

			return Response.builder().success(true).message("PhysiLitePackage updated successfully")
					.status(HttpStatus.OK.value()).data(convertToDTO(updated)).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to update PhysiLitePackage: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response deletePackage(String id, String clinicId, String branchId) {

		try {
			Optional<PhysiLitePackage> existingOpt = physiLitePackageRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysiLitePackage not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.OK.value())
						.build();
			}

			physiLitePackageRepository.delete(existingOpt.get());

			return Response.builder().success(true).message("PhysiLitePackage deleted successfully")
					.status(HttpStatus.OK.value()).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to delete PhysiLitePackage: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getPackageById(String id, String clinicId, String branchId) {

		try {
			Optional<PhysiLitePackage> existingOpt = physiLitePackageRepository.findByIdAndClinicIdAndBranchId(id,
					clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false).message("PhysiLitePackage not found for id: " + id
						+ ", clinicId: " + clinicId + ", branchId: " + branchId).status(HttpStatus.OK.value())
						.build();
			}

			return Response.builder().success(true).message("PhysiLitePackage fetched successfully")
					.status(HttpStatus.OK.value()).data(convertToDTO(existingOpt.get())).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysiLitePackage: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getAllPackagesByClinicIdAndBranchId(String clinicId, String branchId) {

		try {
			List<PhysiLitePackage> list = physiLitePackageRepository.findByClinicIdAndBranchId(clinicId, branchId);
			List<PhysiLitePackageDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());

			return Response.builder().success(true)
					.message(dtoList.isEmpty() ? "No PhysiLitePackage records found"
							: "PhysiLitePackages fetched successfully")
					.status(HttpStatus.OK.value()).data(dtoList).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysiLitePackages: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	@Override
	public Response getAllPackagesByClinicId(String clinicId) {

		try {
			List<PhysiLitePackage> list = physiLitePackageRepository.findByClinicId(clinicId);
			List<PhysiLitePackageDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());

			return Response.builder().success(true)
					.message(dtoList.isEmpty() ? "No PhysiLitePackage records found"
							: "PhysiLitePackages fetched successfully")
					.status(HttpStatus.OK.value()).data(dtoList).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to fetch PhysiLitePackages: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}

	/**
	 * Generates a meaningful, human-readable, guaranteed-unique package id. Format:
	 * PKG-<clinicIdPrefix>-<branchIdPrefix>-<timestamp>-<random3digit> e.g.
	 * PKG-CLI001-BR01-20260725143210-482
	 *
	 * Uniqueness is verified against the database in a loop (extremely unlikely to
	 * collide given timestamp + random suffix, but we still guard against it).
	 */
	private String generateUniquePackageId(String clinicId, String branchId) {

		String clinicPrefix = sanitizePrefix(clinicId);
		String branchPrefix = sanitizePrefix(branchId);

		String candidate;
		int attempts = 0;

		do {
			String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
			int randomSuffix = 100 + RANDOM.nextInt(900); // 3 digit random number (100-999)

			candidate = String.format("PKG-%s-%s-%s-%d", clinicPrefix, branchPrefix, timestamp, randomSuffix);
			attempts++;

			if (attempts > 10) {
				// extreme fallback safety net, practically never reached
				candidate = candidate + "-" + System.nanoTime();
			}

		} while (physiLitePackageRepository.existsByPackageId(candidate));

		return candidate;
	}

	private String sanitizePrefix(String value) {
		if (value == null || value.isBlank()) {
			return "NA";
		}
		String cleaned = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
		return cleaned.length() > 6 ? cleaned.substring(0, 6) : cleaned;
	}

	private PhysiLitePackage convertToEntity(PhysiLitePackageDTO dto) {
		PhysiLitePackage entity = new PhysiLitePackage();
		entity.setId(dto.getId());
		entity.setPackageId(dto.getPackageId());
		entity.setPackageName(dto.getPackageName());
		entity.setPackageType(dto.getPackageType());
		entity.setPrice(dto.getPrice());
		entity.setNoOfSittings(dto.getNoOfSittings());
		return entity;
	}

	private PhysiLitePackageDTO convertToDTO(PhysiLitePackage entity) {
		PhysiLitePackageDTO dto = new PhysiLitePackageDTO();
		dto.setId(entity.getId());
		dto.setPackageId(entity.getPackageId());
		dto.setPackageName(entity.getPackageName());
		dto.setPackageType(entity.getPackageType());
		dto.setPrice(entity.getPrice());
		dto.setNoOfSittings(entity.getNoOfSittings());
		return dto;
	}
	@Override
	public Response deletePackageByPackageId(String packageId, String clinicId, String branchId) {

		try {
			Optional<PhysiLitePackage> existingOpt = physiLitePackageRepository
					.findByPackageIdAndClinicIdAndBranchId(packageId, clinicId, branchId);

			if (existingOpt.isEmpty()) {
				return Response.builder().success(false)
						.message("PhysiLitePackage not found for packageId: " + packageId
								+ ", clinicId: " + clinicId + ", branchId: " + branchId)
						.status(HttpStatus.OK.value()).build();
			}

			physiLitePackageRepository.delete(existingOpt.get());

			return Response.builder().success(true).message("PhysiLitePackage deleted successfully")
					.status(HttpStatus.OK.value()).build();

		} catch (Exception e) {
			return Response.builder().success(false).message("Failed to delete PhysiLitePackage: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
		}
	}
}