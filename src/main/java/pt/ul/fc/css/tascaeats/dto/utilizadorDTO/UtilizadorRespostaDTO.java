package pt.ul.fc.css.tascaeats.dto.utilizadorDTO;

import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaResponseDTO;

public class UtilizadorRespostaDTO {
    private Long id;
    private String name;
    private String email;
    private String type;
    private Boolean available;
    private MoradaResponseDTO address;

    public UtilizadorRespostaDTO() {
    }

    public UtilizadorRespostaDTO(Long id, String name, String email, String type, Boolean available, MoradaResponseDTO address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
        this.available = available;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public MoradaResponseDTO getAddress() {
        return address;
    }

    public void setAddress(MoradaResponseDTO address) {
        this.address = address;
    }
}

