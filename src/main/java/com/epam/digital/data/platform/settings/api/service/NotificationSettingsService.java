/*
 * Copyright 2026 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.settings.api.service;

import com.epam.digital.data.platform.settings.api.model.NotificationChannel;
import com.epam.digital.data.platform.settings.api.model.Settings;
import com.epam.digital.data.platform.settings.api.repository.NotificationChannelRepository;
import com.epam.digital.data.platform.settings.api.repository.SettingsRepository;
import com.epam.digital.data.platform.settings.model.dto.CreateNotificationSettingsInputDto;
import com.epam.digital.data.platform.settings.model.dto.CreateNotificationSettingsOutputDto;
import com.epam.digital.data.platform.settings.model.dto.NotificationChannelInputDto;
import com.epam.digital.data.platform.settings.model.dto.NotificationChannelOutputDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSettingsService {

  private final Logger log = LoggerFactory.getLogger(NotificationSettingsService.class);

  private final SettingsRepository settingsRepository;
  private final NotificationChannelRepository channelRepository;

  public NotificationSettingsService(
      SettingsRepository settingsRepository,
      NotificationChannelRepository channelRepository) {
    this.settingsRepository = settingsRepository;
    this.channelRepository = channelRepository;
  }

  @Transactional
  public CreateNotificationSettingsOutputDto createNotificationSettings(
      CreateNotificationSettingsInputDto input) {
    log.info("Creating notification settings for keycloak user id: {}", input.getKeycloakUserId());

    // Get or create settings record
    Settings settings = settingsRepository.getByKeycloakId(input.getKeycloakUserId());
    log.debug("Settings record obtained with id: {}", settings.getId());

    // Process each channel
    List<NotificationChannelOutputDto> createdChannels = new ArrayList<>();
    for (NotificationChannelInputDto channelInput : input.getChannels()) {
      log.debug("Processing channel: {} for settings id: {}", channelInput.getChannel(), settings.getId());

      // Delete existing channel record if it exists
      channelRepository.deleteBySettingsIdAndChannel(settings.getId(), channelInput.getChannel());
      log.debug("Deleted existing channel record (if any) for settings id: {} and channel: {}",
          settings.getId(), channelInput.getChannel());

      // Create new channel record
      channelRepository.create(
          settings.getId(),
          channelInput.getChannel(),
          channelInput.getAddress(),
          channelInput.getIsActivated(),
          channelInput.getDeactivationReason()
      );
      log.debug("Created new channel record for settings id: {} and channel: {}",
          settings.getId(), channelInput.getChannel());

      // Retrieve the created channel to get the generated ID and timestamps
      NotificationChannel createdChannel = channelRepository
          .findBySettingsIdAndChannel(settings.getId(), channelInput.getChannel())
          .orElseThrow(() -> new IllegalStateException(
              "Failed to retrieve created channel for settings id: " + settings.getId()));

      // Build output DTO
      NotificationChannelOutputDto outputDto = NotificationChannelOutputDto.builder()
          .id(createdChannel.getId())
          .channel(createdChannel.getChannel())
          .address(createdChannel.getAddress())
          .isActivated(createdChannel.isActivated())
          .createdAt(createdChannel.getCreatedAt())
          .build();
      createdChannels.add(outputDto);
    }

    log.info("Successfully created notification settings for keycloak user id: {} with {} channels",
        input.getKeycloakUserId(), createdChannels.size());

    // Build and return response
    return CreateNotificationSettingsOutputDto.builder()
        .settingsId(settings.getId())
        .channels(createdChannels)
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Transactional
  public void deleteNotificationSettings(UUID keycloakUserId) {
    log.info("Deleting notification settings for keycloak user id: {}", keycloakUserId);

    // Find settings by keycloak ID
    Settings settings = settingsRepository.findByKeycloakId(keycloakUserId)
        .orElseThrow(() -> {
          log.warn("Settings not found for keycloak user id: {}", keycloakUserId);
          return new IllegalArgumentException("Settings not found for keycloak user id: " + keycloakUserId);
        });

    log.debug("Found settings with id: {} for keycloak user id: {}", settings.getId(), keycloakUserId);

    // Delete all notification channels for this settings
    channelRepository.deleteBySettingsId(settings.getId());
    log.debug("Deleted all notification channels for settings id: {}", settings.getId());

    // Delete the settings record
    settingsRepository.delete(settings);
    log.info("Successfully deleted notification settings for keycloak user id: {}", keycloakUserId);
  }
}
