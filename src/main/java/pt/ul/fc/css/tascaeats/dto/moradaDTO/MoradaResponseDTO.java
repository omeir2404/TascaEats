package pt.ul.fc.css.tascaeats.dto.moradaDTO;

public class MoradaResponseDTO {
    private String street;
    private String number;
    private String complement;
    private String country;
    private String district;
    private String city;
    private String postalCode;

    public MoradaResponseDTO() {
    }

    public MoradaResponseDTO(String street, String number, String complement, String country, String district, String city, String postalCode) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.country = country;
        this.district = district;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}

