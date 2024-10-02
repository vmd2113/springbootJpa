package vn.duongw.springdatajpademo.dto.reponse;

import lombok.Getter;

@Getter

public class AddressDTO {
    private String apartmentNumber;
    private String floor;
    private String building;
    private String streetNumber;
    private String street;
    private String city;
    private String country;
    private Integer addressType;
}
