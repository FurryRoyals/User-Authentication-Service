package theworldofpuppies.UserService.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theworldofpuppies.UserService.model.Aggression;
import theworldofpuppies.UserService.model.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePetRequest {
    private String id;
    private String name;
    private Gender gender;
    private String breed;
    private String age;
    private String weight;
    private Boolean isVaccinated;
    private Aggression aggression;
}
