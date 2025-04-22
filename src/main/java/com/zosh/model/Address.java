package com.zosh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Locality is required")
    private String locality;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pinCode;

    @Pattern(regexp = "\\d{10}", message = "Mobile must be 10 digits")
    private String mobile;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	@Override
	public String toString() {
		return "Address [id=" + id + ", name=" + name + ", locality=" + locality + ", address=" + address + ", city="
				+ city + ", state=" + state + ", pinCode=" + pinCode + ", mobile=" + mobile + ", user=" + user + "]";
	}
}
