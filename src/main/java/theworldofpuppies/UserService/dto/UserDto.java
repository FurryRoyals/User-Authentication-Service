package theworldofpuppies.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private String userId;
    private List<String> petIds = new ArrayList<>();
    private String username;
    private String email;
    private String phoneNumber;
    private String token;
    private Long expirationTime;
}
