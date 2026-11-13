package tn.esprit.autoloc.web.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.service.tarification.TarificationService;
import tn.esprit.autoloc.web.dto.TarificationRequestDTO;
import tn.esprit.autoloc.web.dto.TarificationResponseDTO;

import java.util.Set;

@RestController
@RequestMapping("/api/tarifications")
public class TarificationController {

    private final TarificationService tarificationService;

    public TarificationController(TarificationService tarificationService) {
        this.tarificationService = tarificationService;
    }

    @PostMapping("/calculer")
    public TarificationResponseDTO calculer(@Valid @RequestBody TarificationRequestDTO dto) {
        double montant = tarificationService.calculer(
                dto.type(),
                dto.tarifJournalier().doubleValue(),
                dto.jours()
        );
        return new TarificationResponseDTO(
                dto.type(),
                dto.tarifJournalier(),
                dto.jours(),
                montant
        );
    }

    @GetMapping("/types")
    public Set<String> typesDisponibles() {
        return tarificationService.typesDisponibles();
    }
}
