package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pet")
public class Pet {
    @Id
    private String id;
    private String userId;
    private PetImage petImage;
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
