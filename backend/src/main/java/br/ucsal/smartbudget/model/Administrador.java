package br.ucsal.smartbudget.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class Administrador extends Usuario {
    @Override
    public String tipo() {
        return "ADMIN";
    }
}
