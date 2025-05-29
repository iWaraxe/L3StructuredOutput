package com.coherentsolutions.l3structuredoutput.s9.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates advanced Jackson annotations for controlling JSON output structure.
 * This class showcases property ordering, descriptions, and conditional inclusion.
 */
@JsonPropertyOrder({"userId", "username", "email", "fullName", "age", "preferences", "accountStatus", "lastLogin", "tags"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfile(
        @JsonPropertyDescription("Unique identifier for the user")
        @JsonProperty("user_id")
        String userId,

        @JsonPropertyDescription("Username for login purposes")
        String username,

        @JsonPropertyDescription("User's email address for communication")
        String email,

        @JsonPropertyDescription("User's full name")
        @JsonProperty("full_name")
        String fullName,

        @JsonPropertyDescription("User's age in years")
        Integer age,

        @JsonPropertyDescription("User preferences including settings and configurations")
        Map<String, Object> preferences,

        @JsonPropertyDescription("Current status of the account: ACTIVE, SUSPENDED, or PENDING")
        @JsonProperty("account_status")
        AccountStatus accountStatus,

        @JsonPropertyDescription("Timestamp of the user's last login")
        @JsonProperty("last_login")
        LocalDateTime lastLogin,

        @JsonPropertyDescription("Tags associated with the user for categorization")
        List<String> tags,

        // This field will be ignored in JSON output
        @JsonIgnore
        String internalNotes
) {
    public enum AccountStatus {
        ACTIVE, SUSPENDED, PENDING
    }
}