package theworldofpuppies.UserService.service.user;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.UpdateUserDto;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.Image;
import theworldofpuppies.UserService.model.User;
import theworldofpuppies.UserService.repository.UserRepository;
import theworldofpuppies.UserService.request.UpdateUserRequest;
import theworldofpuppies.UserService.service.s3.StorageService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UpdateUserImpl implements UpdateUser {

    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public UpdateUserDto updateUser(MultipartFile file, UpdateUserRequest request, String phoneNumber) {
        User existingUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + phoneNumber));

        if (file != null) {
            Image image = new Image();
            String s3Key = "user/" + existingUser.getId() + "/" + file.getOriginalFilename();
            storageService.uploadFileToS3(file, s3Key);
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setS3Key(s3Key);
            existingUser.setImage(image);
        }

        if (request.getUsername() != null && !request.getUsername().equals(existingUser.getUsername())) {
            existingUser.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(request.getEmail());
        }
        User updatedUser = userRepository.save(existingUser);

        UpdateUserDto updateUserDto = convertToDto(updatedUser);
        if (file != null) {
            updateUserDto.setFetchUrl(storageService.generatePresignedUrl(updateUserDto.getImage().getS3Key(), 4320));
        }
        return updateUserDto;
    }

    UpdateUserDto convertToDto(User user) {
        return modelMapper.map(user, UpdateUserDto.class);
    }
}
