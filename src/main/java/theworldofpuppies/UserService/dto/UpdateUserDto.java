package theworldofpuppies.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theworldofpuppies.UserService.model.Image;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String email;
    private String username;
    private Image image;
    private String fetchUrl;
}
