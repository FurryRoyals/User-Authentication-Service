package theworldofpuppies.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetSnapshot {
    private String name;
    private String breed;
    private String age;
}
