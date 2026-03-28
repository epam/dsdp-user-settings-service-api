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

package com.epam.digital.data.platform.settings.api.controller.internal;

import com.epam.digital.data.platform.settings.api.model.DetailedErrorResponse;
import com.epam.digital.data.platform.settings.api.service.NotificationSettingsService;
import com.epam.digital.data.platform.settings.model.dto.CreateNotificationSettingsInputDto;
import com.epam.digital.data.platform.settings.model.dto.CreateNotificationSettingsOutputDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"development", "local", "docker"})
@RestController
@RequestMapping("/internalapi/settings")
@Tag(description = "Internal notification settings service Rest API", name = "internal-notification-settings-api")
public class InternalNotificationSettingsController {

  private final Logger log = LoggerFactory.getLogger(InternalNotificationSettingsController.class);

  private final NotificationSettingsService notificationSettingsService;

  public InternalNotificationSettingsController(NotificationSettingsService notificationSettingsService) {
    this.notificationSettingsService = notificationSettingsService;
  }

  @Operation(
      summary = "Create notification settings and channels",
      description = """
              ### Endpoint purpose:
               This endpoint allows to create notification settings and channels for a user by their Keycloak ID. It creates or updates settings record and creates notification channels (EMAIL) with specified addresses and activation status.
              ### Authorization:
               This endpoint requires valid user authentication. To access this endpoint, the request must include a valid access token in the _X-Access-Token_ header, otherwise, the API will return a _401 Unauthorized_ status code.""",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateNotificationSettingsInputDto.class),
              examples = {
                  @ExampleObject(value = """
                          {
                            "keycloakUserId": "123e4567-e89b-12d3-a456-426614174000",
                            "channels": [
                              {
                                "channel": "email",
                                "address": "user@example.com",
                                "isActivated": true
                              }
                            ]
                          }"""
                  )
              }
          )
      ),
      responses = {
          @ApiResponse(
              description = "Notification settings created successfully",
              responseCode = "201",
              content = @Content(schema = @Schema(implementation = CreateNotificationSettingsOutputDto.class),
                  examples = @ExampleObject(value = """
                          {
                            "settingsId": "789e4567-e89b-12d3-a456-426614174999",
                            "channels": [
                              {
                                "id": "456e4567-e89b-12d3-a456-426614174111",
                                "channel": "email",
                                "address": "user@example.com",
                                "isActivated": true,
                                "createdAt": "2026-02-02T10:30:00"
                              }
                            ],
                            "createdAt": "2026-02-02T10:30:00"
                          }"""
                  ))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad Request - Invalid input data",
              content = @Content(schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @PostMapping("/notifications")
  public ResponseEntity<CreateNotificationSettingsOutputDto> createNotificationSettings(
      @RequestBody @Valid CreateNotificationSettingsInputDto input,
      @Parameter(hidden = true) @RequestHeader("X-Access-Token") String accessToken) {
    log.info("Create notification settings called for keycloak user id: {}", input.getKeycloakUserId());
    var response = notificationSettingsService.createNotificationSettings(input);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Delete notification settings and channels",
      description = """
              ### Endpoint purpose:
               This endpoint allows to delete notification settings and all associated channels for a user by their Keycloak ID. It deletes all notification channel records and the settings record.
              ### Authorization:
               This endpoint requires valid user authentication. To access this endpoint, the request must include a valid access token in the _X-Access-Token_ header, otherwise, the API will return a _401 Unauthorized_ status code.""",
      parameters = {
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              in = ParameterIn.PATH,
              name = "keycloakUserId",
              description = "Keycloak user ID",
              required = true,
              schema = @Schema(type = "string", format = "uuid"),
              example = "123e4567-e89b-12d3-a456-426614174000"
          )
      },
      responses = {
          @ApiResponse(
              description = "Notification settings deleted successfully",
              responseCode = "204"
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad Request - Invalid keycloak user ID",
              content = @Content(schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Not Found - Settings not found for the given keycloak user ID",
              content = @Content(schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @DeleteMapping("/notifications/{keycloakUserId}")
  public ResponseEntity<Void> deleteNotificationSettings(
      @PathVariable("keycloakUserId") UUID keycloakUserId,
      @Parameter(hidden = true) @RequestHeader("X-Access-Token") String accessToken) {
    log.info("Delete notification settings called for keycloak user id: {}", keycloakUserId);
    notificationSettingsService.deleteNotificationSettings(keycloakUserId);
    return ResponseEntity.noContent().build();
  }
}
