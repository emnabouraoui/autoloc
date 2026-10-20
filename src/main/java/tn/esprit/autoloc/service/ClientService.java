package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.repository.ClientRepository;
import tn.esprit.autoloc.web.dto.ClientRequestDTO;
import tn.esprit.autoloc.web.dto.ClientResponseDTO;
import tn.esprit.autoloc.web.exception.ConflictException;
import tn.esprit.autoloc.web.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientResponseDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(ClientResponseDTO::from)
                .toList();
    }

    public ClientResponseDTO findById(Long id) {
        return ClientResponseDTO.from(getEntity(id));
    }

    @Transactional
    public ClientResponseDTO create(ClientRequestDTO dto) {
        validateUniqueness(dto, null);
        Client client = new Client();
        apply(client, dto);
        return ClientResponseDTO.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponseDTO update(Long id, ClientRequestDTO dto) {
        Client client = getEntity(id);
        validateUniqueness(dto, client);
        apply(client, dto);
        return ClientResponseDTO.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.delete(getEntity(id));
    }

    private Client getEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private void validateUniqueness(ClientRequestDTO dto, Client current) {
        String email = dto.email().trim();
        if ((current == null || !current.getEmail().equalsIgnoreCase(email))
                && clientRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Un client avec cet email existe déjà");
        }

        String numPermis = dto.numPermis().trim();
        if ((current == null || !current.getNumPermis().equalsIgnoreCase(numPermis))
                && clientRepository.existsByNumPermisIgnoreCase(numPermis)) {
            throw new ConflictException("Un client avec ce numéro de permis existe déjà");
        }
    }

    private void apply(Client client, ClientRequestDTO dto) {
        client.setNom(dto.nom().trim());
        client.setPrenom(dto.prenom().trim());
        client.setEmail(dto.email().trim());
        client.setTelephone(dto.telephone() == null ? null : dto.telephone().trim());
        client.setNumPermis(dto.numPermis().trim());
        client.setDateInscription(dto.dateInscription());
    }
}
