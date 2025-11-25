package com.ecommerce.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "type")
    private String type; // SHIPPING, BILLING, BOTH

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
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

    public boolean isShippingAddress() {
        return "SHIPPING".equals(type) || "BOTH".equals(type);
    }

    public boolean isBillingAddress() {
        return "BILLING".equals(type) || "BOTH".equals(type);
    }
}
