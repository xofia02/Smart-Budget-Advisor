package br.ucsal.smartbudget.model;

import br.ucsal.smartbudget.model.enums.CategoriaGasto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrevisaoCategoria {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaGasto categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal recomendado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maximo;
}
