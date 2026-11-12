package ua.kpi.blindpig.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SaveCourierRequest(
        @NotBlank(message = "Courier name cannot be blank")
        String name,

        @NotBlank(message = "Courier phone cannot be blank")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Courier phone must be in valid format (10 to 15 digits)")
        String phone,

        boolean available
) {}
