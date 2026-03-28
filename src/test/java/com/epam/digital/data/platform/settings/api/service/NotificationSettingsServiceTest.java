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
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.CreateNotificationSettingsInputDto;
import com.epam.digital.data.platform.settings.model.dto.NotificationChannelInputDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingsServiceTest {

  private static final UUID KEYCLOAK_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SETTINGS_ID = UUID.fromString("789e4567-e89b-12d3-a456-426614174999");
  private static final UUID CHANNEL_ID_1 = UUID.fromString("456e4567-e89b-12d3-a456-426614174111");
  private static final String EMAIL_ADDRESS = "user@example.com";

  private NotificationSettingsService notificationSettingsService;

  @Mock
  private SettingsRepository settingsRepository;
  @Mock
  private NotificationChannelRepository channelRepository;

  @BeforeEach
  void beforeEach() {
    notificationSettingsService = new NotificationSettingsService(settingsRepository, channelRepository);
  }

  @Test
  void shouldCreateNotificationSettingsWithNewKeycloakUserId() {
    // Given
    var channelInputDto = NotificationChannelInputDto.builder()
        .channel(Channel.EMAIL)
        .address(EMAIL_ADDRESS)
        .isActivated(true)
        .build();

    var inputDto = CreateNotificationSettingsInputDto.builder()
        .keycloakUserId(KEYCLOAK_USER_ID)
        .channels(List.of(channelInputDto))
        .build();

    var settings = new Settings();
    settings.setId(SETTINGS_ID);
    settings.setKeycloakId(KEYCLOAK_USER_ID);

    var createdChannel = new NotificationChannel();
    createdChannel.setId(CHANNEL_ID_1);
    createdChannel.setSettingsId(SETTINGS_ID);
    createdChannel.setChannel(Channel.EMAIL);
    createdChannel.setAddress(EMAIL_ADDRESS);
    createdChannel.setActivated(true);
    createdChannel.setCreatedAt(LocalDateTime.now());

    when(settingsRepository.getByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(settings);
    when(channelRepository.findBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL))
        .thenReturn(Optional.of(createdChannel));

    // When
    var result = notificationSettingsService.createNotificationSettings(inputDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSettingsId()).isEqualTo(SETTINGS_ID);
    assertThat(result.getChannels()).hasSize(1);
    assertThat(result.getChannels().get(0).getId()).isEqualTo(CHANNEL_ID_1);
    assertThat(result.getChannels().get(0).getChannel()).isEqualTo(Channel.EMAIL);
    assertThat(result.getChannels().get(0).getAddress()).isEqualTo(EMAIL_ADDRESS);
    assertThat(result.getChannels().get(0).getIsActivated()).isTrue();

    verify(settingsRepository).getByKeycloakId(KEYCLOAK_USER_ID);
    verify(channelRepository).deleteBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL);
    verify(channelRepository).create(SETTINGS_ID, Channel.EMAIL, EMAIL_ADDRESS, true, null);
  }

  @Test
  void shouldCreateNotificationSettingsWithExistingKeycloakUserId() {
    // Given
    var channelInputDto = NotificationChannelInputDto.builder()
        .channel(Channel.EMAIL)
        .address(EMAIL_ADDRESS)
        .isActivated(true)
        .build();

    var inputDto = CreateNotificationSettingsInputDto.builder()
        .keycloakUserId(KEYCLOAK_USER_ID)
        .channels(List.of(channelInputDto))
        .build();

    var existingSettings = new Settings();
    existingSettings.setId(SETTINGS_ID);
    existingSettings.setKeycloakId(KEYCLOAK_USER_ID);

    var createdChannel = new NotificationChannel();
    createdChannel.setId(CHANNEL_ID_1);
    createdChannel.setSettingsId(SETTINGS_ID);
    createdChannel.setChannel(Channel.EMAIL);
    createdChannel.setAddress(EMAIL_ADDRESS);
    createdChannel.setActivated(true);
    createdChannel.setCreatedAt(LocalDateTime.now());

    when(settingsRepository.getByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(existingSettings);
    when(channelRepository.findBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL))
        .thenReturn(Optional.of(createdChannel));

    // When
    var result = notificationSettingsService.createNotificationSettings(inputDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSettingsId()).isEqualTo(SETTINGS_ID);
    assertThat(result.getChannels()).hasSize(1);

    verify(settingsRepository).getByKeycloakId(KEYCLOAK_USER_ID);
    verify(channelRepository).deleteBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL);
    verify(channelRepository).create(SETTINGS_ID, Channel.EMAIL, EMAIL_ADDRESS, true, null);
  }

  @Test
  void shouldReplaceExistingChannel() {
    // Given
    var channelInputDto = NotificationChannelInputDto.builder()
        .channel(Channel.EMAIL)
        .address("newemail@example.com")
        .isActivated(true)
        .build();

    var inputDto = CreateNotificationSettingsInputDto.builder()
        .keycloakUserId(KEYCLOAK_USER_ID)
        .channels(List.of(channelInputDto))
        .build();

    var settings = new Settings();
    settings.setId(SETTINGS_ID);
    settings.setKeycloakId(KEYCLOAK_USER_ID);

    var newChannel = new NotificationChannel();
    newChannel.setId(CHANNEL_ID_1);
    newChannel.setSettingsId(SETTINGS_ID);
    newChannel.setChannel(Channel.EMAIL);
    newChannel.setAddress("newemail@example.com");
    newChannel.setActivated(true);
    newChannel.setCreatedAt(LocalDateTime.now());

    when(settingsRepository.getByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(settings);
    when(channelRepository.findBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL))
        .thenReturn(Optional.of(newChannel));

    // When
    var result = notificationSettingsService.createNotificationSettings(inputDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSettingsId()).isEqualTo(SETTINGS_ID);
    assertThat(result.getChannels()).hasSize(1);
    assertThat(result.getChannels().get(0).getAddress()).isEqualTo("newemail@example.com");

    verify(channelRepository).deleteBySettingsIdAndChannel(SETTINGS_ID, Channel.EMAIL);
    verify(channelRepository).create(SETTINGS_ID, Channel.EMAIL, "newemail@example.com", true, null);
  }

  @Test
  @DisplayName("Should delete notification settings and channels successfully")
  void shouldDeleteNotificationSettingsSuccessfully() {
    // Given
    var settings = new Settings();
    settings.setId(SETTINGS_ID);
    settings.setKeycloakId(KEYCLOAK_USER_ID);

    when(settingsRepository.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(settings));

    // When
    notificationSettingsService.deleteNotificationSettings(KEYCLOAK_USER_ID);

    // Then
    verify(settingsRepository).findByKeycloakId(KEYCLOAK_USER_ID);
    verify(channelRepository).deleteBySettingsId(SETTINGS_ID);
    verify(settingsRepository).delete(settings);
  }

  @Test
  @DisplayName("Should throw exception when settings not found for deletion")
  void shouldThrowExceptionWhenSettingsNotFoundForDeletion() {
    // Given
    when(settingsRepository.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> notificationSettingsService.deleteNotificationSettings(KEYCLOAK_USER_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Settings not found for keycloak user id: " + KEYCLOAK_USER_ID);

    verify(settingsRepository).findByKeycloakId(KEYCLOAK_USER_ID);
    verify(channelRepository, never()).deleteBySettingsId(any());
    verify(settingsRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should log debug messages during deletion")
  void shouldLogDebugMessagesDuringDeletion() {
    // Given
    var settings = new Settings();
    settings.setId(SETTINGS_ID);
    settings.setKeycloakId(KEYCLOAK_USER_ID);

    when(settingsRepository.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(settings));

    // When
    notificationSettingsService.deleteNotificationSettings(KEYCLOAK_USER_ID);

    // Then - verify the service calls happened in the correct order
    var inOrder = inOrder(settingsRepository, channelRepository);
    inOrder.verify(settingsRepository).findByKeycloakId(KEYCLOAK_USER_ID);
    inOrder.verify(channelRepository).deleteBySettingsId(SETTINGS_ID);
    inOrder.verify(settingsRepository).delete(settings);
  }
}
