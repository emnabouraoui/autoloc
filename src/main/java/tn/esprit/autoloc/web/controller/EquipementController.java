package tn.esprit.autoloc.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.service.EquipementService;
import tn.esprit.autoloc.web.dto.EquipementRequestDTO;
import tn.esprit.autoloc.web.dto.EquipementResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/equipements")
public class EquipementController {

    private final EquipementService equipementService;

    public EquipementController(EquipementService equipementService) {
        this.equipementService = equipementService;
    }

    @GetMapping
    public List<EquipementResponseDTO> findAll() {
        return equipementService.findAll();
    }

    @GetMapping("/{id}")
    public EquipementResponseDTO findById(@PathVariable Long id) {
        return equipementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipementResponseDTO create(@Valid @RequestBody EquipementRequestDTO dto) {
        return equipementService.create(dto);
    }

    @PutMapping("/{id}")
    public EquipementResponseDTO update(@PathVariable Long id, @Valid @RequestBody EquipementRequestDTO dto) {
        return equipementService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        equipementService.delete(id);
    }
}
