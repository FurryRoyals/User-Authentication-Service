package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetImage {
    private String fileName;
    private String fileType;
    private String s3Key;
}