/*
 * Copyright 2026 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.settings.api.controller.internal;

import com.epam.digital.data.platform.settings.api.entity.OtpEntity;
import com.epam.digital.data.platform.settings.api.model.DetailedErrorResponse;
import com.epam.digital.data.platform.settings.api.model.OtpData;
import com.epam.digital.data.platform.settings.api.repository.OtpRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile({"development", "local", "docker"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/internalapi/otp")
@Tag(name = "Internal OTP API", description = "Internal API for managing OTP (One-Time Password) data for testing " +
    "purposes. Available only in development, local, and docker profiles.")
public class InternalOtpController {

  private final OtpRepository otpRepository;

  private static String toId(String userId, String channel) {
    return String.format("%s/%s", userId, channel);
  }

  private static OtpEntity toOtpEntity(String id, OtpData otpData) {
    return OtpEntity.builder().id(id).otpData(otpData).build();
  }

  @Operation(
      summary = "Retrieve OTP data for a user and channel",
      description = """
          ### Endpoint purpose:
          This internal endpoint allows to retrieve stored OTP (One-Time Password) data for a specific user and
          communication channel. Used for testing and development purposes only.
          """,
      parameters = {
          @Parameter(
              name = "userId",
              description = "User identifier",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "channel",
              description = "Communication channel (e.g., email, diia, inbox)",
              required = true,
              schema = @Schema(type = "string")
          )
      },
      responses = {
          @ApiResponse(
              description = "Returns OTP data for the specified user and channel",
              responseCode = "200",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = OtpData.class),
                  examples = @ExampleObject(value = """
                      {
                        "address": "user@example.com",
                        "verificationCode": "123456"
                      }""")
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "OTP data not found for the specified user and channel",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class)
              )
          )
      }
  )
  @GetMapping("/{userId}/{channel}")
  public OtpData getOtp(
      @PathVariable String userId,
      @PathVariable String channel) {
    var id = toId(userId, channel);
    return otpRepository.findById(id)
        .map(OtpEntity::getOtpData)
        .orElseThrow(EntityNotFoundException::new);
  }

  @Operation(
      summary = "Create or update OTP data for a user and channel",
      description = """
          ### Endpoint purpose:
          This internal endpoint allows to create or update OTP (One-Time Password) data for a specific user and
          communication channel. Used for testing and development purposes only.
          """,
      parameters = {
          @Parameter(
              name = "userId",
              description = "User identifier",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "channel",
              description = "Communication channel (e.g., email, diia, inbox)",
              required = true,
              schema = @Schema(type = "string")
          )
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          description = "OTP data to store",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = OtpData.class),
              examples = @ExampleObject(value = """
                  {
                    "address": "user@example.com",
                    "verificationCode": "123456"
                  }""")
          )
      ),
      responses = {
          @ApiResponse(
              description = "OTP data successfully created or updated",
              responseCode = "200",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = OtpData.class),
                  examples = @ExampleObject(value = """
                      {
                        "address": "user@example.com",
                        "verificationCode": "123456"
                      }""")
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid request body",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class)
              )
          )
      }
  )
  @PutMapping("/{userId}/{channel}")
  public OtpData putOtp(
      @PathVariable String userId,
      @PathVariable String channel,
      @RequestBody OtpData otpData) {
    var id = toId(userId, channel);
    var entity = toOtpEntity(id, otpData);
    return otpRepository.save(entity).getOtpData();
  }

  @Operation(
      summary = "Delete OTP data for a user and channel",
      description = """
          ### Endpoint purpose:
          This internal endpoint allows to delete stored OTP (One-Time Password) data for a specific user and
          communication channel. Used for testing and development purposes only.
          """,
      parameters = {
          @Parameter(
              name = "userId",
              description = "User identifier",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              name = "channel",
              description = "Communication channel (e.g., email, diia, inbox)",
              required = true,
              schema = @Schema(type = "string")
          )
      },
      responses = {
          @ApiResponse(
              description = "OTP data successfully deleted",
              responseCode = "204"
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class)
              )
          )
      }
  )
  @DeleteMapping("/{userId}/{channel}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteOtp(
      @PathVariable String userId,
      @PathVariable String channel) {
    var id = toId(userId, channel);
    otpRepository.deleteById(id);
  }
}
