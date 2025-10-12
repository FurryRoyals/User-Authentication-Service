package theworldofpuppies.UserService.controller.pet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import theworldofpuppies.UserService.dto.PetDto;
import theworldofpuppies.UserService.exception.ResourceNotFoundException;
import theworldofpuppies.UserService.model.PetSnapshot;
import theworldofpuppies.UserService.response.ApiResponse;
import theworldofpuppies.UserService.service.pet.PetService;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${prefix}/")
@Slf4j
public class PetApiController {

    private final PetService petService;
    private final ModelMapper modelMapper;

    @GetMapping("pet")
    public ResponseEntity<ApiResponse> getPetById(@RequestParam String id) {
        try {
            PetDto petDto = petService.getPetById(id);
            PetSnapshot petSnapshot = convertToSnapshot(petDto);
            return ResponseEntity.ok(new ApiResponse("Pet snapshot fetched", true, petSnapshot));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    private PetSnapshot convertToSnapshot(PetDto petDto) {
        return modelMapper.map(petDto, PetSnapshot.class);
    }
}
