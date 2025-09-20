package theworldofpuppies.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theworldofpuppies.UserService.model.Aggression;
import theworldofpuppies.UserService.model.Gender;
import theworldofpuppies.UserService.model.PetImage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetDto {
    private String id;
    private String userId;
    private PetImage petImage;
    private String downloadUrl;
    private String name;
    private Gender gender;
    private String breed;
    private String age;
    private String weight;
    private Aggression aggression;
    private Boolean isVaccinated;
    private Long creationDate;
    private Long updatedDate;
}
