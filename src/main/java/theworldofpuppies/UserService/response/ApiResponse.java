package theworldofpuppies.UserService.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private Boolean success;
    private Object data;
}
