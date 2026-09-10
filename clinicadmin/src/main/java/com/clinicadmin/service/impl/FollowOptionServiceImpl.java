package com.clinicadmin.service.impl;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.clinicadmin.dto.FollowOptionDTO;
import com.clinicadmin.dto.FollowUpOptionData;
import com.clinicadmin.dto.Response;
import com.clinicadmin.entity.FollowOption;
import com.clinicadmin.repository.FollowOptionRepository;
import com.clinicadmin.service.FollowOptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowOptionServiceImpl implements FollowOptionService {

    private final FollowOptionRepository repository;
    private FollowOptionDTO toDTO(FollowOption entity) {
        return FollowOptionDTO.builder()
                .id(entity.getId())
                .followOptions(entity.getFollowOptions()) // already a List<FollowUpOptionData>
                .build();
    }

    private FollowOption toEntity(FollowOptionDTO dto) {
        return FollowOption.builder()
                .followOptions(dto.getFollowOptions())
                .build();
    }


    @Override
    public Response create(FollowOptionDTO dto) {
        FollowOption saved = repository.save(toEntity(dto));
        return Response.builder()
                .success(true)
                .status(201)
                .message("Follow Option Created Successfully")
                .data(toDTO(saved))
                .build();
    }

    @Override
    public Response getAll() {
        List<FollowOptionDTO> all = repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Response.builder()
                .success(true)
                .status(200)
                .message("All Follow Options")
                .data(all)
                .build();
    }

    @Override
    public Response getById(String id) {
        Optional<FollowOption> option = repository.findById(id);
        if (option.isPresent()) {
            return Response.builder()
                    .success(true)
                    .status(200)
                    .message("Follow Option Found")
                    .data(toDTO(option.get()))
                    .build();
        } else {
            return Response.builder()
                    .success(false)
                    .status(404)
                    .message("Follow Option Not Found")
                    .build();
        }
    }
    @Override
    public Response update(String id, FollowOptionDTO dto) {
        if (dto == null || dto.getFollowOptions() == null || dto.getFollowOptions().isEmpty()) {
            return Response.builder()
                    .success(false)
                    .status(400)
                    .message("FollowOptionDTO or followOptions cannot be null/empty")
                    .build();
        }

        if (id == null || id.isBlank()) {
            return Response.builder()
                    .success(false)
                    .status(400)
                    .message("Invalid ID")
                    .build();
        }

        Optional<FollowOption> existingOption = repository.findById(id);
        if (existingOption.isPresent()) {
            FollowOption entityToUpdate = existingOption.get();

            // Filter out any null or invalid FollowUpOptionData
            List<FollowUpOptionData> validOptions = dto.getFollowOptions().stream()
                    .filter(opt -> opt != null
                            && opt.getLabel() != null && !opt.getLabel().isBlank()
                            && opt.getValue() != null && !opt.getValue().isBlank())
					.toList(); // Java 16+, for older versions use .collect(Collectors.toList())

            if (validOptions.isEmpty()) {
                return Response.builder()
                        .success(false)
                        .status(400)
                        .message("No valid follow-up options provided")
                        .build();
            }

            // Update with valid options only
            entityToUpdate.setFollowOptions(validOptions);

            FollowOption updated = repository.save(entityToUpdate);
            return Response.builder()
                    .success(true)
                    .status(200)
                    .message("Follow Option Updated Successfully")
                    .data(toDTO(updated))
                    .build();
        } else {
            return Response.builder()
                    .success(false)
                    .status(404)
                    .message("Follow Option Not Found")
                    .build();
        }
    }

    @Override
    public Response delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return Response.builder()
                    .success(true)
                    .status(200)
                    .message("Follow Option Deleted")
                    .build();
        } else {
            return Response.builder()
                    .success(false)
                    .status(404)
                    .message("Follow Option Not Found")
                    .build();
        }
    }
}
