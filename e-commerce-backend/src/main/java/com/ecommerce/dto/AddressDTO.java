package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private String type; // SHIPPING, BILLING, BOTH
    private Date createdAt;
    private Date updatedAt;

    // Business methods
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null)
            sb.append(addressLine1);
        if (addressLine2 != null)
            sb.append(", ").append(addressLine2);
        if (city != null)
            sb.append(", ").append(city);
        if (state != null)
            sb.append(", ").append(state);
        if (country != null)
            sb.append(", ").append(country);
        return sb.toString();
    }
}
