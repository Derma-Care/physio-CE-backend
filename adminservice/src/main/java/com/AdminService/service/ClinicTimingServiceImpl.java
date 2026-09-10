package com.AdminService.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.AdminService.dto.ClinicTimingDTO;
import com.AdminService.entity.ClinicTiming;
import com.AdminService.repository.ClinicTimingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClinicTimingServiceImpl implements ClinicTimingService {
    private final ClinicTimingRepository repo;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private static final int OPEN_START = 7;   // 07:00 AM
    private static final int OPEN_END   = 22;  // 10:00 PM

    @Override
    @Transactional
    public List<ClinicTimingDTO> createTimings(ClinicTimingDTO dto) {

        LocalTime start = LocalTime.parse(dto.getOpeningTime(), FMT);
        validateHour(start.getHour());

        List<ClinicTimingDTO> generated = new ArrayList<>();

        if (dto.getClosingTime() == null || dto.getClosingTime().isBlank()) {
            LocalTime end = start.plusHours(1);
            generated.add(saveSlot(start, end));
        } else {
            LocalTime endRange = LocalTime.parse(dto.getClosingTime(), FMT);
            if (!endRange.isAfter(start))
                throw new IllegalArgumentException("closingTime must be after openingTime");

            for (LocalTime s = start; s.isBefore(endRange); s = s.plusHours(1)) {
                validateHour(s.getHour());
                LocalTime e = s.plusHours(1);
                generated.add(saveSlot(s, e));
            }
        }
        return generated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicTimingDTO> getAllTimings() {
        seedIfEmpty();
        return repo.findAllByOrderByStartHourAsc()
                   .stream()
                   .map(t -> new ClinicTimingDTO(t.getOpeningTime(), t.getClosingTime()))
                   .collect(Collectors.toList());
    }

    private ClinicTimingDTO saveSlot(LocalTime s, LocalTime e) {
        ClinicTiming saved = repo.save(
                new ClinicTiming(null, s.getHour(), s.format(FMT), e.format(FMT)));
        return new ClinicTimingDTO(saved.getOpeningTime(), saved.getClosingTime());
    }

    private void validateHour(int hour) {
        if (hour < OPEN_START || hour >= OPEN_END)
            throw new IllegalArgumentException(
                    "Hour must be between 07:00 AM and 09:00 PM");
    }

    @Transactional
    protected void seedIfEmpty() {
        if (repo.count() > 0) return;
        IntStream.rangeClosed(OPEN_START, OPEN_END - 1)
                 .forEach(h -> saveSlot(LocalTime.of(h, 0),
                                        LocalTime.of(h + 1, 0)));
    }
}
