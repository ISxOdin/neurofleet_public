package ch.zhaw.neurofleet.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Auth0UserDTO {
    private String user_id;
    private String email;
    private String name;
}
