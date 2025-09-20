package theworldofpuppies.UserService.service.pet;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.PetDto;
import theworldofpuppies.UserService.request.AddPetRequest;
import theworldofpuppies.UserService.request.UpdatePetRequest;

import java.util.List;

public interface PetService {
    PetDto getPetById(String petId);

    List<PetDto> getPets(List<String> petIds);

    @Transactional
    PetDto addPet(AddPetRequest request, String userId, MultipartFile image);

    @Transactional
    PetDto updatePet(UpdatePetRequest request, MultipartFile image);

    @Transactional
    void deletePet(String id);
}
