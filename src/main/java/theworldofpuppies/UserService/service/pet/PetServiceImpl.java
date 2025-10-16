package theworldofpuppies.UserService.service.pet;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.PetDto;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.Pet;
import theworldofpuppies.UserService.model.Image;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.PetRepository;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.request.AddPetRequest;
import theworldofpuppies.UserService.request.UpdatePetRequest;
import theworldofpuppies.UserService.service.s3.StorageService;
import theworldofpuppies.UserService.utils.DateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;
    private final DateTimeFormatter dateTimeFormatter;

    private final String petNotFound = "No pet found with: ";


    @Override
    public PetDto getPetById(String petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException(petNotFound + petId));
        return convertToDto(pet);
    }

    @Override
    public List<PetDto> getPets(List<String> petIds) {
        List<PetDto> petDtos = petIds.stream()
                .map(id -> petRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(petNotFound + id)))
                .map(this::convertToDto)
                .toList();
        petDtos.stream().peek(petDto -> {
            String downloadUrl = storageService.generatePresignedUrl(petDto.getPetImage().getS3Key(), 15);
            petDto.setDownloadUrl(downloadUrl);
        }).toList();
        return petDtos;
    }


    @Transactional
    @Override
    public PetDto addPet(AddPetRequest request, String userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this: " + userId));
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setPetImage(null);
        pet.setName(request.getName());
        pet.setGender(request.getGender());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setWeight(request.getWeight());
        pet.setIsVaccinated(request.getIsVaccinated());
        pet.setAggression(request.getAggression());
        pet.setCreationDate(dateTimeFormatter.convertLocalDateTimeToLong(LocalDate.now(), LocalTime.now()));

        // Save initial Pet (to get the id)
        Pet savedPet = petRepository.save(pet);

        user.getPetIds().add(savedPet.getId());
        userRepository.save(user);

        // Handle image if present
        if (image != null) {
            String s3Key = "pet/" + savedPet.getId() + "/" + image.getOriginalFilename();
            storageService.uploadFileToS3(image, s3Key);
            Image embeddedImage = new Image();
            embeddedImage.setFileName(image.getOriginalFilename());
            embeddedImage.setFileType(image.getContentType());
            embeddedImage.setS3Key(s3Key);

            savedPet.setPetImage(embeddedImage);
            savedPet = petRepository.save(savedPet);
        }

        PetDto petDto = convertToDto(savedPet);

        String downloadUrl = storageService.generatePresignedUrl(petDto.getPetImage().getS3Key(), 15);
        petDto.setDownloadUrl(downloadUrl);
        return petDto;
    }

    @Override
    @Transactional
    public PetDto updatePet(UpdatePetRequest request, MultipartFile image) {
        Pet pet = petRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + request.getId()));

        Optional.ofNullable(request.getName())
                .filter(name -> !name.equals(pet.getName()))
                .ifPresent(pet::setName);

        Optional.ofNullable(request.getGender())
                .filter(gender -> !gender.equals(pet.getGender()))
                .ifPresent(pet::setGender);

        Optional.ofNullable(request.getBreed())
                .filter(breed -> !breed.equals(pet.getBreed()))
                .ifPresent(pet::setBreed);

        Optional.ofNullable(request.getAge())
                .filter(age -> !age.equals(pet.getAge()))
                .ifPresent(pet::setAge);

        Optional.ofNullable(request.getWeight())
                .filter(weight -> !weight.equals(pet.getWeight()))
                .ifPresent(pet::setWeight);

        Optional.ofNullable(request.getAggression())
                .filter(aggression -> !aggression.equals(pet.getAggression()))
                .ifPresent(pet::setAggression);

        Optional.of(request.getIsVaccinated() )
                .filter(v -> v != pet.getIsVaccinated())
                .ifPresent(pet::setIsVaccinated);

        // Always update timestamp
        pet.setUpdatedDate(dateTimeFormatter.convertLocalDateTimeToLong(LocalDate.now(), LocalTime.now()));

        // ✅ Update embedded image if a new one is provided
        if (image != null) {
            String s3Key = "images/" + pet.getId() + "/" + image.getOriginalFilename();
            storageService.uploadFileToS3(image, s3Key);

            Image newImage = new Image();
            newImage.setFileName(image.getOriginalFilename());
            newImage.setFileType(image.getContentType());

            pet.setPetImage(newImage);
        }

        Pet updatedPet = petRepository.save(pet);
        PetDto petDto = convertToDto(updatedPet);
        String downloadUrl = storageService.generatePresignedUrl(petDto.getPetImage().getS3Key(), 15);
        petDto.setDownloadUrl(downloadUrl);
        return petDto;
    }

    @Override
    @Transactional
    public void deletePet(String id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(petNotFound + id));
        User user = userRepository.findById(pet.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this: " + pet.getUserId()));

        user.getPetIds().remove(pet.getId());
        userRepository.save(user);

        if (pet.getPetImage().getS3Key() != null) {
            storageService.deleteFileFromS3(pet.getPetImage().getS3Key());
        }
        petRepository.delete(pet);
    }

    private PetDto convertToDto(Pet pet) {
        return modelMapper.map(pet, PetDto.class);
    }
}
