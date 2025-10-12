package theworldofpuppies.UserService.service.user;

import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.UpdateUserDto;
import theworldofpuppies.UserService.request.UpdateUserRequest;

public interface UpdateUser {

    UpdateUserDto updateUser(MultipartFile file, UpdateUserRequest request, String phoneNumber);
}
