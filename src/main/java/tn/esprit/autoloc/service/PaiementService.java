package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.repository.ContratRepository;
import tn.esprit.autoloc.repository.PaiementRepository;
import tn.esprit.autoloc.web.dto.PaiementRequestDTO;
import tn.esprit.autoloc.web.dto.PaiementResponseDTO;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final ContratRepository contratRepository;

    public PaiementService(PaiementRepository paiementRepository, ContratRepository contratRepository) {
        this.paiementRepository = paiementRepository;
        this.contratRepository = contratRepository;
    }

    public List<PaiementResponseDTO> findAll() {
        return paiementRepository.findAll().stream()
                .map(PaiementResponseDTO::from)
                .toList();
    }

    public PaiementResponseDTO findById(Long id) {
        return PaiementResponseDTO.from(getEntity(id));
    }

    @Transactional
    public PaiementResponseDTO create(PaiementRequestDTO dto) {
        Contrat contrat = getContrat(dto.contratId());
        Paiement paiement = new Paiement();
        apply(paiement, dto, contrat);
        return PaiementResponseDTO.from(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementResponseDTO update(Long id, PaiementRequestDTO dto) {
        Paiement paiement = getEntity(id);
        Contrat contrat = getContrat(dto.contratId());
        apply(paiement, dto, contrat);
        return PaiementResponseDTO.from(paiementRepository.save(paiement));
    }

    @Transactional
    public void delete(Long id) {
        paiementRepository.delete(getEntity(id));
    }

    private Paiement getEntity(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    private Contrat getContrat(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    private void apply(Paiement paiement, PaiementRequestDTO dto, Contrat contrat) {
        paiement.setMontant(dto.montant());
        paiement.setDatePaiement(dto.datePaiement());
        paiement.setModePaiement(dto.modePaiement());
        paiement.setContrat(contrat);
    }
}
