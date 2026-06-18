package com.sbsolutions.rilybricoule.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbsolutions.rilybricoule.dto.content.UpdateAppVersionContentRequest;
import com.sbsolutions.rilybricoule.dto.version.AppVersionDTO;
import com.sbsolutions.rilybricoule.entity.AppVersion;
import com.sbsolutions.rilybricoule.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;
    private final ObjectMapper objectMapper;

    public List<AppVersionDTO> getAll() {
        return appVersionRepository.findAllByOrderByReleaseDateDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public AppVersionDTO create(AppVersionDTO request) {
        AppVersion appVersion = new AppVersion();

        applyRequest(appVersion, request);
        appVersion.setLastUpdatedAt(LocalDateTime.now());

        return toDTO(appVersionRepository.save(appVersion));
    }

    public AppVersionDTO update(Long id, AppVersionDTO request) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "App version not found"
                ));

        applyRequest(appVersion, request);
        appVersion.setLastUpdatedAt(LocalDateTime.now());

        return toDTO(appVersionRepository.save(appVersion));
    }

    public void delete(Long id) {
        if (!appVersionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "App version not found"
            );
        }

        appVersionRepository.deleteById(id);
    }

    private void applyRequest(AppVersion appVersion, AppVersionDTO request) {
        appVersion.setPlatform(request.getPlatform());
        appVersion.setVersion(request.getVersion());
        appVersion.setBuildNumber(request.getBuildNumber());
        appVersion.setReleaseDate(request.getReleaseDate());
        appVersion.setStatus(request.getStatus());
        appVersion.setForceUpdate(request.isForceUpdate());
        appVersion.setMinSupportedVersion(request.getMinSupportedVersion());
        appVersion.setNote(request.getNote());
        appVersion.setChangelog(writeChangelog(request.getChangelog()));
    }

    private AppVersionDTO toDTO(AppVersion appVersion) {
        AppVersionDTO dto = new AppVersionDTO();

        dto.setId(appVersion.getId());
        dto.setPlatform(appVersion.getPlatform());
        dto.setVersion(appVersion.getVersion());
        dto.setBuildNumber(appVersion.getBuildNumber());
        dto.setReleaseDate(appVersion.getReleaseDate());
        dto.setLastUpdatedAt(appVersion.getLastUpdatedAt());
        dto.setStatus(appVersion.getStatus());
        dto.setForceUpdate(appVersion.isForceUpdate());
        dto.setMinSupportedVersion(appVersion.getMinSupportedVersion());
        dto.setNote(appVersion.getNote());
        dto.setChangelog(readChangelog(appVersion.getChangelog()));

        return dto;
    }

    private String writeChangelog(List<AppVersionDTO.ChangelogEntryDTO> changelog) {
        try {
            return objectMapper.writeValueAsString(
                    changelog == null ? new ArrayList<>() : changelog
            );
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid changelog format"
            );
        }
    }

    @Transactional
    public AppVersionDTO updateContent(Long id, UpdateAppVersionContentRequest request) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "App version not found"
                ));

        appVersion.setChangelog(writeChangelog(request.getChangelog()));
        appVersion.setNote(request.getNote());
        appVersion.setLastUpdatedAt(LocalDateTime.now());

        return toDTO(appVersionRepository.save(appVersion));
    }

    private List<AppVersionDTO.ChangelogEntryDTO> readChangelog(String changelogJson) {
        if (changelogJson == null || changelogJson.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    changelogJson,
                    new TypeReference<List<AppVersionDTO.ChangelogEntryDTO>>() {}
            );
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}