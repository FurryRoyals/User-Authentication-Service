package theworldofpuppies.UserService.controller.pet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import theworldofpuppies.UserService.dto.PetDto;
import theworldofpuppies.UserService.exception.AlreadyExistsException;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.exception.UnauthorizedException;
import theworldofpuppies.UserService.request.AddPetRequest;
import theworldofpuppies.UserService.request.UpdatePetRequest;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.ApiService;
import theworldofpuppies.UserService.service.pet.PetService;
import theworldofpuppies.UserService.utils.JwtUtils;

import java.util.List;

@RestController
@RequestMapping("${prefix}/pet")
@RequiredArgsConstructor
@Slf4j
public class PetController {

    private final ApiService apiService;
    private final JwtUtils jwtUtils;
    private final PetService petService;

    @PostMapping(value = "add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addPet(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart("request") AddPetRequest request,  // Changed to @RequestPart
            @RequestPart("image") MultipartFile image) {
        try {
            String userId = extractAndValidateTokenAndGetUser(authorizationHeader);
            PetDto petDto = petService.addPet(request, userId, image);
            return ResponseEntity
                    .ok(new ApiResponse("Pet added successfully", true, petDto));
        } catch (ResourceNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @PutMapping(path = "update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updatePet(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart UpdatePetRequest request,
            @RequestPart(required = false, value = "image") MultipartFile image) {
        try {
            extractAndValidateTokenAndGetUser(authorizationHeader); // ✅ ensure token valid
            PetDto updatedPet = petService.updatePet(request, image);
            return ResponseEntity.ok(new ApiResponse("Pet updated successfully", true, updatedPet));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("{petId}")
    public ResponseEntity<ApiResponse> getPetById(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String petId) {
        try {
            extractAndValidateTokenAndGetUser(authorizationHeader); // ✅ ensure token valid
            PetDto petDto = petService.getPetById(petId);
            return ResponseEntity.ok(new ApiResponse("Pet retrieved successfully", true, petDto));
        } catch (ResourceNotFoundException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("all")
    public ResponseEntity<ApiResponse> getPetsByIds(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody List<String> petIds) {
        try {
            extractAndValidateTokenAndGetUser(authorizationHeader); // ✅ ensure token valid
            List<PetDto> petDtos = petService.getPets(petIds);
            return ResponseEntity.ok(new ApiResponse("Pet retrieved successfully", true, petDtos));
        } catch (ResourceNotFoundException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @DeleteMapping("{petId}")
    public ResponseEntity<ApiResponse> deletePet(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String petId) {
        try {
            extractAndValidateTokenAndGetUser(authorizationHeader); // ✅ ensure token valid
            petService.deletePet(petId);
            return ResponseEntity.ok(new ApiResponse("Pet deleted successfully", true, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    private String extractAndValidateTokenAndGetUser(String authorizationHeader) {
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        if (!jwtUtils.validateToken(token)) {
            throw new BadCredentialsException("Invalid token");
        }
        String phoneNumber = jwtUtils.extractPhoneNumber(token);
        boolean isVerified = apiService.validateUser(phoneNumber);
        if (!isVerified) {
            throw new UnauthorizedException("User is not authorized to access this data");
        }
        return apiService.getUserId(phoneNumber);
    }
}
